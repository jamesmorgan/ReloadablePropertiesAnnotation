package com.morgan.design.properties.testBeans;

import org.springframework.stereotype.Component;

import com.morgan.design.properties.ReloadableProperty;

@Component
public class ReloadingAutowiredPropertyBean {

	@ReloadableProperty("dynamicProperty.stringValue")
	private String stringProperty;

	public String getStringProperty() {
		return this.stringProperty;
	}
}
