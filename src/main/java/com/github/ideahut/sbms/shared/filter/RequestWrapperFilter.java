package com.github.ideahut.sbms.shared.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.github.ideahut.sbms.shared.wrapper.RequestWrapper;

public class RequestWrapperFilter extends OncePerRequestFilter {
	
	@Override
	public void doFilterInternal(
			HttpServletRequest request, 
			HttpServletResponse response, 
			FilterChain filterChain
	) throws ServletException, IOException {
		filterChain.doFilter(new RequestWrapper(request), response);
	}
}