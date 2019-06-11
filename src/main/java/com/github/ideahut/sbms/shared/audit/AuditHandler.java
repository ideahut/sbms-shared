package com.github.ideahut.sbms.shared.audit;

import com.github.ideahut.sbms.shared.audit.AuditExecutor.ContentType;

public interface AuditHandler {

	public void initialize() throws Exception;
	
	public void save(AuditObject auditObject, ContentType contentType) throws Exception;
	
}
