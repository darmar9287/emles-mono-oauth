package com.emles.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.emles.model.AccountActivationToken;
import com.emles.model.AppUser;
import com.emles.model.Authority;
import com.emles.model.AuthorityName;
import com.emles.model.Role;
import com.emles.model.RoleName;
import com.emles.model.request.UserDataRequestModel;
import com.emles.model.request.UserLoginRequestModel;
import com.emles.model.request.UserRegistrationRequestModel;
import com.emles.model.request.admin.PasswordRequestModel;
import com.emles.repository.AccountActivationTokenRepository;
import com.emles.repository.AuthorityRepository;
import com.emles.repository.RoleRepository;
import com.emles.repository.UserRepository;
import com.emles.utils.OAuthClientParams;
import com.emles.utils.ValidationErrorMessages;
import com.emles.EmlesMonoOauthApplication;


@ExtendWith(SpringExtension.class)
@SpringBootTest(
	webEnvironment = WebEnvironment.DEFINED_PORT,
	classes = EmlesMonoOauthApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class UserRegistrationIntegrationTest extends BaseIntegrationTest {

	private UserRepository userRepository;

	private AuthorityRepository authorityRepository;

	private RoleRepository roleRepository;

	private AccountActivationTokenRepository accountActivationTokenRepository;
	
	private AppUser standardUser;
	
	private UserRegistrationRequestModel userRegistration;
	
	private UserDataRequestModel userDataRequest;
	
	private UserLoginRequestModel loginModel = new UserLoginRequestModel();
	
	private ClientDetails clientDetails;
	
	private String clientId = "oauth_client_id";

	private OAuthClientParams params;
	
	private JdbcClientDetailsService clientDetailsService;
	
	@Autowired
	public UserRegistrationIntegrationTest(UserRepository userRepository, AuthorityRepository authorityRepository,
			RoleRepository roleRepository, AccountActivationTokenRepository accountActivationTokenRepository, JdbcClientDetailsService clientDetailsService) {
		this.userRepository = userRepository;
		this.authorityRepository = authorityRepository;
		this.roleRepository = roleRepository;
		this.accountActivationTokenRepository = accountActivationTokenRepository;
		this.clientDetailsService = clientDetailsService;
	}

	@BeforeEach
	public void setUp() {
		Authority authority = createAuthority(AuthorityName.READ_AUTHORITY);
		Role role = createRole(RoleName.ROLE_USER);
		
		roleRepository.save(role);
		authorityRepository.save(authority);
		
		standardUser = createEnabledUser("test@test.com", Arrays.asList(role), Arrays.asList(authority));
		userRepository.save(standardUser);
		
		userDataRequest = new UserDataRequestModel();
		userDataRequest.setEmail("new_user@test.com");

		userRegistration = new UserRegistrationRequestModel();
		userRegistration.setUserData(userDataRequest);
		
		PasswordRequestModel passwords = new PasswordRequestModel(hashedPassword, hashedPassword);
		userRegistration.setPasswords(passwords);
		
		loginModel.setEmail(standardUser.getEmail());
		loginModel.setPassword(nonHashedPassword);
		
		int accessTokenValidity = 30;
		int refreshTokenValidity = 60;
		clientDetails = createBaseClientDetails(clientId , hashedPassword, standardUser.getAllAuthorities(), accessTokenValidity , refreshTokenValidity);
		clientDetailsService.addClientDetails(clientDetails);
		
		params = new OAuthClientParams();
		params.setClientDetailsPassword(nonHashedPassword);
		params.setClientId(clientId);
		params.setGrantType("password");
		params.setPassword(nonHashedPassword);
		params.setUsername(standardUser.getEmail());
	}
	
	@AfterEach
	public void tearDown() {
		accountActivationTokenRepository.deleteAll();
		roleRepository.deleteAll();
		authorityRepository.deleteAll();
		userRepository.deleteAll();
		clientDetailsService.listClientDetails().forEach(client -> {
			clientDetailsService.removeClientDetails(client.getClientId());
		});
	}

	@Test
	public void testSignUpUserShouldSucceedWhenAllDataIsCorrect() throws Exception {
		MvcResult result = signUp(userRegistration, params, HttpStatus.OK);
		Map<String, Object> responseMap = getJsonMap(result);
		Map<?, ?> userDataResponse = (Map<?, ?>)responseMap.get("userData");
		assertTrue(responseMap.containsKey("userId"));
		assertEquals(userDataResponse.get("email").toString(), userRegistration.getUserData().getEmail());
		
		AppUser found = userRepository.findAppUserByUserDataEmail(userRegistration.getUserData().getEmail());
		assertNotNull(found);
		List<String> authorities = found.getAuthorities().stream().map(a -> a.getName().name()).collect(Collectors.toList());
		List<String> roles = found.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList());
		AccountActivationToken token = accountActivationTokenRepository.findByUser(found);
		
		int expectedAuthoritiesSize = 1;
		int expectedRolesSize = 1;
		assertNotNull(token);
		assertEquals(authorities.size(), expectedAuthoritiesSize);
		assertEquals(roles.size(), expectedRolesSize);
		assertTrue(authorities.contains(AuthorityName.READ_AUTHORITY.name()));
		assertTrue(roles.contains(RoleName.ROLE_USER.name()));
	}
	
	@Test
	public void testSignUpWithInvalidEmailAddressShouldReturnBadRequest() throws Exception {
		userRegistration.getUserData().setEmail("invalid");
		
		MvcResult result = signUp(userRegistration, params, HttpStatus.BAD_REQUEST);
		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorsSize = 1;
		assertEquals(errorMessages.size(), expectedErrorsSize);
		assertTrue(errorMessages.get("email").contains(ValidationErrorMessages.EMAIL_INVALID_MESSAGE));
	}
	
	@Test
	public void testSignUpWithInvalidPasswordShouldReturnBadRequest() throws Exception {
		userRegistration.getPasswords().setPassword("invalid");
		
		MvcResult result = signUp(userRegistration, params, HttpStatus.BAD_REQUEST);
		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		
		int expectedErrorsSize = 2;
		assertEquals(errorMessages.size(), expectedErrorsSize);
		assertTrue(errorMessages.get("password").contains(ValidationErrorMessages.INVALID_PASSWORD_MESSAGE));
		assertTrue(errorMessages.get("passwords").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
	}
	
	@Test
	public void testSignUpWithInvalidPasswordConfirmationShouldReturnBadRequest() throws Exception {
		userRegistration.getPasswords().setPasswordConfirmation("invalid");
		
		MvcResult result = signUp(userRegistration, params, HttpStatus.BAD_REQUEST);
		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		
		int expectedErrorsSize = 2;
		assertEquals(errorMessages.size(), expectedErrorsSize);
		assertTrue(errorMessages.get("passwordConfirmation").contains(ValidationErrorMessages.INVALID_PASSWORD_CONFIRMATION_MESSAGE));
		assertTrue(errorMessages.get("passwords").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
	}
	
	@Test
	public void testSignUpWithNullDataShouldReturnBadRequest() throws Exception {
		userRegistration = new UserRegistrationRequestModel();
		
		MvcResult result = signUp(userRegistration, params, HttpStatus.BAD_REQUEST);
		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorsSize = 2;
		
		assertEquals(errorMessages.size(), expectedErrorsSize);
		assertTrue(errorMessages.get("userData").contains(ValidationErrorMessages.USER_DATA_ATTRIBUTES_NOT_NULL_MESSAGE));
		assertTrue(errorMessages.get("passwords").contains(ValidationErrorMessages.PASSWORDS_NOT_EMPTY_MESSAGE));
	}
	
	@Test
	public void testSignUpWithExistingEmailAddressShouldReturnBadRequest() throws Exception {
		signUp(userRegistration, params, HttpStatus.OK);
		MvcResult result = signUp(userRegistration, params, HttpStatus.BAD_REQUEST);
		
		Map<String, Object> responseMap = jsonParser.parseMap(result.getResponse().getContentAsString());
		String errorMessage = (String)responseMap.get("message");
		assertEquals(errorMessage, ValidationErrorMessages.USERNAME_EXISTS_MESSAGE);
	}
	
	@Test
	public void testActivateUserAccountShouldSucceedWhenAllDataIsCorrect() throws Exception {
		signUp(userRegistration, params, HttpStatus.OK);
		AppUser found = userRepository.findAppUserByUserDataEmail(userRegistration.getUserData().getEmail());
		AccountActivationToken token = accountActivationTokenRepository.findByUser(found);

		params.setUsername(userRegistration.getUserData().getEmail());
		params.setPassword(userRegistration.getPasswords().getPassword());
		signIn(params, HttpStatus.UNAUTHORIZED);
		
		sendActivateAccountRequest(found.getId(), token.getToken(), params, HttpStatus.OK);
		found = userRepository.findAppUserByUserDataEmail(userRegistration.getUserData().getEmail());
		assertTrue(found.getEnabled());
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.OK);
		String accessToken = oauthTokenResponse.get(accessTokenKey).toString();
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testActivateUserAccountShouldReturnNotFoundWhenUserIdIsInvalid() throws Exception {
		signUp(userRegistration, params, HttpStatus.OK);
		AppUser found = userRepository.findAppUserByUserDataEmail(userRegistration.getUserData().getEmail());
		AccountActivationToken token = accountActivationTokenRepository.findByUser(found);
		long invalidUserId = -10L;
		sendActivateAccountRequest(invalidUserId, token.getToken(), params, HttpStatus.NOT_FOUND);
		found = userRepository.findAppUserByUserDataEmail(userRegistration.getUserData().getEmail());
		assertFalse(found.getEnabled());
	}
	
	@Test
	public void testActivateUserAccountShouldReturnNotFoundWhenTokenIsInvalid() throws Exception {
		signUp(userRegistration, params, HttpStatus.OK);
		AppUser found = userRepository.findAppUserByUserDataEmail(userRegistration.getUserData().getEmail());
		
		sendActivateAccountRequest(found.getId(), "invalid", params, HttpStatus.NOT_FOUND);
		found = userRepository.findAppUserByUserDataEmail(userRegistration.getUserData().getEmail());
		assertFalse(found.getEnabled());
	}
	
	@Test
	public void testUpdateAccountDataShouldFailWhenUserIsNotSignedIn() throws Exception {
		updateAccountData(userDataRequest, params, "", HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	public void testUpdateAccountDataShouldFailWhenEmailIsInvalid() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		userDataRequest.setEmail("invalid");
		MvcResult result = updateAccountData(userDataRequest, params, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		assertTrue(errorMessages.get("email").contains(ValidationErrorMessages.EMAIL_INVALID_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateAccountDataShouldFailWhenEmailIsNull() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		userDataRequest.setEmail(null);
		MvcResult result = updateAccountData(userDataRequest, params, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		assertTrue(errorMessages.get("email").contains(ValidationErrorMessages.EMAIL_NOT_BLANK_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateAccountDataShouldFailWhenEmailIsTaken() throws Exception {
		signUp(userRegistration, params, HttpStatus.OK);
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		userDataRequest.setEmail(userRegistration.getUserData().getEmail());
		MvcResult result = updateAccountData(userDataRequest, params, accessToken, HttpStatus.BAD_REQUEST);
		Map<String, Object> responseMap = jsonParser.parseMap(result.getResponse().getContentAsString());
		String errorMessage = (String)responseMap.get("message");
		assertEquals(errorMessage, ValidationErrorMessages.USERNAME_EXISTS_MESSAGE);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateAccountDataShouldSucceedWhenAllDataIsCorrect() throws Exception {
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.OK);
		String accessToken = oauthTokenResponse.get(accessTokenKey).toString();
		String refreshToken = oauthTokenResponse.get(refreshTokenKey).toString();
		String newEmailAddress = "brand_new_changed@email.com";
		userDataRequest.setEmail(newEmailAddress);
		
		MvcResult result = updateAccountData(userDataRequest, params, accessToken, HttpStatus.OK);
		
		oauthTokenResponse = jsonParser.parseMap(result.getResponse().getContentAsString());
		
		String newAccessToken = oauthTokenResponse.get(accessTokenKey).toString();
		String newRefreshToken = oauthTokenResponse.get(refreshTokenKey).toString();
		
		assertNotEquals(accessToken, newAccessToken);
		assertNotEquals(refreshToken, newRefreshToken);

		signOut(accessToken, HttpStatus.UNAUTHORIZED);
		params.setRefreshToken(refreshToken);
		refreshTokenRequest(params, HttpStatus.BAD_REQUEST);
		aboutMeRequest(newAccessToken, params, HttpStatus.OK);
		params.setRefreshToken(newRefreshToken);
		accessToken = getAccessToken(refreshTokenRequest(params, HttpStatus.OK));
		signOut(accessToken, HttpStatus.NO_CONTENT);
		
		params.setRefreshToken(null);
		signIn(params, HttpStatus.UNAUTHORIZED);
		params.setUsername(newEmailAddress);
		accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		
		long standardUserId = standardUser.getId();
		standardUser = userRepository.findAppUserByUserDataEmail(newEmailAddress);
		assertEquals(standardUser.getId(), standardUserId);
	}
	
	@Test
	public void testDeleteAccount() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		long userId = standardUser.getId();
		deleteAccountRequest(accessToken, params, HttpStatus.NO_CONTENT);
		signOut(accessToken, HttpStatus.UNAUTHORIZED);
		assertFalse(userRepository.findById(userId).isPresent());
	}
	
	private MvcResult updateAccountData(UserDataRequestModel body, OAuthClientParams params, String accessToken, HttpStatus expectedHttpStatus) throws Exception {
		String requestBodyString = objectMapper.writeValueAsString(body);
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		return mvc.perform(put("/api/users/update")
				.header("Authorization", "Bearer " + accessToken)
				.with(httpBasic(params.getClientId(), params.getClientDetailsPassword()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.params(paramsMap)
				.content(requestBodyString))
		.andExpect(status().is(expectedHttpStatus.value()))
		.andReturn();
	}
	
	private void sendActivateAccountRequest(Long id, String token, OAuthClientParams params, HttpStatus expectedHttpStatus) throws Exception {
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		mvc.perform(get("/api/users/activate_account?userId=" + id + "&token=" + token)
				.with(httpBasic(params.getClientId(), params.getClientDetailsPassword()))
				.contentType(MediaType.APPLICATION_JSON)
				.params(paramsMap)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().is(expectedHttpStatus.value()));
	}

	private void deleteAccountRequest(String token, OAuthClientParams params, HttpStatus expectedHttpStatus) throws Exception {
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		mvc.perform(delete("/api/users/delete")
				.with(httpBasic(params.getClientId(), params.getClientDetailsPassword()))
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.params(paramsMap)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().is(expectedHttpStatus.value()));
	}
	
	private MvcResult signUp(UserRegistrationRequestModel body, OAuthClientParams params, HttpStatus expectedHttpStatus) throws Exception {
		String requestBodyString = objectMapper.writeValueAsString(body);
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		return mvc.perform(post("/api/users/sign_up")
				.with(httpBasic(params.getClientId(), params.getClientDetailsPassword()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.params(paramsMap)
				.content(requestBodyString))
		.andExpect(status().is(expectedHttpStatus.value()))
		.andReturn();
	}
}
