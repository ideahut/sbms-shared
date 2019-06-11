package com.github.ideahut.sbms.shared.remote.service;

import org.springframework.remoting.support.RemoteExporter;

public abstract class ServiceExporterBase {
		
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

}
