package com.github.ideahut.sbms.shared.remote.cutom;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.caucho.hessian.client.HessianConnection;
import com.caucho.hessian.client.HessianProxy;
import com.caucho.hessian.io.HessianRemoteObject;

public class HessianProxyFactory extends com.caucho.hessian.client.HessianProxyFactory {
	
	private Map<String, String> headers = new HashMap<String, String>();
	
	public void setHeader(String key, String value) {
		headers.put(key, value);
	}
	
	public String getHeader(String key) {
		return headers.get(key);
	}
	
	public void removeHeader(String key) {
		headers.remove(key);
	}
	
	public Set<String> headerKeys() {
		return headers.keySet();
	}
	
	public void clearHeader() {
		headers.clear();
	}
	
	@Override
	public Object create(String url) throws MalformedURLException, ClassNotFoundException {
		throw new UnsupportedOperationException("Unsupported operation");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object create(Class api, String urlName) throws MalformedURLException {
		return create(api, urlName, Thread.currentThread().getContextClassLoader());
	}

	@Override
	public Object create(Class<?> api, String urlName, ClassLoader loader) throws MalformedURLException {
		URL url = new URL(urlName);	    
	    return create(api, url, loader);
	}

	@Override
	public Object create(Class<?> api, URL url, ClassLoader loader) {
		if (api == null) {
			throw new NullPointerException("api must not be null for HessianProxyFactory.create()");
		}
		InvocationHandler handler = new HeaderHessianProxy(url, this, api);
		return Proxy.newProxyInstance(loader, new Class[] { api, HessianRemoteObject.class }, handler);
	}	
	
	
	
	private class HeaderHessianProxy extends HessianProxy {		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7141419695713941106L;

		public HeaderHessianProxy(URL url, HessianProxyFactory factory, Class<?> type) {
			super(url, factory, type);
		}

		public HeaderHessianProxy(URL url, HessianProxyFactory factory) {
			super(url, factory);
		}
		
		@Override
		protected void addRequestHeaders(HessianConnection conn) {
			for (String key : headers.keySet()) {
				conn.addHeader(key, headers.get(key));
			}
			super.addRequestHeaders(conn); // replace dengan default
		}
		
	}
	
}
