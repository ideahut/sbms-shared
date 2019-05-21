package com.github.ideahut.sbms.shared.entity.optional;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

import com.github.ideahut.sbms.shared.entity.EntityStringId;

@Entity
@Table(name = "__audit")
@SuppressWarnings("serial")
public class Audit extends EntityStringId {

	private String auditor;
	
	private String action;
	
	private String classname;
	
	private String content;
	
	private byte[] bytes;
	
	private Date entry;

	
	@Column(name = "auditor_")
	public String getAuditor() {
		return auditor;
	}

	public void setAuditor(String auditor) {
		this.auditor = auditor;
	}

	@Column(name = "action_", length = 100)
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Column(name = "classname_")
	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	@Lob
	@Column(name = "content_")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@Lob
	@Column(name = "bytes_")
	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	@Column(name = "entry_", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	public Date getEntry() {
		return entry;
	}

	public void setEntry(Date entry) {
		this.entry = entry;
	}

}
