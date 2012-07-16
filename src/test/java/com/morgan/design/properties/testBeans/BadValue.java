package com.morgan.design.properties.testBeans;

import org.joda.time.Period;
import org.springframework.stereotype.Component;

import com.morgan.design.properties.ReloadableProperty;

@Component
public class BadValue {

	@SuppressWarnings("unused")
	@ReloadableProperty("invalid.period")
	private Period period;

}
