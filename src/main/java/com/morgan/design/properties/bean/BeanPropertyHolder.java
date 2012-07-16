package com.morgan.design.properties.bean;

import java.lang.reflect.Field;

import com.google.common.base.Objects;

public class BeanPropertyHolder {

	private final Object bean;
	private final Field field;

	public BeanPropertyHolder(Object bean, Field field) {
		this.bean = bean;
		this.field = field;
	}

	public Object getBean() {
		return this.bean;
	}

	public Field getField() {
		return this.field;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.bean, this.field);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof BeanPropertyHolder) {
			BeanPropertyHolder that = (BeanPropertyHolder) object;
			return Objects.equal(this.bean, that.bean) && Objects.equal(this.field, that.field);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("bean", this.bean)
			.add("field", this.field)
			.toString();
	}

}
