package com.emles.controller;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.Errors;

import com.emles.dto.UserDTO;
import com.emles.dto.UserDataDTO;
import com.emles.exception.ValidationException;
import com.emles.model.AppUser;
import com.emles.model.Authority;
import com.emles.model.Role;
import com.emles.model.request.ChangePasswordRequestModel;
import com.emles.model.request.ForgotPasswordRequestModel;
import com.emles.model.request.UserDataRequestModel;
import com.emles.model.request.UserRegistrationRequestModel;
import com.emles.model.request.admin.PasswordRequestModel;
import com.emles.model.response.ResponseModel;
import com.emles.model.response.UserRegistrationModelResponse;
import com.emles.service.UserService;
import com.emles.utils.ResponseMessages;


@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class AppUserControllerTest {
	
	@Mock
	private UserService userService;
	
	@Mock
	private AuthenticationManager authenticationManager;
	
	@Mock
	private MockHttpServletRequest httpServletRequest;
	
	@Mock
	private MockHttpServletResponse httpServletResponse;
	
	@InjectMocks
	private AppUserController appUserController;
	
	private String email = "test@test.com";

	@Mock
	private SecurityContext context;

	@Mock
	private Authentication authentication;

	@Mock
	private User userMock;
	
	@Mock
	private ChangePasswordRequestModel changePasswordRequestModel;
	
	@Mock
	private Errors errors;

	@Mock
	private PasswordRequestModel passwordRequestModel;

	@Mock
	private AppUser user;

	@Mock
	private UserRegistrationRequestModel userRegistrationModel;
	
	@Mock
	private UserDTO userDTO;

	@Mock
	private UserDataDTO userDataDTO;

	@Mock
	private UserDataRequestModel userDataRequestModel;

	@Mock
	private ForgotPasswordRequestModel forgotPasswordRequestModel;
	
	@BeforeEach
	public void setUp() {

		lenient().when(context.getAuthentication()).thenReturn(authentication);
		lenient().when(authentication.getName()).thenReturn(email);
		lenient().when(authentication.getPrincipal()).thenReturn(userMock);
		SecurityContextHolder.setContext(context);
		
		lenient().when(changePasswordRequestModel.getPasswords()).thenReturn(passwordRequestModel);
		lenient().when(changePasswordRequestModel.getOldPassword()).thenReturn("oldPassword");
		lenient().when(passwordRequestModel.getPassword()).thenReturn("password");
		lenient().when(passwordRequestModel.getPasswordConfirmation()).thenReturn("password");
		
		lenient().when(errors.hasErrors()).thenReturn(false);
		
		lenient().when(userRegistrationModel.getPasswords()).thenReturn(passwordRequestModel);
		lenient().when(userRegistrationModel.getUserData()).thenReturn(userDataRequestModel);
		lenient().when(userDataRequestModel.getEmail()).thenReturn(email);
		
		lenient().when(userDataDTO.getEmail()).thenReturn(email);
		lenient().when(userDTO.getUserData()).thenReturn(userDataDTO);
		lenient().when(userDTO.getAuthorities()).thenReturn(new HashSet<Authority>());
		lenient().when(userDTO.getRoles()).thenReturn(new HashSet<Role>());
		lenient().when(userDTO.getEnabled()).thenReturn(false);
		lenient().when(userDTO.getLastAccountUpdateDate()).thenReturn(Date.from(Instant.now()));
		lenient().when(userDTO.getPassword()).thenReturn("password");
		
		lenient().when(forgotPasswordRequestModel.getEmail()).thenReturn(email);
	}
	
	@Test
	public void testActivateAccountShouldReturnOkStatus() throws Exception {
		long userId = 1L;
		String token = "token";
		ResponseEntity<?> response = appUserController.activateAccount(userId, token);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		verify(userService, times(1)).activateUserAccount(userId, token);
	}
	
	@Test
	public void testChangePasswordShouldThrowExceptionIfThereAreValidationErrors() {
		when(errors.hasErrors()).thenReturn(true);
		assertThrows(ValidationException.class, () -> {
			appUserController.changePassword(changePasswordRequestModel, errors, httpServletRequest, httpServletResponse);
		});
	}
	
	@Test
	public void testChangePasswordShouldReturnOkStatusWhenThereAreNoValidationErrors() {
		when(userService.findUserByEmail(email)).thenReturn(user);
		appUserController.changePassword(changePasswordRequestModel, errors, httpServletRequest, httpServletResponse);
		
		verify(context, times(1)).getAuthentication();
		verify(authentication, times(1)).getName();
		verify(userService, times(1)).findUserByEmail(email);
		verify(userService, times(1)).updatePassword(user, changePasswordRequestModel);
	}
	
	@Test
	public void testSignUpShouldThrowExceptionWhenThereAreValidationErrors() {
		when(errors.hasErrors()).thenReturn(true);
		assertThrows(ValidationException.class, () -> {
			appUserController.signUp(userRegistrationModel, errors);
		});
	}
	
	@Test
	public void testSignUpShouldReturnOkStatusWhenThereAreNoValidationErrors() {
		when(userService.createStandardUser(Mockito.any())).thenReturn(userDTO);
		
		ResponseEntity<UserRegistrationModelResponse> response = appUserController.signUp(userRegistrationModel, errors);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertNotNull(response.getBody());
		verify(userRegistrationModel, times(1)).getPasswords();
		verify(passwordRequestModel, times(1)).getPassword();
		verify(userService, times(1)).createStandardUser(Mockito.any());
	}
	
	@Test
	public void testForgotPasswordShouldThrowExceptionWhenThereAreValidationErrors() {
		when(errors.hasErrors()).thenReturn(true);
		assertThrows(ValidationException.class, () -> {
			appUserController.forgotPassword(forgotPasswordRequestModel, errors);
		});
	}
	
	@Test
	public void testForgotPasswordShouldReturnOkStatusWhenThereAreNoValidationErrors() {
		ResponseEntity<ResponseModel> response = appUserController.forgotPassword(forgotPasswordRequestModel, errors);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(response.getBody().getMessage(), ResponseMessages.PASSWORD_TOKEN_CREATED_MESSAGE);
		
		verify(userService, times(1)).createPasswordResetToken(email);
		verify(forgotPasswordRequestModel, times(1)).getEmail();
	}
	
	@Test
	public void testResetPasswordShouldThrowExceptionWhenThereAreValidationErrors() {
		when(errors.hasErrors()).thenReturn(true);
		long userId = 1L;
		String resetToken = "abcd";
		assertThrows(ValidationException.class, () -> {
			appUserController.resetPassword(passwordRequestModel, errors, userId, resetToken);
		});
	}
	
	@Test
	public void testResetPasswordShouldReturnOkStatusWhenThereAreNoValidationErrors() {
		long userId = 1L;
		String resetToken = "abcd";

		ResponseEntity<ResponseModel> response = appUserController.resetPassword(passwordRequestModel, errors, userId, resetToken);
		
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(response.getBody().getMessage(), ResponseMessages.PASSWORD_HAS_BEEN_CHANGED_MESSAGE);
		verify(userService, times(1)).changeForgottenPassword(passwordRequestModel, userId, resetToken);
	}
	
	@Test
	public void testUpdateAccountDataShouldThrowValidationExceptionWhenThereAreValidationErrors() {
		when(errors.hasErrors()).thenReturn(true);
		assertThrows(ValidationException.class, () -> {
			appUserController.updateAccountData(userDataRequestModel, errors, httpServletRequest, httpServletResponse);
		});
	}
	
	@Test
	public void testDeleteAccountShouldReturnNoContent() {
		ResponseEntity<?> response = appUserController.deleteAccount(httpServletRequest);
		assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
		verify(context, times(1)).getAuthentication();
		verify(authentication, times(1)).getName();
		verify(userService, times(1)).deleteUser(email);
	}
}
