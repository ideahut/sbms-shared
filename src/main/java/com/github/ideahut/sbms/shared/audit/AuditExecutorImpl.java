package com.github.ideahut.sbms.shared.audit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.ideahut.sbms.shared.audit.handler.AuditHandler;
import com.github.ideahut.sbms.shared.moment.MomentAttributes;
import com.github.ideahut.sbms.shared.moment.MomentHolder;

public class AuditExecutorImpl implements AuditExecutor, InitializingBean {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditExecutorImpl.class);
	
	private ContentType contentType = ContentType.STRING;
	
	private TaskExecutor taskExecutor;
	
	private AuditHandler auditHandler;
	
	
	public ContentType getContentType() {
		return contentType;
	}
	
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
	
	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}
	
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
	
	public void setAuditHandler(AuditHandler auditHandler) {
		this.auditHandler = auditHandler;
	}

	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (taskExecutor == null) {
			ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	        executor.setCorePoolSize(4);
	        executor.setMaxPoolSize(8);
	        executor.setThreadNamePrefix("AUDIT-EXECUTOR");
	        executor.initialize();
	        taskExecutor = executor;
		}
	}

	@Override
	public void run() {
		MomentAttributes momentAttributes = MomentHolder.getMomentAttributes();
		if (momentAttributes == null) {
			return;
		}
		List<AuditObject> mAuditObjectList = momentAttributes.getAuditObjectList();
		if (mAuditObjectList == null || mAuditObjectList.isEmpty()) {
			return;
		}
		List<AuditObject> auditObjectList = new ArrayList<AuditObject>(mAuditObjectList);
		momentAttributes.setAuditObjectList(null); // Bersihkan daftar object, agar tidak diproses berulang
		Auditor momentAuditor = null;
		Auditor auditor = momentAttributes.getAuditor();		
		if (auditor != null) {
			// Dibuat baru, karena akan diproses terpisah. Antisipasi jika sudah di-remove dari holder
			momentAuditor = new Auditor(auditor.getId(), auditor.getName());
		}		
		Runnable task = task(momentAuditor, auditObjectList);
		taskExecutor.execute(task);
	}
	
	private Runnable task(final Auditor momentAuditor, final List<AuditObject> auditObjectList) {
		return new Runnable() {			
			@Override
			public void run() {
				while (!auditObjectList.isEmpty()) {
					AuditObject auditObject = auditObjectList.remove(0);
					
					Object object = auditObject.getObject();
					if (object == null) {
						continue;
					}
					
					Auditor auditor = auditObject.getAuditor();
					if (auditor == null) {
						auditor = momentAuditor;
					}
					auditObject.setAuditor(auditor);
					
					Date entry = auditObject.getEntry();
					if (entry == null) {
						entry = new Date();
					}
					auditObject.setEntry(entry);
					try {
						auditHandler.save(auditObject, contentType);
					} catch (Exception e) {
						LOGGER.error("AUDIT", e);
					}
				}
			}
		};		
	}
	
}
