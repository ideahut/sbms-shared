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
	
	private List<AuditObject> auditObjectList;
	
	private List<EntityInterceptor> entityInterceptorList;

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

	public List<AuditObject> getAuditObjectList() {
		return auditObjectList;
	}

	public void setAuditObjectList(List<AuditObject> auditObjectList) {
		this.auditObjectList = auditObjectList;
	}
	
	public void addAuditObject(AuditObject auditObject) {
		if (auditObject == null || auditObject.getObject() == null) {
			return;
		}
		if (auditObjectList == null) {
			auditObjectList = new ArrayList<AuditObject>();			
		}
		Date entry = auditObject.getEntry();
		if (entry == null) {
			entry = new Date();
		}
		auditObject.setEntry(entry);		
		auditObjectList.add(auditObject);
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

	public List<EntityInterceptor> getEntityInterceptorList() {
		return entityInterceptorList;
	}

	public void setEntityInterceptorList(List<EntityInterceptor> entityInterceptorList) {
		this.entityInterceptorList = entityInterceptorList;
	}
	
	public void addEntityInterceptor(EntityInterceptor entityInterceptor) {
		if (entityInterceptor == null) {
			return;
		}
		if (entityInterceptorList == null) {
			entityInterceptorList = new ArrayList<EntityInterceptor>();
		}
		entityInterceptorList.add(entityInterceptor);
	}
}
