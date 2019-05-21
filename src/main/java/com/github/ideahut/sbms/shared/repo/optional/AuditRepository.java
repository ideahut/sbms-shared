package com.github.ideahut.sbms.shared.repo.optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import com.github.ideahut.sbms.shared.entity.optional.Audit;

public interface AuditRepository extends PagingAndSortingRepository<Audit, String>, QueryByExampleExecutor<Audit> {

}
