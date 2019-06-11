package com.github.ideahut.sbms.shared.optional.sysparam;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface SysParamRepository extends PagingAndSortingRepository<SysParam, Long>, QueryByExampleExecutor<SysParam> {
	
	SysParam findBySysAndParam(Integer sys, Integer param);
	
	List<SysParam> findBySys(Integer sys);
	
}
