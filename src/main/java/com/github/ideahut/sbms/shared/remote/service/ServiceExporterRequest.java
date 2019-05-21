package com.github.ideahut.sbms.shared.remote.service;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.remoting.support.RemoteExporter;

public class ServiceExporterRequest {
	
	private final RemoteExporter exporter;
	
	private final Method method;
	
	private final Map<String, List<String>> headers;
	
	private final Map<String, Serializable> attributes;

	public ServiceExporterRequest(
		RemoteExporter exporter, 
		Method method, 
		Map<String, List<String>> headers, 
		Map<String, Serializable> attributes
	) {
		this.exporter = exporter;
		this.method = method;
		this.headers = headers != null ? headers : new HashMap<String, List<String>>();
		this.attributes = attributes != null ? attributes : new HashMap<String, Serializable>();
	}
	
	public RemoteExporter getExporter() {
		return exporter;
	}

	public Method getMethod() {
		return method;
	}

	public String[] getHeaders(String name) {
		List<String> values = headers.get(name);
		return values != null ? values.toArray(new String[0]) : null;
	}
	
	public String getHeader(String name) {
		List<String> values = headers.get(name);
		return values != null && values.size() != 0 ? values.get(0) : null;
	}

	public Serializable getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceExporterRequest [exporter=");
		builder.append(exporter);
		builder.append(", method=");
		builder.append(method);
		builder.append(", headers=");
		builder.append(headers);
		builder.append(", attributes=");
		builder.append(attributes);
		builder.append("]");
		return builder.toString();
	}
	
}
