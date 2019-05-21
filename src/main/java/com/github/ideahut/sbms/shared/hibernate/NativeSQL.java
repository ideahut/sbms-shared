package com.github.ideahut.sbms.shared.hibernate;

public class NativeSQL {
	
	private String query;
	
	private Object[] parameters;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
	
}
