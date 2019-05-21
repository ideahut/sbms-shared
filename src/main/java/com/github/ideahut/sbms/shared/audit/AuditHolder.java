package com.github.ideahut.sbms.shared.audit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AuditHolder {
	
	private static final ThreadLocal<String> auditorHolder = new ThreadLocal<String>();

	private static final ThreadLocal<String> inheritableAuditorHolder = new InheritableThreadLocal<String>();
	
	private static final ThreadLocal<List<AuditObject>> objectListHolder = new ThreadLocal<List<AuditObject>>();

	private static final ThreadLocal<List<AuditObject>> inheritableObjectListHolder = new InheritableThreadLocal<List<AuditObject>>();
	
	
	public static void removeAuditor() {
		auditorHolder.remove();
		inheritableAuditorHolder.remove();
	}
	
	public static void setAuditor(String auditor, boolean inheritable) {
		if (auditor == null) {
			removeAuditor();
		} else {
			if (inheritable) {
				inheritableAuditorHolder.set(auditor);
				auditorHolder.remove();
			} else {
				auditorHolder.set(auditor);
				inheritableAuditorHolder.remove();
			}
		}
	}
	
	public static void setAuditor(String auditor) {
		setAuditor(auditor, false);
	}
	
	public static String getAuditor() {
		String auditor = auditorHolder.get();
		if (auditor == null) {
			auditor = inheritableAuditorHolder.get();
		}
		return auditor;
	}
	
	
	public static void resetObjectList() {
		objectListHolder.remove();
		inheritableObjectListHolder.remove();
	}
	
	public static void setObjectList(List<AuditObject> objectList, boolean inheritable) {
		if (objectList == null) {
			resetObjectList();
		} else {
			if (inheritable) {
				inheritableObjectListHolder.set(objectList);
				objectListHolder.remove();				
			} else {
				objectListHolder.set(objectList);
				inheritableObjectListHolder.remove();
			}
		}
	}
	
	public static List<AuditObject> getObjectList() {
		List<AuditObject> objectList = objectListHolder.get();
		if (objectList == null) {
			objectList = inheritableObjectListHolder.get();
		}
		return objectList;
	}
	
	public static void add(AuditObject auditObject, boolean inheritable) {
		if (auditObject == null || auditObject.getObject() == null) {
			return;
		}
		List<AuditObject> objectList = getObjectList();
		if (objectList == null) {
			setObjectList(new ArrayList<AuditObject>(), inheritable);
			objectList = getObjectList();
		}
		
		String auditor = auditObject.getAuditor();
		if (auditor == null) {
			auditor = getAuditor();
		}
		auditObject.setAuditor(auditor);
		
		String action = auditObject.getAction();
		if (action == null) {
			action = "_UNDEFINED_";
		}
		auditObject.setAction(action);
		
		Date entry = auditObject.getEntry();
		if (entry == null) {
			entry = new Date();
		}
		auditObject.setEntry(entry);
		
		objectList.add(auditObject);
	}
	
	public static void add(AuditObject auditObject) {
		add(auditObject, false);
	}
	
	public static void add(String auditor, String action, Object object, boolean inheritable) {
		add(new AuditObject(auditor, action, object), inheritable);
	}
	
	public static void add(String auditor, String action, Object object) {
		add(auditor, action, object, false);
	}
	
	public static void add(String action, Object object, boolean inheritable) {
		add(null, action, object, inheritable);
	}
	
	public static void add(String action, Object object) {
		add(null, action, object, false);
	}
	
}
