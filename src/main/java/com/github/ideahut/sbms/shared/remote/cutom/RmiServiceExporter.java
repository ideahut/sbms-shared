package com.github.ideahut.sbms.shared.remote.cutom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.springframework.remoting.support.RemoteInvocation;

import com.github.ideahut.sbms.client.dto.CodeMessageDto;
import com.github.ideahut.sbms.client.dto.ResponseDto;
import com.github.ideahut.sbms.client.dto.ResponseDto.Status;
import com.github.ideahut.sbms.client.exception.ResponseException;
import com.github.ideahut.sbms.client.service.RemoteMethodService;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterBase;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterInterceptor;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterRequest;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterResult;

public class RmiServiceExporter extends org.springframework.remoting.rmi.RmiServiceExporter {

	private ServiceExporterInterceptor interceptor;

	public RmiServiceExporter setInterceptor(ServiceExporterInterceptor interceptor) {
		this.interceptor = interceptor;
		return this;
	}

	@Override
	protected Object invoke(RemoteInvocation invocation, Object targetObject) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		ServiceExporterBase.setRemoteExporter(this);
		ServiceExporterRequest serviceExporterRequest = null;
		boolean isIntercept = interceptor != null && !RemoteMethodService.class.equals(getServiceInterface());
		if (isIntercept) {
			Method method = getServiceInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
			serviceExporterRequest = new ServiceExporterRequest(this, method, null, invocation.getAttributes());
			ResponseDto responseDto = interceptor.beforeInvoke(serviceExporterRequest);
			if (responseDto != null && !Status.SUCCESS.equals(responseDto.getStatus())) {
				List<CodeMessageDto> errors = responseDto.getError();
				CodeMessageDto cmsg = errors != null && errors.size() != 0 ? errors.get(0) : new CodeMessageDto("99", "Interceptor error");
				throw new InvocationTargetException(new ResponseException(responseDto), "RME::" + cmsg.getCode() + "-" + cmsg.getMessage());
			}			
		}
		Object value = super.invoke(invocation, targetObject);
		if (isIntercept) {
			interceptor.afterInvoke(serviceExporterRequest, new ServiceExporterResult(value));
		}
		return value;
	}

		
	
}
