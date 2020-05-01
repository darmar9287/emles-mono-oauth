package com.emles.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emles.dto.UserDTO;
import com.emles.dto.UserDataDTO;
import com.emles.model.AppUser;
import com.emles.model.request.ChangePasswordRequestModel;
import com.emles.model.request.ForgotPasswordRequestModel;
import com.emles.model.request.UserDataRequestModel;
import com.emles.model.request.UserRegistrationRequestModel;
import com.emles.model.request.admin.PasswordRequestModel;
import com.emles.model.response.ResponseModel;
import com.emles.model.response.UserRegistrationModelResponse;
import com.emles.service.UserService;

import com.emles.utils.ResponseMessages;
import com.emles.utils.Utils;


import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

	private UserService userService;

	public AppUserController(UserService userService) {
		this.userService = userService;
	}

	@ApiOperation("User can change password under this endpoint. New Access token will be sent after success")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer Access Token", paramType = "header")
	})
	@RequestMapping(value = "/change_password", method = RequestMethod.PUT)
	public ResponseEntity<OAuth2AccessToken> changePassword(@Valid @RequestBody ChangePasswordRequestModel model, Errors errors,
			HttpServletRequest request, HttpServletResponse response) {
		Utils.checkValidationErrors(errors);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		AppUser user = userService.findUserByEmail(authentication.getName());
		userService.updatePassword(user, model);

		OAuth2AccessToken accessToken = userService.removeAccessTokens(request.getHeader("Authorization"));
		
		accessToken = userService.requestNewAccessToken(request, user, accessToken);
		return ResponseEntity.ok().body(accessToken);
	}

	@ApiOperation("User signup endpoint")
	@RequestMapping(value = "/sign_up", method = RequestMethod.POST)
	public ResponseEntity<UserRegistrationModelResponse> signUp(
			@Valid @RequestBody UserRegistrationRequestModel newUser, Errors errors) {
		Utils.checkValidationErrors(errors);
		ModelMapper mapper = new ModelMapper();
		UserDTO userDTO = mapper.map(newUser, UserDTO.class);
		userDTO.setPassword(newUser.getPasswords().getPassword());
		UserDTO createdUser = userService.createStandardUser(userDTO);
		UserRegistrationModelResponse response = mapper.map(createdUser, UserRegistrationModelResponse.class);
		return ResponseEntity.ok(response);
	}

	@ApiOperation("Endpoint for activating account after signup.")
	@RequestMapping(value = "/activate_account", method = RequestMethod.GET)
	public ResponseEntity<?> activateAccount(@RequestParam("userId") long userId, @RequestParam("token") String token) {
		userService.activateUserAccount(userId, token);
		return ResponseEntity.ok().build();
	}

	@ApiOperation("Endpoint where user can submit his email address to obtain password reset token")
	@RequestMapping(value = "/forgot_password", method = RequestMethod.POST)
	public ResponseEntity<ResponseModel> forgotPassword(@RequestBody @Valid ForgotPasswordRequestModel model,
			Errors errors) {
		Utils.checkValidationErrors(errors);
		userService.createPasswordResetToken(model.getEmail());
		return ResponseEntity.ok(new ResponseModel(ResponseMessages.PASSWORD_TOKEN_CREATED_MESSAGE));
	}

	@ApiOperation("Endpoint for changing forgotten password")
	@RequestMapping(value = "/reset_password", method = RequestMethod.POST)
	public ResponseEntity<ResponseModel> resetPassword(@RequestBody @Valid PasswordRequestModel model,
			Errors errors, @RequestParam("userId") long userId, @RequestParam("token") String token) {
		Utils.checkValidationErrors(errors);
		userService.changeForgottenPassword(model, userId, token);
		return ResponseEntity.ok(new ResponseModel(ResponseMessages.PASSWORD_HAS_BEEN_CHANGED_MESSAGE));
	}

	@ApiOperation("Endpoint where user can update his account data. New Access token will be sent after success")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer Access Token", paramType = "header")
	})
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	public ResponseEntity<OAuth2AccessToken> updateAccountData(@RequestBody @Valid UserDataRequestModel model,
			Errors errors, HttpServletRequest request, HttpServletResponse response) {
		Utils.checkValidationErrors(errors);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		ModelMapper mapper = new ModelMapper();
		UserDataDTO userDataDTO = mapper.map(model, UserDataDTO.class);
		AppUser updatedAppUser = userService.updateUserData(userDataDTO, authentication.getName());

		OAuth2AccessToken accessToken = userService.removeAccessTokens(request.getHeader("Authorization"));
		accessToken = userService.requestNewAccessToken(request, updatedAppUser, accessToken);

		return ResponseEntity.ok().body(accessToken);
	}

	@ApiOperation("Endpoint where user can remove his account.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteAccount(HttpServletRequest request) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		userService.deleteUser(username);
		userService.removeAccessTokens(request.getHeader("Authorization"));
		SecurityContextHolder.clearContext();
		
		return ResponseEntity.noContent().build();
	}
	
	@ApiOperation("Endpoint where user view his account data.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer Access Token", paramType = "header")
	})
	@RequestMapping(value = "/me", method = RequestMethod.GET)
	public ResponseEntity<?> aboutMe() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(username);
	}
}
