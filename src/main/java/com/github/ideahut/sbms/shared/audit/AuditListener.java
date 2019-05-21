package com.github.ideahut.sbms.shared.audit;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import com.github.ideahut.sbms.shared.annotation.Auditable;
import com.github.ideahut.sbms.shared.entity.EntityBase;

public class AuditListener {
	
	@PrePersist
    public void onPrePersist(EntityBase<?> entity) {
		Auditable auditable = entity.getClass().getAnnotation(Auditable.class);
		if (auditable != null && auditable.value()) {
			AuditHolder.add("CREATE", entity);
		}
    }
      
    @PreUpdate
    public void onPreUpdate(EntityBase<?> entity) {
    	Auditable auditable = entity.getClass().getAnnotation(Auditable.class);
		if (auditable != null && auditable.value()) {
			AuditHolder.add("UPDATE", entity);
		}
    }
      
    @PreRemove
    public void onPreRemove(EntityBase<?> entity) {
    	Auditable auditable = entity.getClass().getAnnotation(Auditable.class);
		if (auditable != null && auditable.value()) {
			AuditHolder.add("DELETE", entity);
		}
    }
	
}
