package com.github.ideahut.sbms.shared.remote.service.exporter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.remoting.support.RemoteInvocation;

import com.github.ideahut.sbms.client.dto.ResponseDto;
import com.github.ideahut.sbms.client.dto.ResponseDto.Status;
import com.github.ideahut.sbms.client.exception.ResponseException;
import com.github.ideahut.sbms.client.remote.RemoteMethodService;
import com.github.ideahut.sbms.shared.remote.RemoteMethodServiceImpl;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterCustom;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterHelper;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterInterceptor;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterRequest;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterResult;

public class RmiServiceExporter extends org.springframework.remoting.rmi.RmiServiceExporter implements ServiceExporterCustom {

	private List<ServiceExporterInterceptor> interceptors = new ArrayList<ServiceExporterInterceptor>();

	public void setInterceptors(List<ServiceExporterInterceptor> interceptors) {
		this.interceptors = interceptors != null ? interceptors : new ArrayList<ServiceExporterInterceptor>();
	}
	
	public void addInterceptor(ServiceExporterInterceptor interceptor) {
		if (!interceptors.contains(interceptor)) {
			interceptors.add(interceptor);
		}
	}
	
	@Override
	public void afterPropertiesSet() throws RemoteException {
		super.afterPropertiesSet();
		interceptors = ServiceExporterHelper.prepareInterceptors(interceptors);
		Object service = getService();
		if (service instanceof RemoteMethodServiceImpl) {
			RemoteMethodServiceImpl impl = (RemoteMethodServiceImpl) service;
			impl.setInterceptors(interceptors);
			impl.setRemoteExporter(this);
		}
	}

	@Override
	public boolean hasInterceptors() {
		return !interceptors.isEmpty();
	}

	@Override
	protected Object invoke(RemoteInvocation invocation, Object targetObject) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		try {
			ServiceExporterRequest serviceExporterRequest = null;
			boolean isIntercept = hasInterceptors() && !RemoteMethodService.class.equals(getServiceInterface());
			if (isIntercept) {
				Method method = getService().getClass().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
				serviceExporterRequest = new ServiceExporterRequest(this, method, invocation.getAttributes(), null);
				for (ServiceExporterInterceptor interceptor : interceptors) {
					ResponseDto responseDto = interceptor.preInvoke(serviceExporterRequest);
					if (responseDto != null && !Status.SUCCESS.equals(responseDto.getStatus())) {
						throw new ResponseException(responseDto);
					}
				}
			}
			Object value = super.invoke(invocation, targetObject);
			if (isIntercept) {
				for (ServiceExporterInterceptor interceptor : interceptors) {
					interceptor.postInvoke(serviceExporterRequest, new ServiceExporterResult(value));
				}
			}
			return value;
		} catch (Exception e) {
			throw new InvocationTargetException(e, e.getMessage());
		}
	}

		
	
}
