package com.github.ideahut.sbms.shared.optional.audit;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface AuditRepository extends PagingAndSortingRepository<Audit, String>, QueryByExampleExecutor<Audit> {

}
