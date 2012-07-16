package com.morgan.design.properties.internal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SuppressWarnings("unused")
public class FailingReloadablePropertyPostProcessorIntTest {

	@Test
	public void shouldThrowBeanInitializationExceptionWhenNoPropertyFoundAndNoDefaultValue() {
		try {
			new ClassPathXmlApplicationContext("classpath:/spring/spring-missingProperty.xml");
			fail("Should have thrown BeanException due to missing property");
		}
		catch (final BeanCreationException e) {
			assertThat(e.getCause(), is(instanceOf(BeanInitializationException.class)));
			assertThat(e.getCause()
				.getMessage(), containsString("requires a property named [does.not.exist]"));
		}
	}

	@Test
	public void shouldThrowBeanInitializationExceptionWhenUnableToConvertPropertyToFieldType() {
		try {
			new ClassPathXmlApplicationContext("classpath:/spring/spring-badValue.xml");
			fail("Should have thrown BeanException due to bad value for conversion");
		}
		catch (final BeanCreationException e) {
			assertThat(e.getCause(), is(instanceOf(BeanInitializationException.class)));
			assertThat(e.getCause()
				.getMessage(), containsString("Unable to convert property"));
		}
	}

	@Test
	public void shouldThrowBeanInitializationExceptionWhenFieldSetAsFinal() {
		try {
			new ClassPathXmlApplicationContext("classpath:/spring/spring-finalFieldBean.xml");
			fail("Should have thrown BeanException due to bad value for conversion");
		}
		catch (final BeanCreationException e) {
			assertThat(e.getCause(), is(instanceOf(BeanInitializationException.class)));
			assertThat(e.getCause()
				.getMessage(), containsString("Unable to set field"));
			assertThat(e.getCause()
				.getMessage(), containsString("as is declared final"));
		}
	}
}
