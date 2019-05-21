package com.github.ideahut.sbms.shared.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

public class CORSFilter extends OncePerRequestFilter {
	
	@Override
	public void doFilterInternal(
			HttpServletRequest request, 
			HttpServletResponse response, 
			FilterChain filterChain
	) throws ServletException, IOException {
		Environment env = this.getEnvironment();
		response.setHeader("Access-Control-Allow-Origin", env.getProperty("cors.origin", "*"));
		response.setHeader("Access-Control-Allow-Credentials", env.getProperty("cors.credentials", "true"));
		response.setHeader("Access-Control-Allow-Methods", env.getProperty("cors.methods", "GET, POST, OPTIONS"));
		response.setHeader("Access-Control-Max-Age", env.getProperty("cors.maxage", "0").trim());
		response.setHeader("Access-Control-Allow-Headers", env.getProperty("cors.headers", ""));
		if("OPTIONS".equalsIgnoreCase(request.getMethod())){
			response.setStatus(HttpStatus.OK.value());
			return;
		}
		filterChain.doFilter(request, response);
	}
}