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

import com.github.ideahut.sbms.client.dto.ResponseDto;
import com.github.ideahut.sbms.client.dto.ResponseDto.Status;
import com.github.ideahut.sbms.client.exception.ResponseException;
import com.github.ideahut.sbms.client.remote.RemoteMethodParameter;
import com.github.ideahut.sbms.client.remote.RemoteMethodService;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterInterceptor;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterRequest;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterResult;
import com.github.ideahut.sbms.shared.util.RequestUtil;

public class RemoteMethodServiceImpl implements RemoteMethodService, InitializingBean {
	
	private final Map<Class<?>, Map<String, MethodMap>> serviceMethods = new HashMap<Class<?>, Map<String, MethodMap>>();
	
	
	private List<ServiceExporterInterceptor> interceptors = new ArrayList<ServiceExporterInterceptor>();
	
	private List<Class<?>> serviceInterfaces = new ArrayList<Class<?>>();
	
	private ApplicationContext applicationContext;
	
	private RemoteExporter remoteExporter;
	
	
	public void setInterceptors(List<ServiceExporterInterceptor> interceptors) {
		this.interceptors = interceptors != null ? interceptors : new ArrayList<ServiceExporterInterceptor>();
	}

	public void setServiceInterfaces(List<Class<?>> serviceInterfaces) {
		this.serviceInterfaces = serviceInterfaces != null ? serviceInterfaces : new ArrayList<Class<?>>();
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setRemoteExporter(RemoteExporter remoteExporter) {
		this.remoteExporter = remoteExporter;
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		serviceInterfaces.remove(RemoteMethodService.class);
		serviceMethods.clear();		
		for (Class<?> serviceInterface : serviceInterfaces) {
			if (serviceMethods.containsKey(serviceInterface)) continue;
			Object serviceObject = applicationContext.getBean(serviceInterface);
			Map<String, MethodMap> mapByName = new HashMap<String, MethodMap>();
			for (Method m : serviceInterface.getMethods()) {
				Method method = serviceObject.getClass().getMethod(m.getName(), m.getParameterTypes());
				MethodMap methodMap = mapByName.get(method.getName());
				if (methodMap == null) {
					mapByName.put(method.getName(), new MethodMap());
					methodMap = mapByName.get(method.getName());					
				}
				methodMap.byArguments.put(method.getParameterCount(), method);
				methodMap.byParameters.put(getParameterTypesName(method.getParameterTypes()), method);
				methodMap.serviceObject = serviceObject;
			}
			serviceMethods.put(serviceInterface, mapByName);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public<R> R call(Class<R> returnType, RemoteMethodParameter parameter) throws ResponseException {
		Class<?> serviceClass = parameter.getServiceClass();
		if (serviceClass == null) {
			throw new ResponseException(ResponseDto.ERROR("RME01", "Service class is required"));
		}
		
		if (RemoteMethodService.class.equals(serviceClass)) {
			throw new ResponseException(ResponseDto.ERROR("RME02", "Invalid service class"));
		}		
		
		String methodName = parameter.getMethodName();
		methodName = methodName != null ? methodName.trim() : "";
		if (methodName.isEmpty()) {
			throw new ResponseException(ResponseDto.ERROR("RME03", "Method name is required"));
		}		
		
		Map<String, MethodMap> mapByMethodName = serviceMethods.get(serviceClass);
		if (mapByMethodName == null) {
			throw new ResponseException(ResponseDto.ERROR("RME04", "Service class is not registered"));
		}
		
		MethodMap methodMap = mapByMethodName.get(methodName);
		if (methodMap == null) {
			throw new ResponseException(ResponseDto.ERROR("RME05", "Method name is not found"));
		}
		
		Method serviceMethod = getServiceMethod(methodMap, parameter);
		if (serviceMethod == null) {
			throw new ResponseException(ResponseDto.ERROR("RME06", "Service method is not found"));
		}
		
		Class<?> smReturnType = serviceMethod.getReturnType();
		boolean isVoid = void.class.equals(smReturnType) || Void.class.equals(smReturnType);
		if ((isVoid && null != returnType) || (returnType != null && !returnType.equals(smReturnType))) {
			throw new ResponseException(ResponseDto.ERROR("RME07", "Invalid return type"));
		}
		
		ServiceExporterRequest request = null;
		boolean isIntercept = !interceptors.isEmpty();
		if (isIntercept) {
			HttpServletRequest httpRequest = RequestUtil.getRequest();
			request = new ServiceExporterRequest(remoteExporter, serviceMethod, parameter.getAttributes(), httpRequest);
			try {
				for (ServiceExporterInterceptor interceptor : interceptors) {
					ResponseDto responseDto = interceptor.preInvoke(request);
					if (responseDto != null && !Status.SUCCESS.equals(responseDto.getStatus())) {
						throw new ResponseException(responseDto);
					}
				}
			} catch (Exception e) {
				throw new ResponseException(ResponseDto.ERROR("RME08", e.getMessage()));
			}
		}
		ServiceExporterResult result = null;
		try {
			Object[] arguments =  Arrays.asList(parameter.getArguments()).toArray(new Object[0]);
			Object value = serviceMethod.invoke(methodMap.serviceObject, arguments);
			result = new ServiceExporterResult(value);
		} catch (Exception e) {
			result = new ServiceExporterResult(e);			
		}
		if (isIntercept) {
			try {
				for (ServiceExporterInterceptor interceptor : interceptors) {
					interceptor.postInvoke(request, result);
				}
			} catch (Exception e) {
				throw new ResponseException(ResponseDto.ERROR("RME08", e.getMessage()));
			}
		}
		if (result.getException() != null) {
			throw new ResponseException(ResponseDto.ERROR("RME08", result.getException().getMessage()));
		}
		return (R)result.getValue();
	}
	
	private String getParameterTypesName(Class<?>[] parameterTypes) {
		StringBuilder result = new StringBuilder("_");
		for (Class<?> type : parameterTypes) {
			result.append(type.getName()).append("_");
		}
		return result.toString();
	}
	
	private Method getServiceMethod(MethodMap methodMap, RemoteMethodParameter parameter) {
		if (parameter.getParameterTypes() != null) {
			String name = getParameterTypesName(parameter.getParameterTypes());
			return methodMap.byParameters.get(name);
		} else {
			return methodMap.byArguments.get(parameter.getArguments().length);
		}
	}
	
	private class MethodMap {
		private Object serviceObject;
		private Map<Integer, Method> byArguments = new HashMap<Integer, Method>();
		private Map<String, Method> byParameters = new HashMap<String, Method>();
	}
	
}
