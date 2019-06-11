package com.github.ideahut.sbms.shared.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

public class MetadataIntegrator implements Integrator {
	
	private Metadata metadata;
	
	private SessionFactoryImplementor sessionFactory;
	
	private ServiceRegistry serviceRegistry;
	
	private Map<String, Object> settings;
	
	private MetadataSources metadataSources;
	
	
	private MetadataIntegrator() {}
	
	
	public Metadata getMetadata() {
		return metadata;
	}

	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public Map<String, Object> getSettings() {
		return settings;
	}

	public Collection<Class<?>> getAnnotatedClasses() {
		return metadataSources != null ? metadataSources.getAnnotatedClasses() : null;
	}
	
	public Connection getConnection() throws SQLException {
		return sessionFactory
			.getSessionFactoryOptions()
			.getServiceRegistry()
			.getService(ConnectionProvider.class)
			.getConnection();
	}
	

	@Override
	public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
		this.metadata = metadata;
		this.sessionFactory = sessionFactory;		
	}
	
	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
		
	}
	
	private static MetadataIntegrator prepare(Map<String, Object> settings) {
		MetadataIntegrator integrator = new MetadataIntegrator();
		BootstrapServiceRegistry bootstrapServiceRegistry = 
			new BootstrapServiceRegistryBuilder()
			.enableAutoClose()
			.applyIntegrator(integrator)
			.build();

		StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder(bootstrapServiceRegistry)
			.applySettings(settings)
			.build();
		
		MetadataSources metadataSources = new MetadataSources(serviceRegistry);
		integrator.serviceRegistry = serviceRegistry;
		integrator.settings = settings;
		integrator.metadataSources = metadataSources;
		return integrator;
	}
	
	public static MetadataIntegrator create(Collection<Class<?>> annotatedClasses, Map<String, Object> settings) {
		MetadataIntegrator integrator = prepare(settings);
		for (Class<?> annotatedClass : annotatedClasses) {
			integrator.metadataSources.addAnnotatedClass(annotatedClass);
		}
		integrator.metadataSources.buildMetadata().buildSessionFactory();
		return integrator;
	}
	
	@SuppressWarnings("deprecation")
	public static MetadataIntegrator create(PlatformTransactionManager transactionManager) {
		Metamodel metamodel = null;
		Map<String, Object> settings = null;
		if (transactionManager instanceof JpaTransactionManager) {
			JpaTransactionManager jpaTransactionManager = (JpaTransactionManager)transactionManager;
			EntityManagerFactory entityManagerFactory = jpaTransactionManager.getEntityManagerFactory();
			settings = entityManagerFactory.getProperties();
			metamodel = entityManagerFactory.getMetamodel();
		}
		else if (transactionManager instanceof HibernateTransactionManager) {
			HibernateTransactionManager hibernateTransactionManager = (HibernateTransactionManager)transactionManager;
			SessionFactory sessionFactory = hibernateTransactionManager.getSessionFactory();
			settings = sessionFactory.getProperties();
			metamodel = sessionFactory.getMetamodel();
		}
		else {
			throw new RuntimeException("Unsupported Transaction Manager: " + transactionManager.getClass());
		}
		MetadataIntegrator integrator = prepare(settings);
		for (EntityType<?> entityType : metamodel.getEntities()) {
			integrator.metadataSources.addAnnotatedClass(entityType.getJavaType());
		}
		integrator.metadataSources.buildMetadata().buildSessionFactory();
		return integrator;
	}
	
	public static void destroy(MetadataIntegrator metadataIntegrator) {
		StandardServiceRegistryBuilder.destroy(metadataIntegrator.serviceRegistry);
	}

}
