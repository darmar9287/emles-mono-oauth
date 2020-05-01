package com.emles.model.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.emles.utils.ValidationRules;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model containing credentials of a given user.
 * 
 * @author darglk
 *
 */
@Data
@NoArgsConstructor
public class UserLoginRequestModel {

	/**
	 * email - login to the application.
	 */
	@Email
	@NotBlank
	private String email;

	/**
	 * password - non hashed password value.
	 */
	@NotBlank
	@Pattern(regexp = ValidationRules.PASSWORD_REGEX)
	private String password;
}