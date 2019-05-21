package com.github.ideahut.sbms.shared.audit;

public interface AuditExecutor {
	
	public enum ContentType {
		STRING,
		BYTES,
		STRING_AND_BYTES
	}
	
	public void run();

}
