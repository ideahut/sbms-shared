package com.github.ideahut.sbms.shared.dao;

import java.io.Serializable;
import java.util.List;

import com.github.ideahut.sbms.client.dto.PageDto;
import com.github.ideahut.sbms.shared.entity.EntityBase;
import com.github.ideahut.sbms.shared.hibernate.OrderSpec;

public interface DaoBase<ET extends EntityBase<ID>, ID extends Serializable> {
	
	ET get(ID id);

	ET save(ET domain);

	ET delete(ID id);

	List<ET> find(ET domain, String... excludedProperties);

	List<ET> find(ET domain, OrderSpec orderSpec, String...excludedProperties);

	PageDto<ET> find(PageDto<ET> page, ET domain, String...excludedProperties);

	PageDto<ET> find(PageDto<ET> page, ET domain, OrderSpec orderSpec, String...excludedProperties);

	boolean isExists(ID id);
}
