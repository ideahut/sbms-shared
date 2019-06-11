package com.github.ideahut.sbms.shared.entity;

import java.util.List;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import com.github.ideahut.sbms.shared.moment.MomentAttributes;
import com.github.ideahut.sbms.shared.moment.MomentHolder;

public class EntityListener {
	
	@PrePersist
    public void onPrePersist(EntityBase<?> entity) {
		List<EntityInterceptor> interceptors = getInterceptors();
		if (interceptors == null) return;
		for (EntityInterceptor interceptor : interceptors) {
			interceptor.onPrePersist(entity);
		}
    }
      
    @PreUpdate
    public void onPreUpdate(EntityBase<?> entity) {
    	List<EntityInterceptor> interceptors = getInterceptors();
    	if (interceptors == null) return;
		for (EntityInterceptor interceptor : interceptors) {
			interceptor.onPreUpdate(entity);
		}
    }
      
    @PreRemove
    public void onPreRemove(EntityBase<?> entity) {
    	List<EntityInterceptor> interceptors = getInterceptors();
    	if (interceptors == null) return;
		for (EntityInterceptor interceptor : interceptors) {
			interceptor.onPreRemove(entity);
		}
    }
    
    private List<EntityInterceptor> getInterceptors() {
    	MomentAttributes momentAttributes = MomentHolder.getMomentAttributes();
    	if (momentAttributes == null) {
    		return null;
    	}
    	return momentAttributes.getEntityInterceptors();
    }
    
}
