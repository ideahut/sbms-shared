package com.github.ideahut.sbms.shared.entity;

import java.io.Serializable;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

@SuppressWarnings("serial")
@MappedSuperclass
@EntityListeners(EntityListener.class)
public abstract class EntityBase<ID extends Serializable> implements Serializable {

	protected static final String COLUMN_ID			= "ID_";
	protected static final String COLUMN_VERSION	= "VERSION_";
	protected static final String COLUMN_CREATED	= "CREATED_AT_";
	protected static final String COLUMN_UPDATED	= "UPDATED_AT_";
	
	public static final String FIELD_ID 			= "id";
	public static final String FIELD_VERSION		= "version";
	public static final String FIELD_CREATED 		= "createdAt";
	public static final String FIELD_UPDATED		= "updatedAt";
	
}