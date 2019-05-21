package com.github.ideahut.sbms.shared.remote.cutom;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestMethodNotSupportedException;
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

public class HessianServiceExporter extends org.springframework.remoting.caucho.HessianServiceExporter {

	private ServiceExporterInterceptor interceptor;

	public HessianServiceExporter setInterceptor(ServiceExporterInterceptor interceptor) {
		this.interceptor = interceptor;
		return this;
	}
	
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!"POST".equals(request.getMethod())) {
			throw new HttpRequestMethodNotSupportedException(request.getMethod(), new String[] {"POST"}, "HessianServiceExporter only supports POST requests");
		}
		ServiceExporterBase.setRemoteExporter(this);
		response.setContentType(CONTENT_TYPE_HESSIAN);
		try {
			ServiceExporterRequest serviceExporterRequest = null;
			boolean isIntercept = interceptor != null && !RemoteMethodService.class.equals(getServiceInterface());
			if (isIntercept) {
				Method method = ServiceExporterBase.getInvocationMethod(this, request);
				Map<String, List<String>> headers = ServiceExporterBase.getRequestHeaders(request);
				serviceExporterRequest = new ServiceExporterRequest(this, method, headers, null);
				ResponseDto responseDto = interceptor.beforeInvoke(serviceExporterRequest);
				if (responseDto != null && !Status.SUCCESS.equals(responseDto.getStatus())) {
					throw new ResponseException(responseDto);
				}
			}
			invoke(request.getInputStream(), response.getOutputStream());
			if (isIntercept) {
				interceptor.afterInvoke(serviceExporterRequest, new ServiceExporterResult(true));
			}
		} catch (ResponseException ex) {
			ResponseDto dto = ex.getResponse();
			List<CodeMessageDto> error = dto != null ? dto.getError() : null;
			CodeMessageDto cmsg = error != null && error.size() != 0 ? error.get(0) : new CodeMessageDto("99", ex.getMessage());
			throw new NestedServletException("RME::" + cmsg.getCode() + "-" + cmsg.getMessage(), ex);
		} catch (Throwable ex) {
			throw new NestedServletException("Hessian skeleton invocation failed", ex);
		}
	}
	
}
