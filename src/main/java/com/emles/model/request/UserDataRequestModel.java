package com.emles.model.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.emles.utils.ValidationErrorMessages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model used for storing new user-specific account data.
 * 
 * @author darglk
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDataRequestModel {

	/**
	 * email - new email of user.
	 */
	@NotBlank(message = ValidationErrorMessages.EMAIL_NOT_BLANK_MESSAGE)
	@Email(message = ValidationErrorMessages.EMAIL_INVALID_MESSAGE)
	private String email;
}
