package com.github.ideahut.sbms.shared.audit;

import java.util.Date;

public class AuditObject {
	
	private String auditor;
	
	private String action;
	
	private Object object;
	
	private Date entry = new Date();
	
	public AuditObject() {}
	
	public AuditObject(String auditor, String action, Object object) {
		this.auditor = auditor;
		this.action  = action;
		this.object  = object;
	}

	public String getAuditor() {
		return auditor;
	}

	public void setAuditor(String auditor) {
		this.auditor = auditor;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Date getEntry() {
		return entry;
	}

	public void setEntry(Date entry) {
		this.entry = entry;
	}
	
}
