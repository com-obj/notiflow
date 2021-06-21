package com.obj.nc.extensions;

public interface ModelForTemplateProvider {

	public <MODEL_TYPE> MODEL_TYPE getModelForTemplatedIntent();
}
