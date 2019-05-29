package com.github.ideahut.sbms.shared.entity;

public interface EntityInterceptor {
	
	public void onPrePersist(EntityBase<?> entity);
      
    public void onPreUpdate(EntityBase<?> entity);
      
    public void onPreRemove(EntityBase<?> entity);
	
}
