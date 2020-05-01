package com.emles.model.request;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.emles.model.request.admin.PasswordRequestModel;
import com.emles.utils.ValidationErrorMessages;
import com.emles.utils.ValidationRules;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This model is used by /change_password endpoint when user wants to change his
 * password to the application.
 * 
 * @author darglk
 *
 */
@Data
@NoArgsConstructor
public class ChangePasswordRequestModel {

	/**
	 * oldPassword - current user password.
	 */
	@Pattern(regexp = ValidationRules.PASSWORD_REGEX, message = ValidationErrorMessages.INVALID_OLD_PASSWORD_MESSAGE)
	@NotBlank(message = ValidationErrorMessages.OLD_PASSWORD_NOT_EMPTY_MESSAGE)
	private String oldPassword;

	/**
	 * password - wrapper for new password (with confirmation).
	 */
	@Valid
	@NotNull(message = ValidationErrorMessages.PASSWORDS_NOT_EMPTY_MESSAGE)
	private PasswordRequestModel passwords;
}
