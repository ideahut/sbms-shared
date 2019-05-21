package com.github.ideahut.sbms.shared.remote;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.ideahut.sbms.client.dto.RemoteMethodDto;
import com.github.ideahut.sbms.client.dto.ResponseDto;
import com.github.ideahut.sbms.client.dto.ResponseDto.Status;
import com.github.ideahut.sbms.client.exception.ResponseException;
import com.github.ideahut.sbms.client.service.RemoteMethodService;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterBase;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterInterceptor;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterRequest;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterResult;

public class RemoteMethodServiceImpl implements RemoteMethodService, InitializingBean {
	
	private Map<Class<?>, Map<String, Method>> classMethods = new HashMap<Class<?>, Map<String,Method>>();
	
	private ApplicationContext applicationContext;
	
	private ServiceExporterInterceptor interceptor;
	
	private List<Class<?>> serviceInterfaces = new ArrayList<Class<?>>();
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	public void setInterceptor(ServiceExporterInterceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public void setServiceInterfaces(List<Class<?>> serviceInterfaces) {
		this.serviceInterfaces = serviceInterfaces != null ? serviceInterfaces : new ArrayList<Class<?>>();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		serviceInterfaces.remove(RemoteMethodService.class);
		classMethods.clear();
		for (Class<?> clazz : serviceInterfaces) {
			Map<String, Method> methodMap = new HashMap<String, Method>();
			for (Method m : clazz.getMethods()) {
				methodMap.put(m.getName() + "__" + m.getParameterCount(), m);
			}
			classMethods.put(clazz, methodMap);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R call(Class<R> returnType, RemoteMethodDto dto) throws ResponseException {
		Class<?> serviceClass = dto.getServiceClass();
		if (serviceClass == null) {
			throw new ResponseException(ResponseDto.ERROR("RME01", "Service class is required"));
		}
		if (RemoteMethodService.class.equals(serviceClass)) {
			throw new ResponseException(ResponseDto.ERROR("RME02", "Invalid service class"));
		}
		if (serviceInterfaces.indexOf(serviceClass) == -1) {
			throw new ResponseException(ResponseDto.ERROR("RME03", "Service class is not registered"));
		}
		Object serviceObject = applicationContext.getBean(serviceClass);
		if (null == serviceObject) {
			throw new ResponseException(ResponseDto.ERROR("RME04", "Service object is not found"));
		}
		Method serviceMethod = getMethod(dto);
		if (null == serviceMethod) {
			throw new ResponseException(ResponseDto.ERROR("RME09", "Method is not found"));
		}
		Class<?> smReturnType = serviceMethod.getReturnType();
		boolean isVoid = void.class.equals(smReturnType) || Void.class.equals(smReturnType);
		if ((isVoid && null != returnType) || (returnType != null && !returnType.equals(smReturnType))) {
			throw new ResponseException(ResponseDto.ERROR("RME10", "Invalid return type"));
		}
		ServiceExporterRequest request = null;
		if (null != interceptor) {
			DummyExporter exporter = new DummyExporter();
			exporter.setService(serviceObject);
			exporter.setServiceInterface(serviceClass);
			Map<String, List<String>> headers = getHeaders();
			request = new ServiceExporterRequest(exporter, serviceMethod, headers, dto.getAttributes());
			ResponseDto responseDto = interceptor.beforeInvoke(request);
			if (responseDto != null && !Status.SUCCESS.equals(responseDto.getStatus())) {
				throw new ResponseException(responseDto);
			}
		}
		ServiceExporterResult result = null;
		try {
			Object[] arguments =  Arrays.asList(dto.getArguments()).toArray(new Object[0]);
			Object value = serviceMethod.invoke(serviceObject, arguments);
			result = new ServiceExporterResult(value);
		} catch (Exception e) {
			result = new ServiceExporterResult(e);			
		}
		if (null != interceptor) {
			interceptor.afterInvoke(request, result);
		}
		if (result.getException() != null) {
			throw new ResponseException(ResponseDto.ERROR("RME11", result.getException().getMessage()));
		}
		return (R)result.getValue();
	}
	
	private Method getMethod(RemoteMethodDto dto) throws ResponseException {
		String methodName = dto.getMethodName() != null ? dto.getMethodName().trim() : "";
		if (methodName.length() == 0) {
			throw new ResponseException(ResponseDto.ERROR("RME05", "Method name is required"));
		}
		if (dto.getParameterTypes() != null) {
			try {
				return dto.getServiceClass().getMethod(dto.getMethodName(), dto.getParameterTypes());
			} catch (NoSuchMethodException e) {
				throw new ResponseException(ResponseDto.ERROR("RME06", "Method is not found"));
			} catch (SecurityException e) {
				throw new ResponseException(ResponseDto.ERROR("RME07", "Invalid method"));
			}
		}
		Map<String, Method> methodMap = classMethods.get(dto.getServiceClass());
		if (null == methodMap) {
			throw new ResponseException(ResponseDto.ERROR("RME08", "Service class method is indefined"));
		}
		return methodMap.get(methodName + "__" + dto.getArguments().length);
	}
	
	private Map<String, List<String>> getHeaders() {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			return null;
		}
		HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
		if (request == null) {
			return null;
		}
		Map<String, List<String>> result = ServiceExporterBase.getRequestHeaders(request);
		return result;
	}
	
	private class DummyExporter extends RemoteExporter  {
		
	}
	
}
