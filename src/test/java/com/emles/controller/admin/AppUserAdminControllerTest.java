package com.emles.controller.admin;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import com.emles.dto.UserDTO;
import com.emles.dto.UserDataDTO;
import com.emles.exception.ValidationException;
import com.emles.model.AppUser;
import com.emles.model.Authority;
import com.emles.model.Role;
import com.emles.model.request.admin.PasswordRequestModel;
import com.emles.model.request.admin.UserDataExtendedModel;
import com.emles.model.request.admin.UserModelRequest;
import com.emles.model.response.admin.UserModelResponse;
import com.emles.service.UserService;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class AppUserAdminControllerTest {

	@Mock
	private UserService userService;
	
	@InjectMocks
	private AppUserAdminController adminController;
	
	@Mock
	private UserModelRequest userModelRequest;
	
	@Mock
	private UserDataExtendedModel userExtendedModel;
	
	@Mock
	private PasswordRequestModel passwordRequestModel;
	
	@Mock
	private UserDTO userDTO;
	
	@Mock
	private UserDataDTO userDataDTO;
	
	@Mock
	private AppUser user;
	
	@Mock
	private Errors errors;
	
	@BeforeEach
	public void setUp() {
		lenient().when(userDataDTO.getEmail()).thenReturn("test@test.com");
		lenient().when(userDTO.getUserData()).thenReturn(userDataDTO);
		lenient().when(userDTO.getAuthorities()).thenReturn(new HashSet<Authority>());
		lenient().when(userDTO.getRoles()).thenReturn(new HashSet<Role>());
		lenient().when(userDTO.getEnabled()).thenReturn(false);
		lenient().when(userDTO.getLastAccountUpdateDate()).thenReturn(Date.from(Instant.now()));
		lenient().when(userDTO.getPassword()).thenReturn("password");
		
		lenient().when(errors.hasErrors()).thenReturn(false);
	}
	
	@Test
	public void testCreateUserShouldThrowExceptionWhenThereAreValidationErrors() {
		when(errors.hasErrors()).thenReturn(true);
		assertThrows(ValidationException.class, () -> {
			adminController.createUser(userModelRequest, errors);
		});
	}
	
	@Test
	public void testCreateUserShouldReturnOkStatusWhenThereAreNoValidationErrors() {
		when(userService.createUser(userModelRequest)).thenReturn(userDTO);
		
		ResponseEntity<UserModelResponse> response = adminController.createUser(userModelRequest, errors);
		UserModelResponse responseModel = response.getBody();
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(responseModel.getUserData().getEmail(), userDataDTO.getEmail());
		assertEquals(responseModel.getEnabled(), userDTO.getEnabled());
		assertEquals(responseModel.getLastAccountUpdateDate(), userDTO.getLastAccountUpdateDate());
		verify(userService, times(1)).createUser(userModelRequest);
	}
	
	@Test
	public void testUpdateUserShouldThrowExceptionWhenThereAreValidationErrors() {
		long userId = 1L;
		when(errors.hasErrors()).thenReturn(true);
		assertThrows(ValidationException.class, () -> {
			adminController.updateUser(userExtendedModel, errors, userId);
		});
	}
	
	@Test
	public void testUpdateUserShouldReturnOkStatusWhenThereAreNoValidationErrors() {
		long userId = 1L;
		when(userService.updateUser(userExtendedModel, userId)).thenReturn(userDTO);
		
		ResponseEntity<UserModelResponse> response = adminController.updateUser(userExtendedModel, errors, userId);
		UserModelResponse responseModel = response.getBody();
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(responseModel.getUserData().getEmail(), userDataDTO.getEmail());
		assertEquals(responseModel.getEnabled(), userDTO.getEnabled());
		assertEquals(responseModel.getLastAccountUpdateDate(), userDTO.getLastAccountUpdateDate());
		verify(userService, times(1)).updateUser(userExtendedModel, userId);
	}
	
	@Test
	public void testUpdateUserPasswordShouldThrowExceptionWhenThereAreValidationErrors() {
		long userId = 1L;
		when(errors.hasErrors()).thenReturn(true);
		assertThrows(ValidationException.class, () -> {
			adminController.updateUserPassword(passwordRequestModel, errors, userId);
		});
	}
	
	@Test
	public void testUpdateUserPasswordShouldReturnOkStatusWhenThereAreNoValidationErrors() {
		long userId = 1L;
		when(userService.updateUserPassword(passwordRequestModel, userId)).thenReturn(userDTO);
		
		ResponseEntity<UserModelResponse> response = adminController.updateUserPassword(passwordRequestModel, errors, userId);
		UserModelResponse responseModel = response.getBody();
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(responseModel.getUserData().getEmail(), userDataDTO.getEmail());
		assertEquals(responseModel.getEnabled(), userDTO.getEnabled());
		assertEquals(responseModel.getLastAccountUpdateDate(), userDTO.getLastAccountUpdateDate());
		verify(userService, times(1)).updateUserPassword(passwordRequestModel, userId);
	}
	
	@Test
	public void testShowUser() {
		long userId = 1L;
		when(userService.findUserById(userId)).thenReturn(userDTO);
		ResponseEntity<UserModelResponse> response = adminController.showUser(userId);
		UserModelResponse responseModel = response.getBody();
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(responseModel.getUserData().getEmail(), userDataDTO.getEmail());
		assertEquals(responseModel.getEnabled(), userDTO.getEnabled());
		assertEquals(responseModel.getLastAccountUpdateDate(), userDTO.getLastAccountUpdateDate());
		verify(userService, times(1)).findUserById(userId);
	}
	
	@Test
	public void testDeleteUserShouldReturnNoContent() {
		long userId = 1L;
		ResponseEntity<?> response = adminController.deleteUser(userId);
		assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
		verify(userService, times(1)).deleteUser(userId);
	}
	
	@Test
	public void testShowUserShouldReturnPaginatedUserModelResponse() {
		int pageNumber = 1;
		String perPage = "10";
		String searchString = "search";
		String searchBy = "searchBy";
		Page<UserDTO> pagedUsers = new PageImpl<>(Arrays.asList(userDTO));
		when(userService.getUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(pagedUsers);

		Page<UserModelResponse> response = adminController.showUsers(pageNumber, perPage, searchString, searchBy);
		
		assertEquals(response.getContent().size(), pagedUsers.getContent().size());
		verify(userService, times(1)).getUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any());
	}
	
	@Test
	public void testSignUserOutShouldReturnNoContentWhenUserExists() {
		Long userId = 1L;
		when(userService.findById(userId)).thenReturn(Optional.of(user));
		
		ResponseEntity<?> response = adminController.signUserOut(userId);
		
		assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
		verify(userService, times(1)).findById(userId);
		verify(userService, times(1)).signOutUser(user);
	}
	
	@Test
	public void testSignUserOutShouldReturnNotFoundWhenUserDoesNotExist() {
		Long userId = 1L;
		when(userService.findById(userId)).thenReturn(Optional.empty());
		
		ResponseEntity<?> response = adminController.signUserOut(userId);
		
		assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
		verify(userService, times(1)).findById(userId);
		verify(userService, times(0)).signOutUser(user);
	}
}
