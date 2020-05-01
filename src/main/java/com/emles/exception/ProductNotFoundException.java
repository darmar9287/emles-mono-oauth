package com.emles.exception;

public class ProductNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1750211243612321220L;

	public ProductNotFoundException(String message) {
		super(message);
	}
}
