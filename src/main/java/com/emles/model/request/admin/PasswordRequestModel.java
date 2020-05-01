package com.emles.model.request.admin;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.emles.model.validator.PasswordsEqual;
import com.emles.utils.ValidationErrorMessages;
import com.emles.utils.ValidationRules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model containing password and its confirmation. It's a wrapper for: -
 * ChangePasswordRequestModel - UserRegistrationRequestModel
 * 
 * @author darglk
 *
 */
@PasswordsEqual
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordRequestModel {

	/**
	 * password - non-hashed password.
	 */
	@NotBlank(message = ValidationErrorMessages.PASSWORD_NOT_EMPTY_MESSAGE)
	@Pattern(regexp = ValidationRules.PASSWORD_REGEX, message = ValidationErrorMessages.INVALID_PASSWORD_MESSAGE)
	private String password;

	/**
	 * passwordConfirmation - non hashed password confirmation. It is meant to be
	 * equal to password.
	 */
	@NotBlank(message = ValidationErrorMessages.PASSWORD_CONFIRMATION_NOT_EMPTY_MESSAGE)
	@Pattern(regexp = ValidationRules.PASSWORD_REGEX, message = ValidationErrorMessages.INVALID_PASSWORD_CONFIRMATION_MESSAGE)
	private String passwordConfirmation;

	public boolean arePasswordsEqual() {
		return password != null && password.equals(passwordConfirmation);
	}
}
