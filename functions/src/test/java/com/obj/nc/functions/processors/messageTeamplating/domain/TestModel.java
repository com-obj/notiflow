package com.obj.nc.functions.processors.messageTeamplating.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TestModel {
	String name;
	
	List<TestChildModel> parts = new ArrayList<>();

}