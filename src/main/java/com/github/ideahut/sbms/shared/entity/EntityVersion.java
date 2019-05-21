package com.github.ideahut.sbms.shared.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class EntityVersion<ID extends Serializable> extends EntityBase<ID> {

	protected static final String COLUMN_VERSION	= "VERSION_";
	
	public static final String FIELD_VERSION		= "version";
	
	
	private Long version;

	@Column(name = COLUMN_VERSION, nullable = false)
	@Version
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}	

}
