package com.github.ideahut.sbms.shared.optional.audit;

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

	private String auditorId;
	
	private String auditorName;
	
	private String action;
	
	private String info;
	
	private String type;	
	
	private String content;
	
	private byte[] bytes;
	
	private Date entry;

	
	@Column(name = "auditor_id_")
	public String getAuditorId() {
		return auditorId;
	}

	public void setAuditorId(String auditorId) {
		this.auditorId = auditorId;
	}

	@Column(name = "auditor_name_")
	public String getAuditorName() {
		return auditorName;
	}

	public void setAuditorName(String auditorName) {
		this.auditorName = auditorName;
	}

	@Column(name = "action_")
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	@Column(name = "info_")
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	@Column(name = "type_")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
