package com.github.ideahut.sbms.shared.hibernate;

import org.hibernate.Criteria;

public interface CriteriaVisitor<Entity> {
	
	Criteria visit(Criteria criteria, Entity entity, String... excludedProperties);
	
}
