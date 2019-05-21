package com.github.ideahut.sbms.shared.mapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class EntityDtoMapper<Entity extends Serializable, DTO extends Serializable> {
	
	public List<DTO> toDto(List<Entity> entities) {
		if (entities == null) {
			return null;
		}
		List<DTO> result = new ArrayList<DTO>();
		for (Entity entity : entities) {
			result.add(toDto(entity));
		}
		return result;
	}
	
	public List<Entity> toEntity(List<DTO> dtos) {
		if (dtos == null) {
			return null;
		}
		List<Entity> result = new ArrayList<Entity>();
		for (DTO dto : dtos) {
			result.add(toEntity(dto));
		}
		return result;
	}

	public abstract DTO toDto(Entity entity);
	
	public abstract Entity toEntity(DTO dto);	
	
}
