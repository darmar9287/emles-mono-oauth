package com.emles.exception;

public class CustomerNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7865334283298524666L;

	public CustomerNotFoundException(String message) {
		super(message);
	}
}
