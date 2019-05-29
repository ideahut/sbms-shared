package com.github.ideahut.sbms.shared.audit.interceptor;

import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.github.ideahut.sbms.shared.annotation.Auditable;
import com.github.ideahut.sbms.shared.audit.AuditObject;
import com.github.ideahut.sbms.shared.entity.EntityBase;
import com.github.ideahut.sbms.shared.entity.EntityInterceptor;
import com.github.ideahut.sbms.shared.moment.MomentAttributes;
import com.github.ideahut.sbms.shared.moment.MomentHolder;

public class TransactionManagerAuditEntityInterceptor implements EntityInterceptor {
	
	private static final Method CurrentTransactionInfo;
	
	static {
		try {
			CurrentTransactionInfo = TransactionAspectSupport.class.getDeclaredMethod("currentTransactionInfo");
			CurrentTransactionInfo.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}

	@Override
	public void onPrePersist(EntityBase<?> entity) {
		Auditable auditable = entity.getClass().getAnnotation(Auditable.class);
		if (auditable != null && auditable.value()) {
			MomentAttributes momentAttributes = MomentHolder.getMomentAttributes();
			if (momentAttributes != null) {
				momentAttributes.addAuditObject(auditObject("CREATE", entity));
			}
		}
	}

	@Override
	public void onPreUpdate(EntityBase<?> entity) {
		Auditable auditable = entity.getClass().getAnnotation(Auditable.class);
		if (auditable != null && auditable.value()) {
			MomentAttributes momentAttributes = MomentHolder.getMomentAttributes();
			if (momentAttributes != null) {
				momentAttributes.addAuditObject(auditObject("UPDATE", entity));
			}
		}
	}

	@Override
	public void onPreRemove(EntityBase<?> entity) {
		Auditable auditable = entity.getClass().getAnnotation(Auditable.class);
		if (auditable != null && auditable.value()) {
			MomentAttributes momentAttributes = MomentHolder.getMomentAttributes();
			if (momentAttributes != null) {
				momentAttributes.addAuditObject(auditObject("DELETE", entity));
			}
		}
	}
	
	private AuditObject auditObject(String action, EntityBase<?> entity) {
		PlatformTransactionManager transactionManager = null;
		try {
			Object transactionInfo = CurrentTransactionInfo.invoke(null);			
			transactionManager = (PlatformTransactionManager)transactionInfo
				.getClass()
				.getMethod("getTransactionManager")
				.invoke(transactionInfo);			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		AuditObject auditObject = new AuditObject(action, entity);
		auditObject.setTransactionManager(transactionManager);
		return auditObject;
	}

}
