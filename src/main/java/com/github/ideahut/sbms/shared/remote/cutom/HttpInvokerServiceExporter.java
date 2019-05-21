package com.github.ideahut.sbms.shared.remote.cutom;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.web.util.NestedServletException;

import com.github.ideahut.sbms.client.dto.CodeMessageDto;
import com.github.ideahut.sbms.client.dto.ResponseDto;
import com.github.ideahut.sbms.client.dto.ResponseDto.Status;
import com.github.ideahut.sbms.client.exception.ResponseException;
import com.github.ideahut.sbms.client.service.RemoteMethodService;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterBase;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterInterceptor;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterRequest;
import com.github.ideahut.sbms.shared.remote.service.ServiceExporterResult;

public class HttpInvokerServiceExporter extends org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter {
	
	private ServiceExporterInterceptor interceptor;

	public HttpInvokerServiceExporter setInterceptor(ServiceExporterInterceptor interceptor) {
		this.interceptor = interceptor;
		return this;
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServiceExporterBase.setRemoteExporter(this);
		try {
			RemoteInvocation invocation = readRemoteInvocation(request);
			ServiceExporterRequest serviceExporterRequest = null;
			boolean isIntercept = interceptor != null && !RemoteMethodService.class.equals(getServiceInterface());
			if (isIntercept) {
				Map<String, List<String>> headers = ServiceExporterBase.getRequestHeaders(request);			
				Method method = getServiceInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
				serviceExporterRequest = new ServiceExporterRequest(this, method, headers, invocation.getAttributes());
				ResponseDto responseDto = interceptor.beforeInvoke(serviceExporterRequest);
				if (responseDto != null && !Status.SUCCESS.equals(responseDto.getStatus())) {
					throw new ResponseException(responseDto);
				}
			}
			RemoteInvocationResult result = invokeAndCreateResult(invocation, getProxy());
			if (isIntercept) {
				interceptor.afterInvoke(serviceExporterRequest, new ServiceExporterResult(result.hasException() ? result.getException() : result.getValue()));
			}
			writeRemoteInvocationResult(request, response, result);
		} catch (ClassNotFoundException ex) {
			throw new NestedServletException("Class not found during deserialization", ex);
		} catch (ResponseException ex) {
			ResponseDto dto = ex.getResponse();
			List<CodeMessageDto> error = dto != null ? dto.getError() : null;
			CodeMessageDto cmsg = error != null && error.size() != 0 ? error.get(0) : new CodeMessageDto("99", ex.getMessage());
			throw new NestedServletException("RME::" + cmsg.getCode() + "-" + cmsg.getMessage(), ex);
		} catch (Exception ex) {
			throw new NestedServletException("Error during execution", ex);
		} 
	}
	
}
