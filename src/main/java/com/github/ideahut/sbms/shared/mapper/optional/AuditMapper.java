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
	private AuditRepository auditRepo;

	@Override
	public AuditDto toDto(Audit entity) {
		if (null == entity) {
			return null;
		}
		AuditDto dto = new AuditDto();
		dto.setAction(entity.getAction());
		dto.setAuditor(entity.getAuditor());
		dto.setBytes(entity.getBytes());
		dto.setClassname(entity.getClassname());
		dto.setContent(entity.getContent());
		dto.setEntry(entity.getEntry());
		dto.setId(entity.getId());
		return dto;
	}

	@Override
	public Audit toEntity(AuditDto dto) {
		if (null == dto) {
			return null;
		}
		Audit entity = null != dto.getId() ? auditRepo.findById(dto.getId()).orElse(null) : null;
		if (null == entity) entity = new Audit();
		entity.setAction(dto.getAction());
		entity.setAuditor(dto.getAuditor());
		entity.setBytes(dto.getBytes());
		entity.setClassname(dto.getClassname());
		entity.setContent(dto.getContent());
		entity.setEntry(dto.getEntry());	
		return entity;
	}

}
