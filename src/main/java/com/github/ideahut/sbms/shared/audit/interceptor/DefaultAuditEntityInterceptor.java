package com.github.ideahut.sbms.shared.audit.interceptor;

import com.github.ideahut.sbms.shared.annotation.Auditable;
import com.github.ideahut.sbms.shared.entity.EntityBase;
import com.github.ideahut.sbms.shared.entity.EntityInterceptor;
import com.github.ideahut.sbms.shared.moment.MomentAttributes;
import com.github.ideahut.sbms.shared.moment.MomentHolder;

public class DefaultAuditEntityInterceptor implements EntityInterceptor {

	@Override
	public void onPrePersist(EntityBase<?> entity) {
		Auditable auditable = entity.getClass().getAnnotation(Auditable.class);
		if (auditable != null && auditable.value()) {
			MomentAttributes momentAttributes = MomentHolder.getMomentAttributes();
			if (momentAttributes != null) {
				momentAttributes.addAuditObject("CREATE", entity);
			}
		}
	}

	@Override
	public void onPreUpdate(EntityBase<?> entity) {
		Auditable auditable = entity.getClass().getAnnotation(Auditable.class);
		if (auditable != null && auditable.value()) {
			MomentAttributes momentAttributes = MomentHolder.getMomentAttributes();
			if (momentAttributes != null) {
				momentAttributes.addAuditObject("UPDATE", entity);
			}
		}
	}

	@Override
	public void onPreRemove(EntityBase<?> entity) {
		Auditable auditable = entity.getClass().getAnnotation(Auditable.class);
		if (auditable != null && auditable.value()) {
			MomentAttributes momentAttributes = MomentHolder.getMomentAttributes();
			if (momentAttributes != null) {
				momentAttributes.addAuditObject("DELETE", entity);
			}
		}
	}
	
}
