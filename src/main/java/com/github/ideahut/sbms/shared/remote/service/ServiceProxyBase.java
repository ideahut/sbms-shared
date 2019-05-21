package com.github.ideahut.sbms.shared.remote.service;

import java.lang.reflect.Method;

import org.springframework.beans.factory.FactoryBean;

@SuppressWarnings("rawtypes")
public abstract class ServiceProxyBase {
	
	protected<P extends FactoryBean> P proxy(
		Class<P> factoryBeanClass, 
		Class<?> serviceInterface, 
		String serviceUrl,
		String username,
		String password) 
	{
		try {
			P proxy = factoryBeanClass.newInstance();
			factoryBeanClass.getMethod("setServiceInterface", Class.class).invoke(proxy, serviceInterface);
			factoryBeanClass.getMethod("setServiceUrl", String.class).invoke(proxy, serviceUrl);
			
			Method mtd = getMethod(factoryBeanClass, "setOverloadEnabled", boolean.class);
			if (null != mtd) {
				mtd.invoke(proxy, true);
			}
			//factoryBeanClass.getMethod("setDebug", boolean.class).invoke(proxy, true);
			if (null != username) {
				mtd = getMethod(factoryBeanClass, "setUsername", String.class);
				if (null != mtd) {
					mtd.invoke(proxy, username);
					String pswd = null != password ? password : "";
					mtd = getMethod(factoryBeanClass, "setPassword", String.class);
					if (null != mtd) {
						mtd.invoke(proxy, pswd);
					}
				}
			}			
			return proxy;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected<P extends FactoryBean> P proxy(
		Class<P> factoryBeanClass, 
		Class<?> serviceInterface, 
		String serviceUrl) 
	{
		return proxy(factoryBeanClass, serviceInterface, serviceUrl, null, null);
	}
	
	
	private<P extends FactoryBean> Method getMethod(Class<P> factoryBeanClass, String methodName, Class<?>...parameterTypes) {
		try {
			return factoryBeanClass.getMethod(methodName, parameterTypes);
		} catch (Exception e) {
			
		}
		return null;
	}
	
}
