package com.devy.droidipc.exception;

public class NotSupportOperationException extends IPCRuntimeException{
	
	public NotSupportOperationException(int code) {
		super(code);
	}
	
	public NotSupportOperationException(int code, String message) {
		super(code, message);
	}
	
	public NotSupportOperationException(String message) {
		super(0, message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
