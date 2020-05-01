package com.emles.controller.admin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emles.dto.UserDTO;
import com.emles.model.AppUser;
import com.emles.model.request.admin.PasswordRequestModel;
import com.emles.model.request.admin.UserDataExtendedModel;
import com.emles.model.request.admin.UserModelRequest;
import com.emles.model.response.admin.UserModelResponse;
import com.emles.service.UserService;
import com.emles.utils.Utils;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/admin/users")
public class AppUserAdminController {

	@Value("${config.pagination.default_page_offset}")
	private Integer PER_PAGE;

	private UserService userService;

	private ModelMapper mapper = new ModelMapper();

	public AppUserAdminController(UserService userService) {
		super();
		this.userService = userService;
	}

	@ApiOperation("Endpoint where admin can create new users. New user will not have to activate his account.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer Access Token", paramType = "header")
	})
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity<UserModelResponse> createUser(@Valid @RequestBody UserModelRequest model, @ApiIgnore Errors errors) {
		Utils.checkValidationErrors(errors);
		UserDTO created = userService.createUser(model);
		UserModelResponse response = mapper.map(created, UserModelResponse.class);
		return ResponseEntity.ok(response);
	}

	@ApiOperation("Endpoint where admin can change user account data (including authorities and enabling/disabling account)")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer Access Token", paramType = "header")
	})
	@RequestMapping(value = "/update/{userId}", method = RequestMethod.PUT)
	public ResponseEntity<UserModelResponse> updateUser(@Valid @RequestBody UserDataExtendedModel model,
			Errors errors, @PathVariable(name = "userId", required = true) Long userId) {
		Utils.checkValidationErrors(errors);
		UserDTO updated = userService.updateUser(model, userId);
		UserModelResponse response = mapper.map(updated, UserModelResponse.class);
		return ResponseEntity.ok(response);
	}

	@ApiOperation("Endpoint where admin can change users password")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer Access Token", paramType = "header")
	})
	@RequestMapping(value = "/update_password/{userId}", method = RequestMethod.PUT)
	public ResponseEntity<UserModelResponse> updateUserPassword(@Valid @RequestBody PasswordRequestModel model,
			Errors errors, @PathVariable(name = "userId", required = true) Long userId) {
		Utils.checkValidationErrors(errors);
		UserDTO updated = userService.updateUserPassword(model, userId);
		UserModelResponse response = mapper.map(updated, UserModelResponse.class);
		return ResponseEntity.ok(response);
	}

	@ApiOperation("Endpoint where admin can view given users account data")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer Access Token", paramType = "header")
	})
	@RequestMapping(value = "/show/{userId}", method = RequestMethod.GET)
	public ResponseEntity<UserModelResponse> showUser(@PathVariable("userId") Long userId) {
		UserDTO user = userService.findUserById(userId);
		UserModelResponse response = mapper.map(user, UserModelResponse.class);
		return ResponseEntity.ok(response);
	}

	@ApiOperation("Endpoint where admin can get list of users")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer Access Token", paramType = "header")
	})
	@RequestMapping(value = { "/list/{pageNum}", "/list" }, method = RequestMethod.GET)
	public Page<UserModelResponse> showUsers(@PathVariable(value = "pageNum", required = false) Integer pageNumber,
			@RequestParam(value = "per_page", required = false) String perPage,
			@RequestParam(value = "search", required = false, defaultValue = "") String searchString,
			@RequestParam(value = "searchBy", required = false, defaultValue = "email") String searchBy) {
		if (pageNumber == null) {
			pageNumber = 0;
		}
		int pageOffset = calculatePageOffset(perPage);
		Pageable pageable = PageRequest.of(pageNumber, pageOffset);
		Page<UserDTO> users = userService.getUsers(searchString, searchBy, pageable);
		List<UserModelResponse> usersContent = users.get().map(userDTO -> mapper.map(userDTO, UserModelResponse.class))
				.collect(Collectors.toList());
		Page<UserModelResponse> usersResponse = new PageImpl<>(usersContent, pageable, users.getTotalElements());
		return usersResponse;
	}

	@ApiOperation("Endpoint where admin can delete users account")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "Bearer JWT Token", paramType = "header")
	})
	@RequestMapping(value = "/delete/{userId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteUser(@PathVariable("userId") Long userId) {
		userService.deleteUser(userId);
		return ResponseEntity.noContent().build();
	}
	
	@RequestMapping(value = "/sign_user_out/{userId}", method = RequestMethod.POST)
	public ResponseEntity<?> signUserOut(@PathVariable("userId") Long userId) {
		Optional<AppUser> userOpt = userService.findById(userId);
		if (userOpt.isPresent()) {
			userService.signOutUser(userOpt.get());
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}

	private int calculatePageOffset(String perPage) {
		int defaultPageOffset;
		try {
			defaultPageOffset = Integer.valueOf(perPage);
			if (defaultPageOffset <= 0) {
				defaultPageOffset = PER_PAGE;
			}
		} catch (NumberFormatException e) {
			defaultPageOffset = PER_PAGE;
		}
		return defaultPageOffset;
	}
}
