package com.github.ideahut.sbms.shared.audit.handler;

import com.github.ideahut.sbms.shared.audit.AuditExecutor.ContentType;
import com.github.ideahut.sbms.shared.audit.AuditObject;

public interface AuditHandler {

	public void save(AuditObject auditObject, ContentType contentType) throws Exception;
	
}
