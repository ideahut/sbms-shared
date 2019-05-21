package com.github.ideahut.sbms.shared.remote.service;

import com.github.ideahut.sbms.client.dto.ResponseDto;

public interface ServiceExporterInterceptor {

	public ResponseDto beforeInvoke(ServiceExporterRequest request);
	
	public void afterInvoke(ServiceExporterRequest request, ServiceExporterResult result);
	
}
