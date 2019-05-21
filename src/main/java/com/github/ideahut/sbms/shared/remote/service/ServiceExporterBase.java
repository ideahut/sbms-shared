package com.github.ideahut.sbms.shared.remote.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
	
	private static final Class<?> _Hessian2Input;
	private static final Class<?> _HessianInput;
	private static final Class<?> _HessianRemoteResolver;
	private static final Class<?> _SerializerFactory;
	private static final Class<?> _AbstractSkeleton;
	private static final boolean isHessianAvailable;
	
	static {
		boolean hessianExist = false;
		Class<?>[] hessianClasses = new Class<?>[5];
		try {
			hessianClasses = new Class<?>[] {
				Class.forName("com.caucho.hessian.io.Hessian2Input"),
				Class.forName("com.caucho.hessian.io.HessianInput"),
				Class.forName("com.caucho.hessian.io.HessianRemoteResolver"),
				Class.forName("com.caucho.hessian.io.SerializerFactory"),
				Class.forName("com.caucho.services.server.AbstractSkeleton")
			};			
			hessianExist = true;
		} catch (Exception e) {
			hessianClasses = new Class<?>[5];
			hessianExist = false;
		}
		_Hessian2Input = hessianClasses[0];
		_HessianInput = hessianClasses[1];
		_HessianRemoteResolver = hessianClasses[2];
		_SerializerFactory = hessianClasses[3];
		_AbstractSkeleton = hessianClasses[4];
		isHessianAvailable = hessianExist;
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
					in = _Hessian2Input.getConstructor(InputStream.class).newInstance(is);
					_Hessian2Input.getMethod("readCall").invoke(in);
				}
				else if (code == 'C') {
					is.reset();
					in = _Hessian2Input.getConstructor(InputStream.class).newInstance(is);
					_Hessian2Input.getMethod("readCall").invoke(in);
				}
				else if (code == 'c') {
					major = is.read();
					minor = is.read();
					in = _HessianInput.getConstructor(InputStream.class).newInstance(is);
				}
				else {
					throw new IOException("Expected 'H'/'C' (Hessian 2.0) or 'c' (Hessian 1.0) in hessian input at " + code);
				}
				Field field = HessianExporter.class.getDeclaredField("serializerFactory");
				field.setAccessible(true);
				Object serializerFactory = field.get(exporter);
				field = HessianExporter.class.getDeclaredField("remoteResolver");
				field.setAccessible(true);
				Object remoteResolver = field.get(exporter);
				field = HessianExporter.class.getDeclaredField("skeleton");
				field.setAccessible(true);
				Object skeleton = field.get(exporter);
				
				Class<?> classIn = in.getClass(); 
				classIn.getMethod("setSerializerFactory", _SerializerFactory).invoke(in, serializerFactory);
				if (remoteResolver != null) {
					classIn.getMethod("setRemoteResolver", _HessianRemoteResolver).invoke(in, remoteResolver);
				}
				
				classIn.getMethod("skipOptionalCall").invoke(in);
				String methodName = (String)classIn.getMethod("readMethod").invoke(in);
			    int argLength = (int)classIn.getMethod("readMethodArgLength").invoke(in);
				
			    field = _AbstractSkeleton.getDeclaredField("_methodMap");
			    field.setAccessible(true);
			    @SuppressWarnings("rawtypes")
				HashMap map = (HashMap)field.get(skeleton);
			    		
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
			Method readRemoteInvocation = HttpInvokerServiceExporter.class.getDeclaredMethod("readRemoteInvocation", HttpServletRequest.class);
			readRemoteInvocation.setAccessible(true);
			RemoteInvocation remoteInvocation = (RemoteInvocation)readRemoteInvocation.invoke(exporter, request);
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

}
