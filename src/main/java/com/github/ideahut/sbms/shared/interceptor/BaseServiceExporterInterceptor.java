package com.github.ideahut.sbms.shared.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import com.github.ideahut.sbms.client.dto.ResponseDto;
import com.github.ideahut.sbms.client.dto.ResponseDto.Status;
import com.github.ideahut.sbms.shared.audit.AuditExecutor;
import com.github.ideahut.sbms.shared.entity.EntityInterceptor;
import com.github.ideahut.sbms.shared.moment.MomentAttributes;
import com.github.ideahut.sbms.shared.moment.MomentHolder;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterInterceptor;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterRequest;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterResult;

public abstract class BaseServiceExporterInterceptor implements ServiceExporterInterceptor, InitializingBean {
	
	private AuditExecutor auditExecutor;
	
	private List<EntityInterceptor> entityInterceptors = new ArrayList<EntityInterceptor>();
	
	
	public void setAuditExecutor(AuditExecutor auditExecutor) {
		this.auditExecutor = auditExecutor;
	}
	
	public void setEntityInterceptors(List<EntityInterceptor> entityInterceptors) {
		this.entityInterceptors = entityInterceptors != null ? entityInterceptors : new ArrayList<EntityInterceptor>();
	}
	
	public void addEntityInterceptor(EntityInterceptor entityInterceptor) {
		entityInterceptors.add(entityInterceptor);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		List<EntityInterceptor> tempEntityInterceptors = new ArrayList<EntityInterceptor>(entityInterceptors);
		entityInterceptors.clear();
		while (!tempEntityInterceptors.isEmpty()) {
			EntityInterceptor interceptor = tempEntityInterceptors.remove(0);
			if (entityInterceptors.contains(interceptor)) continue;
			entityInterceptors.add(interceptor);
		}
		tempEntityInterceptors = null;		
	}

	@Override
	public ResponseDto preInvoke(ServiceExporterRequest request) throws Exception {
		MomentAttributes momentAttributes = new MomentAttributes();
		momentAttributes.setEntityInterceptors(entityInterceptors);
		MomentHolder.setMomentAttributes(momentAttributes);
		
		try {
			ResponseDto result = beforeInvoke(request);
			if (result != null && !Status.SUCCESS.equals(result.getStatus())) {
				MomentHolder.removeMomentAttributes();
			}		
			return result;
		} catch (Exception e) {
			MomentHolder.removeMomentAttributes();
			throw e;
		}
	}

	@Override
	public void postInvoke(ServiceExporterRequest request, ServiceExporterResult result) throws Exception {
		try {
			afterInvoke(request, result);
		} finally {
			if (auditExecutor != null) {
				auditExecutor.run();
			}
			MomentHolder.removeMomentAttributes();		
		}
	}
	
	public abstract ResponseDto beforeInvoke(ServiceExporterRequest request) throws Exception;
	
	public abstract void afterInvoke(ServiceExporterRequest request, ServiceExporterResult result) throws Exception;
	
}
