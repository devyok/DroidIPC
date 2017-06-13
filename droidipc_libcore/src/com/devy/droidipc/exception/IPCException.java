package com.devy.droidipc.exception;

public class IPCException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int code;
	
	public int getCode() {
		return code;
	}

	public IPCException(int code,String message){
		super((code > 0 ? "error code : " + code +"  " + message : message));
	}

	public IPCException(int code) {
		super((code > 0 ? "error code : " + code : "none"));
	}
	
	public IPCException(String message) {
		super(message);
	}

	@Override
	public void printStackTrace() {
		super.printStackTrace();
	}
	
}
