package com.obj.nc.domain.refIntegrity;

public interface EntityExistenceChecker<ID> {
	
	boolean existsById(ID id);

}
