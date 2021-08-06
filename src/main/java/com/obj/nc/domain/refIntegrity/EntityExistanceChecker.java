package com.obj.nc.domain.refIntegrity;

public interface EntityExistanceChecker<ID> {
	
	boolean existsById(ID id);

}
