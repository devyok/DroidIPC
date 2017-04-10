package com.devy.droidipc.exception;

public class IPCRuntimeException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * ´íÎóÂë
	 */
	private int code;
	
	public int getCode() {
		return code;
	}

	public IPCRuntimeException(int code,String message){
		super((code > 0 ? "error code : " + code +"  " + message : message));
	}

	public IPCRuntimeException(int code) {
		super((code > 0 ? "error code : " + code : "none"));
	}
	
	public IPCRuntimeException(String message) {
		super(message);
	}

	@Override
	public void printStackTrace() {
		super.printStackTrace();
	}
	
}
