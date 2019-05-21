package com.github.ideahut.sbms.shared.entity;

import java.io.Serializable;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import com.github.ideahut.sbms.shared.audit.AuditListener;

@SuppressWarnings("serial")
@MappedSuperclass
@EntityListeners(AuditListener.class)
public abstract class EntityBase<ID extends Serializable> implements Serializable {

	protected static final String COLUMN_ID	= "ID_";
	
	public static final String FIELD_ID 	= "id";
	
}