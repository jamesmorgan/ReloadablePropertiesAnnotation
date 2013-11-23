package com.morgan.design.properties.testBeans;

import org.springframework.stereotype.Component;

import com.morgan.design.properties.ReloadableProperty;

@Component
public class MissingProperty {

	@ReloadableProperty("does.not.exist")
	private String hasNoDefaultValue;

}
