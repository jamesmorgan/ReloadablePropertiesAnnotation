package com.morgan.design.properties.internal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.morgan.design.properties.testBeans.AutowiredPropertyBean;

@ContextConfiguration(locations = { "classpath:/spring/spring-reloadablePropertyPostProcessorIntTest.xml" })
public class ReloadablePropertyPostProcessorIntTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private AutowiredPropertyBean bean;

	@Test
	public void shouldRecurseThroughNestedPropertiesWhenAutowiring() {
		assertThat("Property was not resolved as expected", this.bean.getSubstitutedProperty(), is("elephant"));
	}

	@Test
	public void shouldNotAdjustFieldWhenNotAnnotatedWithAutowiredProperty() {
		assertThat("Field not annotated should not be changed", this.bean.getNotAnnotated(), is("Original value"));
	}

	@Test
	public void shouldPreserveDefaultIfNoPropertyReplacementFound() {
		assertThat("Should have preserved default value", this.bean.getWithDefaultValue(), is("Default Value"));
		assertThat("Should have preserved default value for primitive", this.bean.getPrimitiveWithDefaultValue(), is(55));
	}

	@Test
	public void shouldInjectStringValue() {
		assertThat(this.bean.getStringProperty(), is("Injected String Value"));
	}

	@Test
	public void shouldInjectBooleanValue() {
		assertThat(this.bean.getBooleanProperty(), is(true));
	}

	@Test
	public void shouldInjectIntValue() {
		assertThat(this.bean.getIntProperty(), is(42));
		assertThat(this.bean.getIntObjectProperty(), is(42));
	}

	@Test
	public void shouldInjectLongValue() {
		assertThat(this.bean.getLongProperty(), is(12345L));
		assertThat(this.bean.getLongObjectProperty(), is(12345L));
	}

	@Test
	public void shouldInjectDoubleValue() {
		assertThat(this.bean.getDoubleProperty(), is(12345.67));
		assertThat(this.bean.getDoubleObjectProperty(), is(12345.67));
	}

	@Test
	public void shouldInjectBigIntegerValue() {
		assertThat(this.bean.getBigIntegerProperty(), is(new BigInteger("224411")));
	}

	@Test
	public void shouldInjectBigDecimalValue() {
		assertThat("Should have 2 decimal places", this.bean.getBigDecimalProperty()
			.scale(), is(2));
		assertThat(this.bean.getBigDecimalProperty(), is(new BigDecimal("20012.56")));
	}

	@Test
	public void shouldInjectPeriodValue() {
		assertThat(this.bean.getPeriodProperty(), is(new Period(0, 12, 22, 0)));
	}

	@Test
	public void shouldInjectLocalDateValue() {
		assertThat(this.bean.getLocalDateProperty(), is(new LocalDate(2009, 06, 12)));
	}

	@Test
	public void shouldInjectLocalDateTimeValue() {
		assertThat(this.bean.getLocalDateTimeProperty(), is(new LocalDateTime(2009, 7, 5, 12, 56, 2)));
	}

	@Test
	public void shouldInjectLocalTimeValue() {
		assertThat(this.bean.getLocalTimeProperty(), is(new LocalTime(12, 22, 45)));
	}

}
