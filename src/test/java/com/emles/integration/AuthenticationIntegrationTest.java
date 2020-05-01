package com.emles.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.emles.model.AppUser;
import com.emles.model.Authority;
import com.emles.model.AuthorityName;
import com.emles.model.Role;
import com.emles.model.RoleName;
import com.emles.model.request.ChangePasswordRequestModel;
import com.emles.model.request.UserLoginRequestModel;
import com.emles.model.request.admin.PasswordRequestModel;
import com.emles.repository.AuthorityRepository;
import com.emles.repository.RoleRepository;
import com.emles.repository.UserRepository;
import com.emles.utils.OAuthClientParams;
import com.emles.utils.Utils;
import com.emles.utils.ValidationErrorMessages;
import com.emles.EmlesMonoOauthApplication;


@ExtendWith(SpringExtension.class)
@SpringBootTest(
	webEnvironment = WebEnvironment.DEFINED_PORT,
	classes = EmlesMonoOauthApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class AuthenticationIntegrationTest extends BaseIntegrationTest {

	private UserRepository userRepository;

	private AuthorityRepository authorityRepository;

	private RoleRepository roleRepository;
	
	private JdbcClientDetailsService clientDetailsService;
	
	private AppUser standardUser;
	
	private AppUser adminUser;
	
	private UserLoginRequestModel credentials = new UserLoginRequestModel();
	
	private ChangePasswordRequestModel newPasswords = new ChangePasswordRequestModel();

	private ClientDetails clientDetails;
	
	private String clientId = "oauth_client_id";

	private OAuthClientParams params;
	
	@Autowired
	public AuthenticationIntegrationTest(UserRepository userRepository, AuthorityRepository authorityRepository,
			RoleRepository roleRepository, JdbcClientDetailsService clientDetailsService) {
		this.userRepository = userRepository;
		this.authorityRepository = authorityRepository;
		this.roleRepository = roleRepository;
		this.clientDetailsService = clientDetailsService;
	}

	@BeforeEach
	public void setUp() {	
		Authority authority = createAuthority(AuthorityName.READ_AUTHORITY);
		Authority createAuthority = createAuthority(AuthorityName.CREATE_AUTHORITY);
		Role role = createRole(RoleName.ROLE_USER);
		Role admin = createRole(RoleName.ROLE_ADMIN);
		
		roleRepository.save(role);
		roleRepository.save(admin);
		authorityRepository.save(authority);
		authorityRepository.save(createAuthority);
		
		standardUser = createEnabledUser("test@test.com", Arrays.asList(role), Arrays.asList(authority));
		standardUser.setLastPasswordResetDate(Date.from(Instant.now()));
		userRepository.save(standardUser);
		
		adminUser = createEnabledUser("admin@test.com", Arrays.asList(admin), Arrays.asList(authority, createAuthority));
		adminUser.setLastPasswordResetDate(Date.from(Instant.now()));
		userRepository.save(adminUser);
		
		credentials.setEmail(standardUser.getEmail());
		credentials.setPassword(nonHashedPassword);
		
		
		newPasswords.setOldPassword(nonHashedPassword);
		PasswordRequestModel passwords = new PasswordRequestModel(newNonHashedPassword, newNonHashedPassword);
		newPasswords.setPasswords(passwords );
		
		int accessTokenValidity = 30;
		int refreshTokenValidity = 60;
		clientDetails = createBaseClientDetails(clientId , hashedPassword, adminUser.getAllAuthorities(), accessTokenValidity , refreshTokenValidity);
		clientDetailsService.addClientDetails(clientDetails);
		
		params = new OAuthClientParams();
		params.setClientDetailsPassword(nonHashedPassword);
		params.setClientId(clientId);
		params.setGrantType("password");
		params.setPassword(nonHashedPassword);
		params.setUsername(adminUser.getEmail());
	}
	
	@AfterEach
	public void tearDown() {
		userRepository.deleteAll();
		authorityRepository.deleteAll();
		roleRepository.deleteAll();
		clientDetailsService.listClientDetails().forEach(client -> {
			clientDetailsService.removeClientDetails(client.getClientId());
		});
	}
	
	@Test
	public void testSignInSuccess() throws Exception {
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.OK);

		assertTrue(oauthTokenResponse.containsKey("access_token"));
		assertTrue(oauthTokenResponse.containsKey("refresh_token"));
		assertTrue(oauthTokenResponse.containsKey("authorities"));
		assertTrue(oauthTokenResponse.containsKey("username"));
		List<String> userAuthorities = adminUser.getAllAuthoritiesAsStrings();
		List<?> authoritiesFromTokenResponse = (List<?>) oauthTokenResponse.get("authorities");
		assertEquals(userAuthorities.size(), authoritiesFromTokenResponse.size());
		authoritiesFromTokenResponse.forEach(a -> {
			String authorityFromToken = ((Map<?, ?>) a).get("authority").toString();
			assertTrue(userAuthorities.contains(authorityFromToken));
		});

		assertEquals(oauthTokenResponse.get("username"), adminUser.getEmail());

		adminUser = userRepository.findAppUserByUserDataEmail(adminUser.getEmail());

		assertNotNull(adminUser.getLastSignInDate());
		assertNotNull(adminUser.getLastSignInIp());

		// wait for access token expiration
		Utils.sleepSeconds(35000L);
		String accessToken = oauthTokenResponse.get("access_token").toString();
		signOut(accessToken, HttpStatus.UNAUTHORIZED);

		// use refresh token, check if new access token has been sent
		String refreshToken = oauthTokenResponse.get("refresh_token").toString();
		params.setRefreshToken(refreshToken);
		oauthTokenResponse = refreshTokenRequest(params, HttpStatus.OK);

		accessToken = oauthTokenResponse.get("access_token").toString();
		signOut(accessToken, HttpStatus.NO_CONTENT);
		signOut(accessToken, HttpStatus.UNAUTHORIZED);

		refreshToken = oauthTokenResponse.get("refresh_token").toString();
		params.setRefreshToken(refreshToken);
		refreshTokenRequest(params, HttpStatus.BAD_REQUEST);

		oauthTokenResponse = signIn(params, HttpStatus.OK);
		refreshToken = oauthTokenResponse.get("refresh_token").toString();
		params.setRefreshToken(refreshToken);
		accessToken = oauthTokenResponse.get("access_token").toString();
		// wait for refresh token expiration
		Utils.sleepSeconds(63000L);
		signOut(accessToken, HttpStatus.UNAUTHORIZED);
		refreshTokenRequest(params, HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testSignInShouldReturnUnauthorizedWhenUsernameIsInvalid() throws Exception {
		params.setUsername("invalid");
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.UNAUTHORIZED);
		assertFalse(oauthTokenResponse.containsKey("access_token"));
		assertFalse(oauthTokenResponse.containsKey("refresh_token"));
	}
	
	@Test
	public void testSignInShouldReturnUnauthorizedWhenPasswordIsInvalid() throws Exception {
		params.setPassword("invalid");
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.UNAUTHORIZED);
		assertFalse(oauthTokenResponse.containsKey("access_token"));
		assertFalse(oauthTokenResponse.containsKey("refresh_token"));
	}
	
	@Test
	public void testSignInShouldReturnUnauthorizedWhenClientIdIsInvalid() throws Exception {
		params.setClientId("invalid");
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.UNAUTHORIZED);
		assertFalse(oauthTokenResponse.containsKey("access_token"));
		assertFalse(oauthTokenResponse.containsKey("refresh_token"));
	}
	
	@Test
	public void testSignInShouldReturnUnauthorizedWhenClientDetailsPasswordIsInvalid() throws Exception {
		params.setClientDetailsPassword("invalid");
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.UNAUTHORIZED);
		assertFalse(oauthTokenResponse.containsKey("access_token"));
		assertFalse(oauthTokenResponse.containsKey("refresh_token"));
	}
	
	@Test
	public void testChangePasswordShouldReturnForbiddenWhenAccessTokenIsMissing() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		MvcResult mvcResult = sendChangePasswordRequest(newPasswords, params, "", HttpStatus.UNAUTHORIZED);
		Map<String, Object> responseMap = getJsonMap(mvcResult);
		assertTrue(responseMap.get("error_description").toString().contains("Invalid access token"));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testChangePasswordShouldSucceedWhenAllDataIsCorrect() throws Exception {
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.OK);
		String accessToken = oauthTokenResponse.get(accessTokenKey).toString();
		String refreshToken = oauthTokenResponse.get(refreshTokenKey).toString();
		MvcResult mvcResult = sendChangePasswordRequest(newPasswords, params, accessToken, HttpStatus.OK);
		
		oauthTokenResponse = getJsonMap(mvcResult);
		
		String newAccessToken =  oauthTokenResponse.get(accessTokenKey).toString();
		String newRefreshToken = oauthTokenResponse.get(refreshTokenKey).toString();
		assertNotEquals(accessToken, newAccessToken);
		assertNotEquals(refreshToken, newRefreshToken);
		signOut(accessToken, HttpStatus.UNAUTHORIZED);
		
		params.setRefreshToken(refreshToken);
		oauthTokenResponse = refreshTokenRequest(params, HttpStatus.BAD_REQUEST);
		aboutMeRequest(newAccessToken, params, HttpStatus.OK);
		
		params.setRefreshToken(newRefreshToken);
		oauthTokenResponse = refreshTokenRequest(params, HttpStatus.OK);
		newAccessToken = oauthTokenResponse.get(accessTokenKey).toString();
		
		signOut(newAccessToken, HttpStatus.NO_CONTENT);
		signIn(params, HttpStatus.UNAUTHORIZED);
		
		params.setPassword(newPasswords.getPasswords().getPassword());
		oauthTokenResponse = signIn(params, HttpStatus.OK);
		newAccessToken = oauthTokenResponse.get(accessTokenKey).toString();
		signOut(newAccessToken, HttpStatus.NO_CONTENT);
	}

	@Test
	public void testChangePasswordShouldReturnBadRequestWhenOldPasswordDoesNotMatch() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		newPasswords.setOldPassword("aaZZ44b##@");
		MvcResult mvcResult = sendChangePasswordRequest(newPasswords, params, accessToken, HttpStatus.BAD_REQUEST);
		
		Map<String, Object> jsonMap =  getJsonMap(mvcResult);
		String errorMessage = (String) jsonMap.get("message");
		assertEquals(errorMessage, ValidationErrorMessages.OLD_PASSWORD_DOES_NOT_MATCH);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testChangePasswordShouldReturnBadRequestWhenOldPasswordIsInvalid() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		newPasswords.setOldPassword("invalid");
		int expectedErrorMessagesLength = 1;
		MvcResult mvcResult = sendChangePasswordRequest(newPasswords, params, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(mvcResult);		
		assertEquals(errorMessages.size(), expectedErrorMessagesLength);
		assertTrue(errorMessages.get("oldPassword").contains(ValidationErrorMessages.INVALID_OLD_PASSWORD_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testChangePasswordShouldReturnBadRequestWhenNewPasswordIsInvalid() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		newPasswords.getPasswords().setPassword("invalid");
		MvcResult mvcResult = sendChangePasswordRequest(newPasswords, params, accessToken, HttpStatus.BAD_REQUEST);
		int expectedErrorMessagesLength = 2;
		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(mvcResult);		
		
		assertEquals(errorMessages.size(), expectedErrorMessagesLength);
		assertTrue(errorMessages.get("password").contains(ValidationErrorMessages.INVALID_PASSWORD_MESSAGE));
		assertTrue(errorMessages.get("passwords").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testChangePasswordShouldReturnBadRequestWhenNewPasswordConfirmationIsInvalid() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		newPasswords.getPasswords().setPasswordConfirmation("invalid");
		MvcResult mvcResult = sendChangePasswordRequest(newPasswords, params, accessToken, HttpStatus.BAD_REQUEST);
		int expectedErrorMessagesLength = 2;

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(mvcResult);
		
		assertEquals(errorMessages.size(), expectedErrorMessagesLength);
		assertTrue(errorMessages.get("passwordConfirmation").contains(ValidationErrorMessages.INVALID_PASSWORD_CONFIRMATION_MESSAGE));
		assertTrue(errorMessages.get("passwords").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testChangePasswordShouldReturnBadRequestWhenNewPasswordsAreNull() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		newPasswords.getPasswords().setPasswordConfirmation("invalid");
		MvcResult mvcResult = sendChangePasswordRequest(new ChangePasswordRequestModel(), params, accessToken, HttpStatus.BAD_REQUEST);
		int expectedErrorMessagesLength = 2;

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(mvcResult);
		
		assertEquals(errorMessages.size(), expectedErrorMessagesLength);
		assertTrue(errorMessages.get("passwords").contains(ValidationErrorMessages.PASSWORDS_NOT_EMPTY_MESSAGE));
		assertTrue(errorMessages.get("oldPassword").contains(ValidationErrorMessages.OLD_PASSWORD_NOT_EMPTY_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testSignOutShouldReturnForbiddenWhenAccessTokenIsMissing() throws Exception {
		signOut("", HttpStatus.UNAUTHORIZED);
	}

	private MvcResult sendChangePasswordRequest(ChangePasswordRequestModel passwordsRequest, OAuthClientParams clientParams, String accessToken, HttpStatus expectedHttpStatus) throws Exception {
		String credentials = objectMapper.writeValueAsString(passwordsRequest);
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		return mvc.perform(put("/api/users/change_password")
				.with(httpBasic(clientParams.getClientId(), clientParams.getClientDetailsPassword()))
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.params(paramsMap)
				.content(credentials))
		.andExpect(status().is(expectedHttpStatus.value()))
		.andReturn();
	}
}
