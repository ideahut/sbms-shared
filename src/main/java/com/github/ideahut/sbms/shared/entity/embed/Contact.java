package com.github.ideahut.sbms.shared.entity.embed;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
@Access(AccessType.PROPERTY)
public class Contact implements Serializable {

	private String phone;
	
	private String email;

	@Column(name = "phone", length = 100)
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Column(name = "email")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}
