package com.github.ideahut.sbms.shared.optional.mapper;

import com.github.ideahut.sbms.client.dto.optional.SysParamDto;
import com.github.ideahut.sbms.shared.mapper.EntityDtoMapper;
import com.github.ideahut.sbms.shared.optional.sysparam.SysParam;
import com.github.ideahut.sbms.shared.optional.sysparam.SysParamRepository;

public class SysParamMapper extends EntityDtoMapper<SysParam, SysParamDto> {
	
	private SysParamRepository sysParamRepository;
	
	public void setSysParamRepository(SysParamRepository sysParamRepository) {
		this.sysParamRepository = sysParamRepository;
	}

	@Override
	public SysParamDto toDto(SysParam entity) {
		if (null == entity) {
			return null;
		}
		SysParamDto dto = new SysParamDto();
		dto.setBytes(entity.getBytes());
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setDesc(entity.getDesc());
		dto.setId(entity.getId());
		dto.setParam(entity.getParam());
		dto.setSys(entity.getSys());
		dto.setUpdatedAt(entity.getUpdatedAt());
		dto.setValue(entity.getValue());
		dto.setVersion(entity.getVersion());
		return dto;
	}

	@Override
	public SysParam toEntity(SysParamDto dto) {
		if (null == dto) {
			return null;
		}
		SysParam entity = null != dto.getId() ? sysParamRepository.findById(dto.getId()).orElse(null) : null;
		if (null == entity) entity = new SysParam();
		entity.setBytes(dto.getBytes());
		entity.setCreatedAt(dto.getCreatedAt());
		entity.setDesc(dto.getDesc());
		entity.setParam(dto.getParam());
		entity.setSys(dto.getSys());
		entity.setUpdatedAt(dto.getUpdatedAt());
		entity.setValue(dto.getValue());
		entity.setVersion(dto.getVersion());
		return entity;
	}

}
