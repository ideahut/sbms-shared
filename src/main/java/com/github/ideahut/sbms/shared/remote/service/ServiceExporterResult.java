package com.github.ideahut.sbms.shared.remote.service;

public class ServiceExporterResult {

	private final Object value;

	private final Throwable exception;
	
	public ServiceExporterResult(Object value) {
		this.value = value;
		exception = null;
	}
	
	public ServiceExporterResult(Throwable exception) {
		this.value = null;
		this.exception = exception;		
	}

	public Object getValue() {
		return value;
	}

	public Throwable getException() {
		return exception;
	}	
	
}
