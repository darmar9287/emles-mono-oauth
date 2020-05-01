package com.emles.model.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.emles.utils.ValidationErrorMessages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestModel {

	private Long customerId;
	
	@NotBlank(message = ValidationErrorMessages.FIRST_NAME_NOT_EMPTY_MESSAGE)
	private String firstName;
	
	@NotBlank(message = ValidationErrorMessages.SECOND_NAME_NOT_EMPTY_MESSAGE)
	private String secondName;
	
	@NotBlank(message = ValidationErrorMessages.EMAIL_NOT_EMPTY_MESSAGE)
	@Email(message = ValidationErrorMessages.INVALID_EMAIL_MESSAGE)
	private String email;
	
	@NotBlank(message = ValidationErrorMessages.PHONE_NUMBER_NOT_EMPTY_MESSAGE)
	private String phone;
	
	@NotBlank(message = ValidationErrorMessages.ADDRESS_NOT_EMPTY_MESSAGE)
	private String address;
}
