package com.github.ideahut.sbms.shared.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.github.ideahut.sbms.client.remote.RemoteMethodService;
import com.github.ideahut.sbms.shared.audit.AuditExecutor;
import com.github.ideahut.sbms.shared.entity.EntityInterceptor;
import com.github.ideahut.sbms.shared.moment.MomentAttributes;
import com.github.ideahut.sbms.shared.moment.MomentHolder;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterCustom;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterHelper;

public abstract class BaseHandlerInterceptor extends HandlerInterceptorAdapter implements InitializingBean {

	private AuditExecutor auditExecutor;
	
	private List<Class<?>> ignoredHandlerClasses = new ArrayList<Class<?>>();
	
	private List<EntityInterceptor> entityInterceptors = new ArrayList<EntityInterceptor>();
	
	
	public void setAuditExecutor(AuditExecutor auditExecutor) {
		this.auditExecutor = auditExecutor;
	}

	public void setIgnoredHandlerClasses(List<Class<?>> ignoredHandlerClasses) {
		this.ignoredHandlerClasses = ignoredHandlerClasses != null ? ignoredHandlerClasses : new ArrayList<Class<?>>();
	}
	
	public void addIgnoredHandlerClasses(Class<?> ignoredHandlerClass) {
		ignoredHandlerClasses.add(ignoredHandlerClass);
	}
	
	public void setEntityInterceptors(List<EntityInterceptor> entityInterceptors) {
		this.entityInterceptors = entityInterceptors != null ? entityInterceptors : new ArrayList<EntityInterceptor>();
	}
	
	public void addEntityInterceptor(EntityInterceptor entityInterceptor) {
		entityInterceptors.add(entityInterceptor);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		List<Class<?>> tempIgnoredHandlerClasses = new ArrayList<Class<?>>(ignoredHandlerClasses);
		ignoredHandlerClasses.clear();
		while (!tempIgnoredHandlerClasses.isEmpty()) {
			Class<?> clazz = tempIgnoredHandlerClasses.remove(0);
			if (ignoredHandlerClasses.contains(clazz))continue;
			ignoredHandlerClasses.add(clazz);
		}
		tempIgnoredHandlerClasses = null;
		
		List<EntityInterceptor> tempEntityInterceptors = new ArrayList<EntityInterceptor>(entityInterceptors);
		entityInterceptors.clear();
		while (!tempEntityInterceptors.isEmpty()) {
			EntityInterceptor interceptor = tempEntityInterceptors.remove(0);
			if (entityInterceptors.contains(interceptor)) continue;
			entityInterceptors.add(interceptor);
		}
		tempEntityInterceptors = null;
		
		if (!ignoredHandlerClasses.contains(BasicErrorController.class)) {
			ignoredHandlerClasses.add(BasicErrorController.class);
		}
		if (!ignoredHandlerClasses.contains(RemoteMethodService.class)) {
			ignoredHandlerClasses.add(RemoteMethodService.class);
		}
		
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		if (handler instanceof ServiceExporterCustom && ((ServiceExporterCustom)handler).hasInterceptors()) {
			return true;
		}
		
		Method method = ServiceExporterHelper.getMethod(handler, request);
		if (method == null || ignoredHandlerClasses.contains(method.getDeclaringClass())) {
			return true;
		}
		if (handler instanceof RemoteExporter) {
			RemoteExporter remoteExporter = (RemoteExporter)handler;
			method = remoteExporter.getService().getClass().getMethod(method.getName(), method.getParameterTypes());
		}
		MomentAttributes momentAttributes = new MomentAttributes();
		momentAttributes.setEntityInterceptors(entityInterceptors);
		MomentHolder.setMomentAttributes(momentAttributes);
		try {
			boolean success = beforeHandle(request, method);
			if (!success) {
				MomentHolder.removeMomentAttributes();
			}
			return success;
		} catch (Exception e) {
			MomentHolder.removeMomentAttributes();
			throw e;
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		try {
			afterHandle(request, modelAndView);
		} finally {
			if (auditExecutor != null) {
				auditExecutor.run();
			}
			MomentHolder.removeMomentAttributes();		
		}
	}
	
	public abstract boolean beforeHandle(HttpServletRequest request, Method method) throws Exception;
	
	public abstract void afterHandle(HttpServletRequest request, ModelAndView modelAndView) throws Exception;
	
}
