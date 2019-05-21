package com.github.ideahut.sbms.shared.audit;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.github.ideahut.sbms.shared.annotation.Auditable;
import com.github.ideahut.sbms.shared.entity.optional.Audit;
import com.github.ideahut.sbms.shared.repo.optional.AuditRepository;

public class AuditExecutorImpl implements AuditExecutor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditExecutorImpl.class);
	
	private static final int THREADS = Runtime.getRuntime().availableProcessors();	
	
	private static final ExecutorService executorService = 
			new ThreadPoolExecutor(1, THREADS, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	
	private final ObjectMapper objectMapper;
	
	
	private AuditRepository auditRepository;
	
	private boolean asynchronous = true;
	
	private ContentType contentType = ContentType.STRING;
		
	
	@SuppressWarnings("serial")
	public AuditExecutorImpl() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
			@Override
			public boolean hasIgnoreMarker(AnnotatedMember m) {
				Auditable auditable = _findAnnotation(m, Auditable.class);
				if (auditable != null && !auditable.value()) {
					return true;
				}
				return super.hasIgnoreMarker(m);
			}
		});
	}
	
	public AuditRepository getAuditRepository() {
		return auditRepository;
	}

	public void setAuditRepository(AuditRepository auditRepository) {
		this.auditRepository = auditRepository;
	}
	
	public boolean isAsynchronous() {
		return asynchronous;
	}

	public void setAsynchronous(boolean asynchronous) {
		this.asynchronous = asynchronous;
	}
	
	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType != null ? contentType : ContentType.STRING;
	}
	
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	@Override
	public void run() {
		List<AuditObject> objectList = AuditHolder.getObjectList();
		AuditHolder.resetObjectList();
		if (objectList == null) {
			return;
		}
		Runnable task = task(objectList);
		if (asynchronous) {
			executorService.execute(task);
		} else {
			task.run();
		}
	}
	
	private Runnable task(final List<AuditObject> objectList) {
		return new Runnable() {			
			@Override
			public void run() {
				for (AuditObject auditObject : objectList) {
					save(auditObject);
				}
			}
		};		
	}
	
	private void save(AuditObject object) {
		try {
			Object value = object.getObject();
			if (value == null) {
				return;
			}
			Class<?> clazz = value.getClass();
			Audit audit = new Audit();
			audit.setAction(object.getAction());
			audit.setAuditor(object.getAuditor());
			audit.setClassname(clazz.getName());
			audit.setEntry(object.getEntry());
			if (value instanceof byte[]) {
				audit.setBytes((byte[])value);
			} else {
				if (ContentType.BYTES.equals(contentType)) {
					byte[] bytes = objectMapper.writeValueAsBytes(value);
					audit.setBytes(bytes);
				} else if (ContentType.STRING_AND_BYTES.equals(contentType)) {
					byte[] bytes = objectMapper.writeValueAsBytes(value);
					audit.setBytes(bytes);
					String content = objectMapper.writeValueAsString(value);
					audit.setContent(content);
				} else {
					String content = objectMapper.writeValueAsString(value);
					audit.setContent(content);
				}
			}
			auditRepository.save(audit);
		} catch (Exception e) {
			LOGGER.error("AUDIT", e);
		}
	}
	
}
