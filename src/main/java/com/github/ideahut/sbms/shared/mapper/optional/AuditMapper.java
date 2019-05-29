package com.github.ideahut.sbms.shared.mapper.optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.ideahut.sbms.client.dto.optional.AuditDto;
import com.github.ideahut.sbms.shared.entity.optional.Audit;
import com.github.ideahut.sbms.shared.mapper.EntityDtoMapper;
import com.github.ideahut.sbms.shared.repo.optional.AuditRepository;

@Component
public class AuditMapper extends EntityDtoMapper<Audit, AuditDto> {
	
	@Autowired
	private AuditRepository auditRepository;

	@Override
	public AuditDto toDto(Audit entity) {
		if (null == entity) {
			return null;
		}
		AuditDto dto = new AuditDto();
		dto.setAction(entity.getAction());
		dto.setAuditorId(entity.getAuditorId());
		dto.setAuditorName(entity.getAuditorName());
		dto.setBytes(entity.getBytes());
		dto.setContent(entity.getContent());
		dto.setEntry(entity.getEntry());
		dto.setId(entity.getId());
		dto.setInfo(entity.getInfo());
		dto.setType(entity.getType());
		return dto;
	}

	@Override
	public Audit toEntity(AuditDto dto) {
		if (null == dto) {
			return null;
		}
		Audit entity = null != dto.getId() ? auditRepository.findById(dto.getId()).orElse(new Audit()) : new Audit();
		entity.setAction(dto.getAction());
		entity.setAuditorId(dto.getAuditorId());
		entity.setAuditorName(dto.getAuditorName());
		entity.setBytes(dto.getBytes());
		entity.setContent(dto.getContent());
		entity.setEntry(dto.getEntry());
		entity.setInfo(dto.getInfo());
		entity.setType(dto.getType());	
		return entity;
	}

}
