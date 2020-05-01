package com.emles.integration.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.emles.EmlesMonoOauthApplication;
import com.emles.integration.BaseIntegrationTest;
import com.emles.model.AppUser;
import com.emles.model.Authority;
import com.emles.model.AuthorityName;
import com.emles.model.Role;
import com.emles.model.RoleName;
import com.emles.model.embedded.UserData;
import com.emles.model.request.UserDataRequestModel;
import com.emles.model.request.UserLoginRequestModel;
import com.emles.model.request.admin.PasswordRequestModel;
import com.emles.model.request.admin.UserDataExtendedModel;
import com.emles.model.request.admin.UserModelRequest;
import com.emles.repository.AuthorityRepository;
import com.emles.repository.RoleRepository;
import com.emles.repository.UserRepository;
import com.emles.utils.OAuthClientParams;
import com.emles.utils.ValidationErrorMessages;
import com.github.javafaker.Faker;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = EmlesMonoOauthApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class UserIntegrationTest extends BaseIntegrationTest {

	private UserRepository userRepository;

	private AuthorityRepository authorityRepository;

	private RoleRepository roleRepository;

	private AppUser adminUser;
	
	private AppUser standardUser;

	private UserLoginRequestModel credentials = new UserLoginRequestModel();
	
	private UserModelRequest userRequest= new UserModelRequest();
	
	@Value("${config.pagination.default_page_offset}")
	private int PER_PAGE;
	
	private Faker faker = new Faker();

	private UserDataExtendedModel userDataRequest = new UserDataExtendedModel();
	
	private PasswordRequestModel passwordRequest = new PasswordRequestModel();
	
	private ClientDetails clientDetails;
	
	private String clientId = "oauth_client_id";

	private OAuthClientParams params;
	
	private JdbcClientDetailsService clientDetailsService;
	
	@Autowired
	public UserIntegrationTest(UserRepository userRepository, AuthorityRepository authorityRepository,
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
		
		adminUser = createEnabledUser("admin@test.com", Arrays.asList(role, admin), Arrays.asList(authority, createAuthority));		
		adminUser.setLastPasswordResetDate(Date.from(Instant.now()));
		userRepository.save(adminUser);
		
		credentials.setEmail(adminUser.getEmail());
		credentials.setPassword(nonHashedPassword);

		UserDataRequestModel userData = new UserDataRequestModel();
		userData.setEmail("test1@test.com");

		userRequest.setUserData(userData);
		PasswordRequestModel passwords = new PasswordRequestModel(nonHashedPassword, nonHashedPassword);
		userRequest.setPasswords(passwords);
		userRequest.setEnabled(true);
		userRequest.setAuthorityIds(Arrays.asList(authority, createAuthority).stream().map(Authority::getId).collect(Collectors.toSet()));
		userRequest.setRoleIds(Arrays.asList(role, admin).stream().map(Role::getId).collect(Collectors.toSet()));

		userDataRequest.setAuthorityIds(userRequest.getAuthorityIds());
		userDataRequest.setRoleIds(userRequest.getRoleIds());
		userDataRequest.setEmail(userRequest.getUserData().getEmail());
		userDataRequest.setEnabled(true);

		passwordRequest.setPassword(newNonHashedPassword);
		passwordRequest.setPasswordConfirmation(newNonHashedPassword);
		
		int accessTokenValidity = 30;
		int refreshTokenValidity = 60;
		clientDetails = createBaseClientDetails(clientId , hashedPassword, standardUser.getAllAuthorities(), accessTokenValidity , refreshTokenValidity);
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
		authorityRepository.deleteAll();
		roleRepository.deleteAll();
		clientDetailsService.listClientDetails().forEach(client -> {
			clientDetailsService.removeClientDetails(client.getClientId());
		});
	}
	
	@Test
	public void testCreateNewUserShouldSucceed() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		MvcResult result = createAccount(userRequest, params, accessToken, HttpStatus.OK);
		Map<String, Object> responseMap = getJsonMap(result);
		checkPresenceOfFieldsInResponse(responseMap);
		AppUser found = userRepository.findById(Long.valueOf(responseMap.get("userId").toString())).get();
		compareResponseDataToAppUser(responseMap, found);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testCreateNewUserWithExistingEmailShouldFail() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		UserDataRequestModel userDataReq = new UserDataRequestModel();
		userDataReq.setEmail(standardUser.getEmail());
		userRequest.setUserData(userDataReq);
		MvcResult result = createAccount(userRequest, params, accessToken, HttpStatus.BAD_REQUEST);
		Map<String, Object> responseMap = getJsonMap(result);

		assertEquals(ValidationErrorMessages.USERNAME_EXISTS_MESSAGE, responseMap.get("message"));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testCreateNewUserWithInvalidEmailShouldFail() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		UserDataRequestModel userDataReq = new UserDataRequestModel();
		userDataReq.setEmail("invalid");
		userRequest.setUserData(userDataReq);
		MvcResult result = createAccount(userRequest, params, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		assertTrue(errorMessages.get("email").contains(ValidationErrorMessages.EMAIL_INVALID_MESSAGE));
		int expectedErrorsSize = 1;
		assertEquals(errorMessages.size(), expectedErrorsSize);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testCreateNewUserWithNullEmailShouldFail() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		UserDataRequestModel userDataReq = new UserDataRequestModel();
		userDataReq.setEmail(null);
		userRequest.setUserData(userDataReq);
		MvcResult result = createAccount(userRequest, params, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorsSize = 1;
		assertTrue(errorMessages.get("email").contains(ValidationErrorMessages.EMAIL_NOT_BLANK_MESSAGE));
		assertEquals(errorMessages.size(), expectedErrorsSize);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testCreateNewUserWithNullUserDataShouldFail() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		userRequest.setUserData(null);
		MvcResult result = createAccount(userRequest, params, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorsSize = 1;
		assertTrue(errorMessages.get("userData").contains(ValidationErrorMessages.USER_DATA_ATTRIBUTES_NOT_NULL_MESSAGE));
		assertEquals(errorMessages.size(), expectedErrorsSize);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testCreateNewUserWithInvalidPasswordShouldFail() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		userRequest.getPasswords().setPassword("invalid");
		MvcResult result = createAccount(userRequest, params, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorsSize = 2;
		assertTrue(errorMessages.get("password").contains(ValidationErrorMessages.INVALID_PASSWORD_MESSAGE));
		assertTrue(errorMessages.get("passwords").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
		assertEquals(errorMessages.size(), expectedErrorsSize);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testCreateNewUserWithNullPasswordShouldFail() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		userRequest.getPasswords().setPassword(null);
		MvcResult result = createAccount(userRequest, params, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorsSize = 2;
		assertTrue(errorMessages.get("password").contains(ValidationErrorMessages.PASSWORD_NOT_EMPTY_MESSAGE));
		assertTrue(errorMessages.get("passwords").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
		assertEquals(errorMessages.size(), expectedErrorsSize);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}

	@Test
	public void testCreateNewUserWithInvalidPasswordConrimationShouldFail() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		userRequest.getPasswords().setPasswordConfirmation("invalid");
		MvcResult result = createAccount(userRequest, params, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorsSize = 2;
		assertTrue(errorMessages.get("passwordConfirmation").contains(ValidationErrorMessages.INVALID_PASSWORD_CONFIRMATION_MESSAGE));
		assertTrue(errorMessages.get("passwords").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
		assertEquals(errorMessages.size(), expectedErrorsSize);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testCreateNewUserWithNullPasswordConrimationShouldFail() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		userRequest.getPasswords().setPasswordConfirmation(null);
		MvcResult result = createAccount(userRequest, params, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorsSize = 2;
		assertTrue(errorMessages.get("passwordConfirmation").contains(ValidationErrorMessages.PASSWORD_CONFIRMATION_NOT_EMPTY_MESSAGE));
		assertTrue(errorMessages.get("passwords").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
		assertEquals(errorMessages.size(), expectedErrorsSize);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}

	@Test
	public void testCreateNewUserWithNullEnabledFieldShouldFail() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		userRequest.setEnabled(null);
		MvcResult result = createAccount(userRequest, params, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorsSize = 1;
		assertTrue(errorMessages.get("enabled").contains(ValidationErrorMessages.USER_ENABLED_NOT_EMPTY_MESSAGE));
		assertEquals(errorMessages.size(), expectedErrorsSize);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}

	@Test
	public void testCreateNewUserWithEmptyAuthoritiesAndRolesShouldSucceed() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		userRequest.setAuthorityIds(Collections.emptySet());
		userRequest.setRoleIds(Collections.emptySet());
		MvcResult result = createAccount(userRequest, params, accessToken, HttpStatus.OK);
		Map<String, Object> responseMap = jsonParser.parseMap(result.getResponse().getContentAsString());
		AppUser found = userRepository.findById(Long.valueOf(responseMap.get("userId").toString())).get();
		assertTrue(found.getAuthorities().isEmpty());
		assertTrue(found.getRoles().isEmpty());
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testPaginationOfUsersWithoutSearchParams() throws Exception {
		int usersNum = 20;
		int perPageRequestParam = 7;
		List<AppUser> users = createAppUsers(usersNum, Optional.empty());
		users.add(0, adminUser);
		users.add(0, standardUser);
		Optional<String> emptySearchParams = Optional.empty();
		testPaginationOfUsers(emptySearchParams, params, emptySearchParams, Optional.empty(), users);
		testPaginationOfUsers(emptySearchParams, params, emptySearchParams, Optional.of(perPageRequestParam), users);
		testPaginationOfUsers(Optional.of(""), params, Optional.of(""), Optional.empty(), users);
	}
	
	@Test
	public void testPaginationOfUsersWithSearchByEmail() throws Exception {
		int usersNum = 10;
		String testDomainName = "testing.com";
		String otherDomainName = "other.com";
		String emailQueryParam = "email";
		List<AppUser> usersFromTestDomain = createAppUsers(usersNum, Optional.of(testDomainName));
		
		createAppUsers(usersNum, Optional.of(otherDomainName));
		
		testPaginationOfUsers(Optional.of(testDomainName), params, Optional.of(emailQueryParam), Optional.empty(), usersFromTestDomain);
		testPaginationOfUsers(Optional.of(testDomainName), params, Optional.empty(), Optional.empty(), usersFromTestDomain);
		testPaginationOfUsers(Optional.of(testDomainName), params, Optional.of(""), Optional.of(6), usersFromTestDomain);
	}
	
	@Test
	public void testFetchExistingUserById() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		MvcResult result = fetchUser(standardUser.getId(), accessToken, params, HttpStatus.OK);
		Map<String, Object> responseMap = jsonParser.parseMap(result.getResponse().getContentAsString());
		compareResponseDataToAppUser(responseMap, standardUser);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testFetchNonExistingUserById() throws Exception {
		long invalidUserId = -1L;
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		MvcResult result = fetchUser(invalidUserId, accessToken, params, HttpStatus.NOT_FOUND);
		Map<String, Object> responseMap = jsonParser.parseMap(result.getResponse().getContentAsString());
		assertEquals(responseMap.get("message"), ValidationErrorMessages.USER_NOT_FOUND_MESSAGE);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateUserShouldSucceedWhenAllDataIsCorrect() throws Exception {
		params.setUsername(adminUser.getEmail());
		String adminAccessToken = getAccessToken(signIn(params, HttpStatus.OK));
		
		params.setUsername(standardUser.getEmail());
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.OK);
		String userAccessToken = oauthTokenResponse.get(accessTokenKey).toString();
		String userRefreshToken = oauthTokenResponse.get(refreshTokenKey).toString();

		long userAuthoritiesSize = standardUser.getAuthorities().size();
		long userRolesSize = standardUser.getRoles().size();
		long standardUserId = standardUser.getId();
		MvcResult result = updateAccount(userDataRequest, params, standardUserId, adminAccessToken, HttpStatus.OK);
		Map<String, Object> responseMap = jsonParser.parseMap(result.getResponse().getContentAsString());
		checkPresenceOfFieldsInResponse(responseMap);

		AppUser found = userRepository.findById(Long.valueOf(responseMap.get("userId").toString())).get();
		assertTrue(found.getAuthorities().size() > userAuthoritiesSize);
		assertTrue(found.getRoles().size() > userRolesSize);
		compareResponseDataToAppUser(responseMap, found);
		signOut(userAccessToken, HttpStatus.UNAUTHORIZED);
		params.setRefreshToken(userRefreshToken);
		refreshTokenRequest(params, HttpStatus.BAD_REQUEST);
		signIn(params, HttpStatus.UNAUTHORIZED);
		params.setUsername(found.getEmail());
		signIn(params, HttpStatus.OK);

		userDataRequest.setEnabled(false);
		result = updateAccount(userDataRequest, params, standardUserId, adminAccessToken, HttpStatus.OK);
		signIn(params, HttpStatus.UNAUTHORIZED);
		
		Long firstAuthorityId = found.getAuthorities().iterator().next().getId();
		userDataRequest.setAuthorityIds(new HashSet<>(Arrays.asList(firstAuthorityId)));
		userDataRequest.setRoleIds(Collections.emptySet());
		
		result = updateAccount(userDataRequest, params, standardUserId, adminAccessToken, HttpStatus.OK);
		found = userRepository.findById(Long.valueOf(responseMap.get("userId").toString())).get();
		int expectedAuthoritiesSize = 1;
		assertEquals(found.getAuthorities().size(), expectedAuthoritiesSize);
		assertTrue(found.getRoles().isEmpty());
		signOut(adminAccessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateUserShouldFailWhenEmailExists() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		long userId = standardUser.getId();
		userDataRequest.setEmail(adminUser.getEmail());
		MvcResult result = updateAccount(userDataRequest, params, userId, accessToken, HttpStatus.BAD_REQUEST);
		Map<String, Object> responseMap = jsonParser.parseMap(result.getResponse().getContentAsString());
		assertEquals(responseMap.get("message"), ValidationErrorMessages.USERNAME_EXISTS_MESSAGE);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateUserShouldFailWhenUserIdDoesNotExist() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		long invalidUserId = -1;
		MvcResult result = updateAccount(userDataRequest, params, invalidUserId, accessToken, HttpStatus.NOT_FOUND);
		Map<String, Object> responseMap = jsonParser.parseMap(result.getResponse().getContentAsString());
		assertEquals(responseMap.get("message"), ValidationErrorMessages.USER_NOT_FOUND_MESSAGE);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateUserShouldFailWhenEnabledIsNull() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		long userId = standardUser.getId();
		userDataRequest.setEnabled(null);
		MvcResult result = updateAccount(userDataRequest, params, userId, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorMessagesSize = 1;
		assertEquals(errorMessages.size(), expectedErrorMessagesSize);
		assertTrue(errorMessages.get("enabled").contains(ValidationErrorMessages.USER_ENABLED_NOT_EMPTY_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateUserShouldFailWhenAuthorityIdsIsNull() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		long userId = standardUser.getId();
		userDataRequest.setAuthorityIds(null);
		MvcResult result = updateAccount(userDataRequest, params, userId, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorMessagesSize = 1;
		assertEquals(errorMessages.size(), expectedErrorMessagesSize);
		assertTrue(errorMessages.get("authorityIds").contains(ValidationErrorMessages.AUTHORITY_IDS_NOT_NULL_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateUserShouldFailWhenRoleIdsIsNull() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		long userId = standardUser.getId();
		userDataRequest.setRoleIds(null);
		MvcResult result = updateAccount(userDataRequest, params, userId, accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorMessagesSize = 1;
		assertEquals(errorMessages.size(), expectedErrorMessagesSize);
		assertTrue(errorMessages.get("roleIds").contains(ValidationErrorMessages.ROLE_IDS_NOT_NULL_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testSignOutUserShouldSucceedWhenUserExists() throws Exception {
		params.setUsername(adminUser.getEmail());
		String adminAccessToken = getAccessToken(signIn(params, HttpStatus.OK));
		params.setUsername(standardUser.getEmail());
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.OK);
		String userAccessToken = oauthTokenResponse.get(accessTokenKey).toString();
		String userRefreshToken = oauthTokenResponse.get(refreshTokenKey).toString();
		
		signUserOut(params, standardUser.getId(), adminAccessToken, HttpStatus.NO_CONTENT);
		
		signOut(userAccessToken, HttpStatus.UNAUTHORIZED);
		params.setRefreshToken(userRefreshToken);
		refreshTokenRequest(params, HttpStatus.BAD_REQUEST);
		signOut(adminAccessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testSignOutUserShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
		params.setUsername(adminUser.getEmail());
		String adminAccessToken = getAccessToken(signIn(params, HttpStatus.OK));
		params.setUsername(standardUser.getEmail());
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.OK);
		String userAccessToken = oauthTokenResponse.get(accessTokenKey).toString();
		
		Long userId = -1L;
		signUserOut(params, userId , adminAccessToken, HttpStatus.NOT_FOUND);
		
		signOut(userAccessToken, HttpStatus.NO_CONTENT);
		signOut(adminAccessToken, HttpStatus.NO_CONTENT);
	}

	@Test
	public void testUpdateUserPasswordShouldSucceedWhenAllDataIsCorrect() throws Exception {
		params.setUsername(adminUser.getEmail());
		String adminAccessToken = getAccessToken(signIn(params, HttpStatus.OK));
		params.setUsername(standardUser.getEmail());
		Map<String, Object> oauthTokenResponse = signIn(params, HttpStatus.OK);
		String userAccessToken = oauthTokenResponse.get(accessTokenKey).toString();
		String userRefreshToken = oauthTokenResponse.get(refreshTokenKey).toString();
		
		MvcResult result = updatePassword(passwordRequest, params, standardUser.getId(), adminAccessToken, HttpStatus.OK);
		Map<String, Object> responseMap = jsonParser.parseMap(result.getResponse().getContentAsString());
		compareResponseDataToAppUser(responseMap, standardUser);
		
		signOut(userAccessToken, HttpStatus.UNAUTHORIZED);
		params.setRefreshToken(userRefreshToken);
		refreshTokenRequest(params, HttpStatus.BAD_REQUEST);
		signIn(params, HttpStatus.UNAUTHORIZED);
		params.setPassword(newNonHashedPassword);
		oauthTokenResponse = signIn(params, HttpStatus.OK);
		userAccessToken = oauthTokenResponse.get(accessTokenKey).toString();
		signOut(userAccessToken, HttpStatus.NO_CONTENT);
		signOut(adminAccessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateUserPasswordShouldFailWhenPasswordIsIncorrect() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		passwordRequest.setPassword("invalid");
		MvcResult result = updatePassword(passwordRequest, params, standardUser.getId(), accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorMessagesSize = 2;
		assertEquals(errorMessages.size(), expectedErrorMessagesSize);
		assertTrue(errorMessages.get("other").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
		assertTrue(errorMessages.get("password").contains(ValidationErrorMessages.INVALID_PASSWORD_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateUserPasswordShouldFailWhenPasswordIsNull() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		passwordRequest.setPassword(null);
		MvcResult result = updatePassword(passwordRequest, params, standardUser.getId(), accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorMessagesSize = 2;
		assertEquals(errorMessages.size(), expectedErrorMessagesSize);
		assertTrue(errorMessages.get("other").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
		assertTrue(errorMessages.get("password").contains(ValidationErrorMessages.PASSWORD_NOT_EMPTY_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateUserPasswordShouldFailWhenPasswordConfirmationIsIncorrect() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		passwordRequest.setPasswordConfirmation("invalid");
		MvcResult result = updatePassword(passwordRequest, params, standardUser.getId(), accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorMessagesSize = 2;
		assertEquals(errorMessages.size(), expectedErrorMessagesSize);
		assertTrue(errorMessages.get("other").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
		assertTrue(errorMessages.get("passwordConfirmation").contains(ValidationErrorMessages.INVALID_PASSWORD_CONFIRMATION_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}

	@Test
	public void testUpdateUserPasswordShouldFailWhenPasswordConfirmationIsNull() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		passwordRequest.setPasswordConfirmation(null);
		MvcResult result = updatePassword(passwordRequest, params, standardUser.getId(), accessToken, HttpStatus.BAD_REQUEST);

		Map<String, List<String>> errorMessages = extractValidationErrorMapFromResponse(result);
		int expectedErrorMessagesSize = 2;
		assertEquals(errorMessages.size(), expectedErrorMessagesSize);
		assertTrue(errorMessages.get("other").contains(ValidationErrorMessages.PASSWORDS_NOT_EQUAL_MESSAGE));
		assertTrue(errorMessages.get("passwordConfirmation").contains(ValidationErrorMessages.PASSWORD_CONFIRMATION_NOT_EMPTY_MESSAGE));
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testUpdateUserPasswordShouldFailWhenUserIdDoesNotExist() throws Exception {
		long invalidUserId = -1L;
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		MvcResult result = updatePassword(passwordRequest, params, invalidUserId, accessToken, HttpStatus.NOT_FOUND);
		Map<String, Object> responseMap = jsonParser.parseMap(result.getResponse().getContentAsString());
		assertEquals(responseMap.get("message"), ValidationErrorMessages.USER_NOT_FOUND_MESSAGE);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testDeleteUserShouldSucceedWhenUserExists() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		long userId = standardUser.getId();
		deleteUser(userId, accessToken, params, HttpStatus.NO_CONTENT);
		assertFalse(userRepository.findById(userId).isPresent());
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testDeleteUserShouldFailWhenUserDoesNotExist() throws Exception {
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		long userId = -1L;
		deleteUser(userId, accessToken, params, HttpStatus.NOT_FOUND);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	@Test
	public void testAccessToEndpointsByStandardUser() throws Exception {
		params.setUsername(standardUser.getEmail());
		long userId = 1L;
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		deleteUser(standardUser.getId(), accessToken, params, HttpStatus.FORBIDDEN);
		createAccount(userRequest, params, accessToken, HttpStatus.FORBIDDEN);
		updateAccount(userDataRequest, params, userId, accessToken, HttpStatus.FORBIDDEN);
		updatePassword(passwordRequest, params, standardUser.getId(), accessToken, HttpStatus.FORBIDDEN);
		getUsers(accessToken, params, HttpStatus.FORBIDDEN, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		fetchUser(standardUser.getId(), accessToken, params, HttpStatus.FORBIDDEN);
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	private void deleteUser(long userId, String accessToken, OAuthClientParams params, HttpStatus expectedHttpStatus) throws Exception {
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		mvc.perform(delete("/api/admin/users/delete/" + userId)
				.with(httpBasic(params.getClientId(), params.getClientDetailsPassword()))
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.params(paramsMap)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().is(expectedHttpStatus.value()))
		.andReturn();
	}

	private MvcResult fetchUser(Long id, String accessToken, OAuthClientParams params, HttpStatus expectedHttpStatus) throws Exception {
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		return mvc.perform(get("/api/admin/users/show/" + id)
				.with(httpBasic(params.getClientId(), params.getClientDetailsPassword()))
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.params(paramsMap)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().is(expectedHttpStatus.value()))
		.andReturn();
	}

	@SuppressWarnings("unchecked")
	private void testPaginationOfUsers(Optional<String> search, OAuthClientParams params, Optional<String> SearchBy, Optional<Integer> perPage, List<AppUser> users) throws Exception, UnsupportedEncodingException {
		int numberOfElementsPerPage = (!perPage.isPresent()) ? PER_PAGE : perPage.get();
		int pagesNum = users.size() / numberOfElementsPerPage;
		String accessToken = getAccessToken(signIn(params, HttpStatus.OK));
		
		for (int i = 0; i <= pagesNum; i++) {
			MvcResult result = getUsers(accessToken, params, HttpStatus.OK, Optional.of(i), perPage, search, SearchBy);
			
			Map<String, Object> responseMap = jsonParser.parseMap(result.getResponse().getContentAsString());
			List<Map<String, Object>> results = ((List<?>) responseMap.get("content")).stream().map(obj -> {
				return (Map<String, Object>)obj;
			}).collect(Collectors.toList());
			
			int toIndex = i * numberOfElementsPerPage + numberOfElementsPerPage;
			List<AppUser> subList = users.subList(i * numberOfElementsPerPage, toIndex > users.size() ? users.size() : toIndex);
			
			assertEquals(subList.size(), results.size());
			
			for (int j = 0; j < subList.size(); j++) {
				Map<String, Object> responseObj = results.get(j);
				AppUser user = subList.get(j);
				compareResponseDataToAppUser(responseObj, user);
			}
		}
		signOut(accessToken, HttpStatus.NO_CONTENT);
	}
	
	private MvcResult getUsers(String accessToken, OAuthClientParams params, HttpStatus expectedHttpStatus, Optional<Integer> page, Optional<Integer> perPage, Optional<String> search, Optional<String> searchBy) throws Exception {
		StringBuilder queryStringBuilder = new StringBuilder("");
		if (page.isPresent() && page.get() > 0) {
			queryStringBuilder.append("/");
			queryStringBuilder.append(page.get());
		}
		appendToQueryString(queryStringBuilder, Optional.of("search"), search);
		appendToQueryString(queryStringBuilder, Optional.of("searchBy"), searchBy);
		appendToQueryString(queryStringBuilder, Optional.of("per_page"), perPage);
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		return mvc.perform(get("/api/admin/users/list" + queryStringBuilder.toString())
				.with(httpBasic(params.getClientId(), params.getClientDetailsPassword()))
				.header("Authorization", "Bearer " + accessToken)
				.params(paramsMap)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().is(expectedHttpStatus.value()))
		.andReturn();
	}
	
	private void appendToQueryString(StringBuilder queryStringBuilder, Optional<String> parameterName, Optional<?> parameterValue) {
		if (parameterValue.isPresent()) {
			String qs = queryStringBuilder.toString();
			queryStringBuilder.append(!qs.contains("?") ? "?" : "&");
			queryStringBuilder.append(parameterName.get());
			queryStringBuilder.append("=");
			queryStringBuilder.append(parameterValue.get().toString());
		}
	}

	@SuppressWarnings("unchecked")
	private void compareResponseDataToAppUser(Map<String, Object> responseMap, AppUser user) {
		List<Long> authorityIds = extractListOfLongsFromMapWithField(responseMap, "authorities");
		List<Long> roleIds = extractListOfLongsFromMapWithField(responseMap, "roles");
		
		UserData userData = new UserData();
		Map<String, Object> userDataMap = (Map<String, Object>) responseMap.get("userData");
		userData.setEmail(userDataMap.get("email").toString());
		
		assertEquals(user.getUserData().getEmail(), userData.getEmail());
		assertEquals(user.getEnabled(), (Boolean)responseMap.get("enabled"));
		user.getAuthorities().forEach(a -> {
			assertTrue(authorityIds.contains(a.getId()));
		});
		
		user.getRoles().forEach(r -> {
			assertTrue(roleIds.contains(r.getId()));
		});
	}

	private List<AppUser> createAppUsers(int usersNum, Optional<String> emailAddress) {
		return IntStream.range(0, usersNum).mapToObj(i -> {
			AppUser user = new AppUser();
			user.setEmail(emailAddress.isPresent() ? "test" + i + "@" + emailAddress.get() : faker.internet().emailAddress());
			user.setEnabled(true);
			//user.setLastAccountUpdateDate(Date.from(Instant.now()));
			user.setPassword(hashedPassword);
			user.getAuthorities().addAll(standardUser.getAuthorities());
			user.getRoles().addAll(standardUser.getRoles());
			userRepository.save(user);
			return user;
		}).collect(Collectors.toList());
	}
	
	@SuppressWarnings("unchecked")
	private List<Long> extractListOfLongsFromMapWithField(Map<String, Object> responseMap, String fieldName) {
		return ((List<?>) responseMap.get(fieldName)).stream().map(m -> {
			Map<String, Object> authorityMap = (Map<String, Object>)m;
			return Long.valueOf(authorityMap.get("id").toString());
		}).collect(Collectors.toList());
	}
	
	private void checkPresenceOfFieldsInResponse(Map<String, Object> responseMap) {
		int expectedKeySetSize = 6;
		assertTrue(responseMap.containsKey("userId"));
		assertTrue(responseMap.containsKey("userData"));
		assertTrue(responseMap.containsKey("enabled"));
		assertTrue(responseMap.containsKey("lastAccountUpdateDate"));
		assertTrue(responseMap.containsKey("authorities"));
		assertTrue(responseMap.containsKey("roles"));
		assertFalse(responseMap.containsKey("password"));
		assertFalse(responseMap.containsKey("passwordConfirmation"));
		assertEquals(responseMap.keySet().size(), expectedKeySetSize);
	}
	
	private MvcResult createAccount(UserModelRequest body, OAuthClientParams params, String accessToken, HttpStatus expectedHttpStatus) throws Exception {
		String requestBodyString = objectMapper.writeValueAsString(body);
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		return mvc.perform(post("/api/admin/users/create")
				.with(httpBasic(params.getClientId(), params.getClientDetailsPassword()))
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.params(paramsMap)
				.content(requestBodyString))
		.andExpect(status().is(expectedHttpStatus.value()))
		.andReturn();
	}
	
	private MvcResult updateAccount(UserDataExtendedModel body, OAuthClientParams params, long userId, String accessToken, HttpStatus expectedHttpStatus) throws Exception {
		String requestBodyString = objectMapper.writeValueAsString(body);
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		return mvc.perform(put("/api/admin/users/update/" + userId)
				.with(httpBasic(params.getClientId(), params.getClientDetailsPassword()))
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.params(paramsMap)
				.content(requestBodyString))
		.andExpect(status().is(expectedHttpStatus.value()))
		.andReturn();
	}
	
	private MvcResult updatePassword(PasswordRequestModel body, OAuthClientParams params, Long userId, String accessToken, HttpStatus expectedHttpStatus) throws Exception {
		String requestBodyString = objectMapper.writeValueAsString(body);
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		return mvc.perform(put("/api/admin/users/update_password/" + userId)
				.with(httpBasic(params.getClientId(), params.getClientDetailsPassword()))
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.params(paramsMap)
				.content(requestBodyString))
		.andExpect(status().is(expectedHttpStatus.value()))
		.andReturn();
	}
	
	private MvcResult signUserOut(OAuthClientParams params, Long userId, String adminAccessToken, HttpStatus expectedHttpStatus) throws Exception {
		MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
		paramsMap.add("client_id", params.getClientId());
		return mvc.perform(post("/api/admin/users/sign_user_out/" + userId)
				.with(httpBasic(params.getClientId(), params.getClientDetailsPassword()))
				.header("Authorization", "Bearer " + adminAccessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.params(paramsMap))
		.andExpect(status().is(expectedHttpStatus.value()))
		.andReturn();
	}
}
