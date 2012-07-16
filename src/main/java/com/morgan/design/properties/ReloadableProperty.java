package com.morgan.design.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be set from the given property value, the specified property will reset the field if changed during runtime.
 * 
 * @author James Morgan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ReloadableProperty {

	String value();

}
