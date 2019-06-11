package com.github.ideahut.sbms.shared.audit.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.github.ideahut.sbms.shared.annotation.Auditable;
import com.github.ideahut.sbms.shared.audit.AuditExecutor.ContentType;
import com.github.ideahut.sbms.shared.audit.AuditHandler;
import com.github.ideahut.sbms.shared.audit.AuditObject;
import com.github.ideahut.sbms.shared.audit.Auditor;
import com.github.ideahut.sbms.shared.entity.EntityBase;
import com.github.ideahut.sbms.shared.optional.audit.Audit;
import com.github.ideahut.sbms.shared.optional.audit.AuditRepository;

public class RepositoryAuditHandler implements AuditHandler {
	
	private final ObjectMapper objectMapper;
	
	private AuditRepository auditRepository;
	
	@SuppressWarnings("serial")
	public RepositoryAuditHandler() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
			@Override
			public boolean hasIgnoreMarker(AnnotatedMember m) {
				Auditable auditable = _findAnnotation(m, Auditable.class);
				if (auditable != null && !auditable.value()) {
					return true;
				}
				return super.hasIgnoreMarker(m);
			}
		});
	}
	
	public void setAuditRepository(AuditRepository auditRepository) {
		this.auditRepository = auditRepository;
	}
	
	@Override
	public void initialize() throws Exception {
		if (auditRepository == null) {
			throw new Exception(AuditRepository.class.getName() + " is required");
		}
	}

	@Override
	public void save(AuditObject auditObject, ContentType contentType) throws Exception {
		Object object = auditObject.getObject();
		String type = object.getClass().getName();
		Auditor auditor = auditObject.getAuditor();
		
		Audit audit = new Audit();
		String action = auditObject.getAction();
		if (action == null) {
			action = "__UNDEFINED__";
		} else {
			action = (object instanceof EntityBase ? "ENTITY_" : "") + action;
		}
		audit.setAction(action);
		if (auditor != null) {
			audit.setAuditorId(auditor.getId());
			audit.setAuditorName(auditor.getName());
		}
		audit.setEntry(auditObject.getEntry());
		audit.setInfo(auditObject.getInfo());
		audit.setType(type);
		
		String content = null;
		byte[] bytes = null;
		if (object instanceof byte[]) {
			bytes = (byte[])object;
		} else {
			if (ContentType.BYTES.equals(contentType)) {
				bytes = objectMapper.writeValueAsBytes(object);					
			} else if (ContentType.STRING_AND_BYTES.equals(contentType)) {
				bytes = objectMapper.writeValueAsBytes(object);
				content = objectMapper.writeValueAsString(object);
			} else {
				content = objectMapper.writeValueAsString(object);
			}
		}
		audit.setBytes(bytes);
		audit.setContent(content);
		auditRepository.save(audit);
	}

}
