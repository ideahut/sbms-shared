package com.github.ideahut.sbms.shared.repo.optional;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import com.github.ideahut.sbms.shared.entity.optional.SysParam;

public interface SysParamRepository extends PagingAndSortingRepository<SysParam, Long>, QueryByExampleExecutor<SysParam> {
	
	SysParam findBySysAndParam(Integer sys, Integer param);
	
	List<SysParam> findBySys(Integer sys);
	
}
