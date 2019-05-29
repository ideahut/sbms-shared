package com.github.ideahut.sbms.shared.audit;

import java.util.Date;

import org.springframework.transaction.PlatformTransactionManager;

public class AuditObject {
	
	private PlatformTransactionManager transactionManager;
	
	private Auditor auditor;
	
	private String action;
	
	private String info;
	
	private Object object;
	
	private Date entry = new Date();
	
	public AuditObject() {}
	
	public AuditObject(String action, Object object, Auditor auditor, String info) {
		this.action = action;
		this.object = object;
		this.auditor = auditor;
		this.info = info;
	}
	
	public AuditObject(String action, Object object, Auditor auditor) {
		this(action, object, auditor, null);
	}
	
	public AuditObject(String action, Object object, String info) {
		this(action, object, null, info);
	}
	
	public AuditObject(String action, Object object) {
		this(action, object, null, null);
	}

	public Auditor getAuditor() {
		return auditor;
	}

	public void setAuditor(Auditor auditor) {
		this.auditor = auditor;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
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

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}	
	
}
