package com.github.ideahut.sbms.shared.remote.service;

import com.github.ideahut.sbms.client.dto.ResponseDto;

public interface ServiceExporterInterceptor {

	public ResponseDto preInvoke(ServiceExporterRequest request) throws Exception;
	
	public void postInvoke(ServiceExporterRequest request, ServiceExporterResult result) throws Exception;
	
}
