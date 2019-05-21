package com.github.ideahut.sbms.shared.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class EntityLongIdTime extends EntityTime<Long> {	
	
	private Long id;

	@Id
	@GeneratedValue(generator = "hibincr")
	@GenericGenerator(name = "hibincr", strategy = "identity")
	@Column(name = COLUMN_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}
