package com.github.ideahut.sbms.shared.moment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.github.ideahut.sbms.shared.audit.AuditObject;
import com.github.ideahut.sbms.shared.audit.Auditor;
import com.github.ideahut.sbms.shared.entity.EntityInterceptor;

public class MomentAttributes {
	
	private Locale locale;
	
	private String language;
	
	private Auditor auditor;
	
	private List<AuditObject> auditObjects;
	
	private List<EntityInterceptor> entityInterceptors;

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Auditor getAuditor() {
		return auditor;
	}

	public void setAuditor(Auditor auditor) {
		this.auditor = auditor;
	}

	public List<AuditObject> getAuditObjects() {
		return auditObjects;
	}

	public void setAuditObjects(List<AuditObject> auditObjects) {
		this.auditObjects = auditObjects;
	}

	public void addAuditObject(AuditObject auditObject) {
		if (auditObject == null || auditObject.getObject() == null) {
			return;
		}
		if (auditObjects == null) {
			auditObjects = new ArrayList<AuditObject>();			
		}
		Date entry = auditObject.getEntry();
		if (entry == null) {
			entry = new Date();
		}
		auditObject.setEntry(entry);		
		auditObjects.add(auditObject);
	}
	
	public void addAuditObject(String action, Object object, Auditor auditor, String info) {
		addAuditObject(new AuditObject(action, object, auditor, info));
	}
	
	public void addAuditObject(String action, Object object, Auditor auditor) {
		addAuditObject(action, object, auditor, null);
	}
	
	public void addAuditObject(String action, Object object, String info) {
		addAuditObject(action, object, null, info);
	}
	
	public void addAuditObject(String action, Object object) {
		addAuditObject(action, object, null, null);
	}
	
	public List<EntityInterceptor> getEntityInterceptors() {
		return entityInterceptors;
	}

	public void setEntityInterceptors(List<EntityInterceptor> entityInterceptors) {
		this.entityInterceptors = entityInterceptors;
	}
	
	public void addEntityInterceptor(EntityInterceptor entityInterceptor) {
		if (entityInterceptor == null) {
			return;
		}
		if (entityInterceptors == null) {
			entityInterceptors = new ArrayList<EntityInterceptor>();
		}
		entityInterceptors.add(entityInterceptor);
	}
}
