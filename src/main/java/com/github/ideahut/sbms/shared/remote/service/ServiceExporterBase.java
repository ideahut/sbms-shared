package com.github.ideahut.sbms.shared.remote.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.remoting.caucho.HessianExporter;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.util.Assert;
import org.springframework.web.method.HandlerMethod;

import com.github.ideahut.sbms.shared.wrapper.RequestWrapper;

public abstract class ServiceExporterBase {
	
	private static final ThreadLocal<RemoteExporter> exporterHolder = new ThreadLocal<RemoteExporter>();
	
	private static final boolean isHessianAvailable;
	private static final __HessianClasses__ HessianClasses;
	private static final __Hessian2Input__ Hessian2Input;
	private static final __HessianInput__ HessianInput;
	private static final __AbstractHessianInput__ AbstractHessianInput;
	private static final __HessianExporter__ HessianExporter;
	private static final __HttpInvokerServiceExporter__ HttpInvokerServiceExporter;
	
	static {
		boolean hessianExists = false;
		HessianClasses = new __HessianClasses__(); 
		try {
			HessianClasses.AbstractHessianInput = Class.forName("com.caucho.hessian.io.AbstractHessianInput");
			HessianClasses.Hessian2Input = Class.forName("com.caucho.hessian.io.Hessian2Input");
			HessianClasses.HessianInput = Class.forName("com.caucho.hessian.io.HessianInput");
			HessianClasses.HessianRemoteResolver = Class.forName("com.caucho.hessian.io.HessianRemoteResolver");
			HessianClasses.SerializerFactory = Class.forName("com.caucho.hessian.io.SerializerFactory");
			HessianClasses.AbstractSkeleton = Class.forName("com.caucho.services.server.AbstractSkeleton");
			hessianExists = true;
		} catch (Exception e) {
			hessianExists = false;
		}
		isHessianAvailable = hessianExists;
		
		HessianExporter = new __HessianExporter__();
		AbstractHessianInput = new __AbstractHessianInput__();
		Hessian2Input = new __Hessian2Input__();
		HessianInput = new __HessianInput__();
		
		if (isHessianAvailable) {
			try {
				HessianExporter.serializerFactory = HessianExporter.class.getDeclaredField("serializerFactory");
				HessianExporter.serializerFactory.setAccessible(true);
				HessianExporter.remoteResolver = HessianExporter.class.getDeclaredField("remoteResolver");
				HessianExporter.remoteResolver.setAccessible(true);
				HessianExporter.skeleton = HessianExporter.class.getDeclaredField("skeleton");
				HessianExporter.skeleton.setAccessible(true);
				HessianExporter._methodMap = HessianClasses.AbstractSkeleton.getDeclaredField("_methodMap");
				HessianExporter._methodMap.setAccessible(true);
				
				Hessian2Input.constructor = HessianClasses.Hessian2Input.getConstructor(InputStream.class);
				Hessian2Input.readCall = HessianClasses.Hessian2Input.getMethod("readCall");				
				
				HessianInput.constructor = HessianClasses.HessianInput.getConstructor(InputStream.class);				
				
				AbstractHessianInput.setSerializerFactory = HessianClasses.AbstractHessianInput.getMethod("setSerializerFactory", HessianClasses.SerializerFactory);
				AbstractHessianInput.setRemoteResolver = HessianClasses.AbstractHessianInput.getMethod("setRemoteResolver", HessianClasses.HessianRemoteResolver);
				AbstractHessianInput.skipOptionalCall = HessianClasses.AbstractHessianInput.getMethod("skipOptionalCall");
				AbstractHessianInput.readMethod = HessianClasses.AbstractHessianInput.getMethod("readMethod");
				AbstractHessianInput.readMethodArgLength = HessianClasses.AbstractHessianInput.getMethod("readMethodArgLength");
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		HttpInvokerServiceExporter = new __HttpInvokerServiceExporter__();
		try {
			HttpInvokerServiceExporter.readRemoteInvocation = HttpInvokerServiceExporter.class.getDeclaredMethod("readRemoteInvocation", HttpServletRequest.class);
			HttpInvokerServiceExporter.readRemoteInvocation.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	protected<E extends RemoteExporter, S> E export(
		Class<E> exporterClass, 
		Class<S> serviceInterface, 
		S serviceImplementation, 
		int registryPort,
		String serviceName
	) {
		try {
			E exporter = exporterClass.newInstance();
			exporter.setServiceInterface(serviceInterface);
			exporter.setService(serviceImplementation);
			
			//RMI
			if (registryPort > 0) {
				exporterClass.getMethod("setRegistryPort", int.class).invoke(exporter, registryPort);
				String nameService = null != serviceName ? serviceName.trim() : "";
				if (nameService == "") {
					nameService = exporterClass.getSimpleName();
				}
				exporterClass.getMethod("setServiceName", String.class).invoke(exporter, nameService);
			}
			return exporter;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected<E extends RemoteExporter, S> E export(
		Class<E> exporterClass,
		Class<S> serviceInterface, 
		S serviceImplementation, 
		int registryPort
	) {
		return export(exporterClass, serviceInterface, serviceImplementation, registryPort, null);				
	}
	
	protected<E extends RemoteExporter, S> E export(
		Class<E> exporterClass,
		Class<S> serviceInterface, 
		S serviceImplementation
	) {
		return export(exporterClass, serviceInterface, serviceImplementation, 0, null);
	}
	
	
	
	public static Method getInvocationMethod(Object handler, HttpServletRequest request) throws Exception {
		Method method = null;
		if (handler instanceof HandlerMethod) {
			method = ((HandlerMethod) handler).getMethod();
		} else if (isHessianAvailable && (handler instanceof HessianExporter)) {
			Assert.isInstanceOf(RequestWrapper.class, request);
			InputStream is = request.getInputStream();
			try {
				HessianExporter exporter = (HessianExporter)handler;
				if (!is.markSupported()) {
					is = new BufferedInputStream(is);
					is.mark(1);
				}
				
				int code = is.read();
				int major;
				int minor;
				
				Object in;
		
				if (code == 'H') {
					major = is.read();
					minor = is.read();
					if (major != 0x02) {
						throw new IOException("Version " + major + '.' + minor + " is not understood");
					}
					in = Hessian2Input.constructor.newInstance(is);
					Hessian2Input.readCall.invoke(is);
				}
				else if (code == 'C') {
					is.reset();
					in = Hessian2Input.constructor.newInstance(is);
					Hessian2Input.readCall.invoke(is);
				}
				else if (code == 'c') {
					major = is.read();
					minor = is.read();
					in = HessianInput.constructor.newInstance(is);
				}
				else {
					throw new IOException("Expected 'H'/'C' (Hessian 2.0) or 'c' (Hessian 1.0) in hessian input at " + code);
				}
				Object serializerFactory = HessianExporter.serializerFactory.get(exporter);
				Object remoteResolver = HessianExporter.remoteResolver.get(exporter);
				Object skeleton = HessianExporter.skeleton.get(exporter);
				
				AbstractHessianInput.setSerializerFactory.invoke(in, serializerFactory);
				if (remoteResolver != null) {
					AbstractHessianInput.setRemoteResolver.invoke(in, remoteResolver);
				}
				
				AbstractHessianInput.skipOptionalCall.invoke(in);
				
				String methodName = (String)AbstractHessianInput.readMethod.invoke(in);
			    int argLength = (int)AbstractHessianInput.readMethodArgLength.invoke(in);
				
			    @SuppressWarnings("rawtypes")
				HashMap map = (HashMap)HessianExporter._methodMap.get(skeleton);
			    		
			    method = (Method)map.get(methodName + "__" + argLength);
			    if (method == null) {
			    	method = (Method)map.get(methodName);
			    }
			} catch (Exception e) {
				throw e;
			} finally {
				try {
					is.close();
				} catch (Exception e) {}
			}
		} else if (handler instanceof HttpInvokerServiceExporter) {
			Assert.isInstanceOf(RequestWrapper.class, request);
			HttpInvokerServiceExporter exporter = (HttpInvokerServiceExporter)handler;
			RemoteInvocation remoteInvocation = (RemoteInvocation)HttpInvokerServiceExporter.readRemoteInvocation.invoke(exporter, request);
			method = exporter.getServiceInterface().getMethod(remoteInvocation.getMethodName(), remoteInvocation.getParameterTypes());
		}
		return method;
	}
	
	public static Map<String, List<String>> getRequestHeaders(HttpServletRequest request) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		Enumeration<String> en = request.getHeaderNames();
		while (en.hasMoreElements()) {
			String name = en.nextElement();
			Enumeration<String> values = request.getHeaders(name);
			if (values != null) {
				List<String> list = new ArrayList<String>();
				while (values.hasMoreElements()) {
					list.add(values.nextElement());
				}
				result.put(name, list);
			} else {
				result.put(name, null);
			}
		}
		return result;
	}
	
	public static void setRemoteExporter(RemoteExporter exporter) {
		exporterHolder.set(exporter);
	}
	
	public static RemoteExporter getRemoteExporter() {
		return exporterHolder.get();
	}
	
	public static void resetRemoteExporter() {
		exporterHolder.remove();
	}
	
	
	
	
	
	// Shadow classes :D
	
	private static class __HessianClasses__ {
		private Class<?> AbstractHessianInput;
		private Class<?> Hessian2Input;
		private Class<?> HessianInput;
		private Class<?> HessianRemoteResolver;
		private Class<?> SerializerFactory;
		private Class<?> AbstractSkeleton;
	}	
	private static class __HessianExporter__ {
		private Field serializerFactory;
		private Field remoteResolver;
		private Field skeleton;
		private Field _methodMap;
	}
	private static class __Hessian2Input__ {
		private Constructor<?> constructor;
		private Method readCall;			
	}
	private static class __HessianInput__ {
		private Constructor<?> constructor;
	}
	private static class __AbstractHessianInput__ {
		private Method setSerializerFactory;
		private Method setRemoteResolver;
		private Method skipOptionalCall;
		private Method readMethod;
		private Method readMethodArgLength;		
	}	
	private static class __HttpInvokerServiceExporter__ {
		private Method readRemoteInvocation;
	}

}
