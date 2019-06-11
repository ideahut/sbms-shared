package com.github.ideahut.sbms.shared.remote.service.exporter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.util.NestedServletException;

import com.github.ideahut.sbms.client.dto.CodeMessageDto;
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

public class HessianServiceExporter extends org.springframework.remoting.caucho.HessianServiceExporter implements ServiceExporterCustom {
	
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
	public void afterPropertiesSet() {
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
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!"POST".equals(request.getMethod())) {
			throw new HttpRequestMethodNotSupportedException(
				request.getMethod(), 
				new String[] {"POST"}, 
				"HessianServiceExporter only supports POST requests"
			);
		}
		response.setContentType(CONTENT_TYPE_HESSIAN);
		try {
			ServiceExporterRequest serviceExporterRequest = null;
			boolean isIntercept = hasInterceptors() && !RemoteMethodService.class.equals(getServiceInterface());
			if (isIntercept) {
				Method method = ServiceExporterHelper.getMethod(this, request);
				method = getService().getClass().getMethod(method.getName(), method.getParameterTypes());
				serviceExporterRequest = new ServiceExporterRequest(this, method, null, request);
				for (ServiceExporterInterceptor interceptor : interceptors) {
					ResponseDto responseDto = interceptor.preInvoke(serviceExporterRequest);
					if (responseDto != null && !Status.SUCCESS.equals(responseDto.getStatus())) {
						throw new ResponseException(responseDto);
					}
				}
			}
			super.invoke(request.getInputStream(), response.getOutputStream());
			if (isIntercept) {
				for (ServiceExporterInterceptor interceptor : interceptors) {
					interceptor.postInvoke(serviceExporterRequest, new ServiceExporterResult(true));
				}				
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
