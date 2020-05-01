package com.emles.exception;

public class OrderNotFoundException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4177246776325613136L;

	public OrderNotFoundException(String message) {
		super(message);
	}
}
