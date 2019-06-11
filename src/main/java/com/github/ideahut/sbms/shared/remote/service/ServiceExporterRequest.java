package com.github.ideahut.sbms.shared.remote.service;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.remoting.support.RemoteExporter;

public class ServiceExporterRequest {
	
	private final RemoteExporter exporter;
	
	private final Method method;
	
	private final HttpServletRequest request;
	
	private final Map<String, Serializable> attributes;

	public ServiceExporterRequest(
		RemoteExporter exporter, 
		Method method, 
		Map<String, Serializable> attributes,
		HttpServletRequest request
	) {
		this.exporter = exporter;
		this.method = method;
		this.attributes = attributes != null ? attributes : new HashMap<String, Serializable>();
		this.request = request;
	}
	
	public RemoteExporter getExporter() {
		return exporter;
	}

	public Method getMethod() {
		return method;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public Serializable getAttribute(String name) {
		return attributes.get(name);
	}
	
}
