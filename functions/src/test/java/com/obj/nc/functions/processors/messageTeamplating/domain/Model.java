package com.obj.nc.functions.processors.messageTeamplating.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Model {
	String name;
	
	List<ChilModel>	parts = new ArrayList<>();
}