package com.github.ideahut.sbms.shared.util;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public abstract class RequestUtil {
	
	/*
	 * GET REQUEST
	 */
	public static HttpServletRequest getRequest() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes == null) {
			return null;
		}
		return ((ServletRequestAttributes)requestAttributes).getRequest();
	}
	
	/*
	 * GET HEADER
	 */
	public static String getHeader(HttpServletRequest request, String name) {
		String value = request.getHeader(name);
		if (value == null) {
			value = request.getHeader(name.toLowerCase());
		}
		return value;
	}
	
	public static String getHeader(String name) {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}
		return getHeader(request, name);
	}
	
	public static String[] getHeaders(HttpServletRequest request, String name) {
		Enumeration<String> values = request.getHeaders(name);
		if (values == null) {
			values = request.getHeaders(name.toLowerCase());
		}
		return values != null ? Collections.list(values).toArray(new String[0]) : null;
	}
	
	public static String[] getHeaders(String name) {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}
		return getHeaders(request, name);
	}
	
}
