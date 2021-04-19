package com.obj.nc.domain;

import com.obj.nc.domain.content.Content;

public interface IsAggregatedContent<T extends Content> {
	
	public void add(T other);
	
	public T asSimpleContent();

}
