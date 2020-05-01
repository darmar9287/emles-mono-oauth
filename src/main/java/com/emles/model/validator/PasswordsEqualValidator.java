package com.emles.model.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.emles.model.request.admin.PasswordRequestModel;

public class PasswordsEqualValidator implements ConstraintValidator<PasswordsEqual, Object> {

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value instanceof PasswordRequestModel) {
			return ((PasswordRequestModel) value).arePasswordsEqual();
		}
		return false;
	}
}
