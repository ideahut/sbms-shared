package com.github.ideahut.sbms.shared.entity.optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.github.ideahut.sbms.shared.entity.EntityLongIdTimeVersion;

@Entity
@Table(name = "__sys_param", uniqueConstraints = {@UniqueConstraint(columnNames = {"sys_", "param_"})})
@SuppressWarnings("serial")
public class SysParam extends EntityLongIdTimeVersion {
	
	private Integer sys;
	
	private Integer param;
	
	private String value;
	
	private byte[] bytes;
	
	private String desc;

	
	@Column(name = "sys_", nullable = false)
	public Integer getSys() {
		return sys;
	}

	public void setSys(Integer sys) {
		this.sys = sys;
	}

	@Column(name = "param_", nullable = false)
	public Integer getParam() {
		return param;
	}

	public void setParam(Integer param) {
		this.param = param;
	}

	@Lob
	@Column(name = "value_")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Lob
	@Column(name = "bytes_")
	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	@Column(name = "desc_")
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
