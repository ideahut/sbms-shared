package com.github.ideahut.sbms.shared.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class MDCFilter extends OncePerRequestFilter {
	
	@Override
	public void doFilterInternal(
			HttpServletRequest request, 
			HttpServletResponse response, 
			FilterChain filterChain
	) throws ServletException, IOException {
		try { 
			MDC.put("traceId", RandomStringUtils.randomAlphanumeric(6));
			filterChain.doFilter(request, response); 
		} finally { 
			MDC.clear(); 
		}		 
	}
}