package com.emles.model.request.admin;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.emles.model.request.UserDataRequestModel;
import com.emles.utils.ValidationErrorMessages;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model used for creating new user by admin.
 * 
 * @author darglk
 *
 */
@Data
@NoArgsConstructor
public class UserModelRequest {

	/**
	 * userId - user ID in DB.
	 */
	private Long userId;

	/**
	 * userData - user-specific account data.
	 */
	@Valid
	@NotNull(message = ValidationErrorMessages.USER_DATA_ATTRIBUTES_NOT_NULL_MESSAGE)
	private UserDataRequestModel userData;

	/**
	 * password - wrapper for user password.
	 */
	@Valid
	@NotNull(message = ValidationErrorMessages.PASSWORDS_NOT_EMPTY_MESSAGE)
	private PasswordRequestModel passwords;

	/**
	 * enabled - boolean value which determines if user account should be
	 * enabled/disabled.
	 */
	@NotNull(message = ValidationErrorMessages.USER_ENABLED_NOT_EMPTY_MESSAGE)
	private Boolean enabled;

	/**
	 * authorityIds - IDs of authorities for newly created user.
	 */
	@NotNull(message = ValidationErrorMessages.AUTHORITY_IDS_NOT_NULL_MESSAGE)
	private Set<Long> authorityIds;

	/**
	 * roleIds - IDs of roles for newly created user.
	 */
	@NotNull(message = ValidationErrorMessages.ROLE_IDS_NOT_NULL_MESSAGE)
	private Set<Long> roleIds;
}
