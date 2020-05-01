package com.emles.exception;

public class CustomerPhoneAlreadyExistsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5992845110241733722L;

	public CustomerPhoneAlreadyExistsException(String message) {
		super(message);
	}
}
