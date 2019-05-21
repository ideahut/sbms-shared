package com.github.ideahut.sbms.shared.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class EntityStringIdVersion extends EntityVersion<String> {
	
	private String id;

	@Id
	@GeneratedValue(generator = "hbm_uuid")
	@GenericGenerator(name = "hbm_uuid", strategy = "uuid")
	@Column(name = COLUMN_ID, length = 64)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
