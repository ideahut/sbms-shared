package com.github.ideahut.sbms.shared.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class EntityTime <ID extends Serializable> extends EntityBase<ID> {
	
	protected static final String COLUMN_CREATED	= "CREATED_AT_";
	protected static final String COLUMN_UPDATED	= "UPDATED_AT_";
	
	public static final String FIELD_CREATED 		= "createdAt";
	public static final String FIELD_UPDATED		= "updatedAt";
	
	private Date createdAt;
	
	private Date updatedAt;
	
	@Column(name = COLUMN_CREATED, nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Column(name = COLUMN_UPDATED, nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@UpdateTimestamp
	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	
}
