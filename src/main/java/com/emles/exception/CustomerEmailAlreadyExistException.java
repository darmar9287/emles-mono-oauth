package com.emles.exception;

public class CustomerEmailAlreadyExistException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -88511952090545977L;

	public CustomerEmailAlreadyExistException(String message) {
		super(message);
	}
}
