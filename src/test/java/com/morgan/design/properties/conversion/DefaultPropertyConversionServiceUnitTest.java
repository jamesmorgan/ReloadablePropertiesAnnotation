package com.morgan.design.properties.conversion;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = {"classpath:/spring/spring-reloadablePropertyPostProcessorIntTest.xml"})
public class DefaultPropertyConversionServiceUnitTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private PropertyConversionService conversionService;

	@Test
	public void shouldConvertPeriodForPropertyForField() throws NoSuchFieldException, SecurityException {
		assertThat((Period) convertPropertyForField("period", "24:00:00"), is(new Period(24, 0, 0, 0)));
		assertThat((Period) convertPropertyForField("period", "48:00:00"), is(new Period(48, 0, 0, 0)));
		assertThat((Period) convertPropertyForField("period", "72:00:00"), is(new Period(72, 0, 0, 0)));
	}

	@Test
	public void shouldConvertLocalDateTimeForPropertyForField() throws NoSuchFieldException, SecurityException {
		LocalDateTime actual = (LocalDateTime) convertPropertyForField("localDateTime", "2006-05-27 16:03:34.0");
		assertThat(actual, is(new LocalDateTime(2006, 5, 27, 16, 3, 34)));

		actual = (LocalDateTime) convertPropertyForField("localDateTime", "2007-8-1 3:2:4.0");
		assertThat(actual, is(new LocalDateTime(2007, 8, 1, 3, 2, 4)));
	}

	@Test
	public void shouldConvertLocalDateForPropertyForField() throws NoSuchFieldException, SecurityException {
		final LocalDate date = (LocalDate) convertPropertyForField("localDate", "2007-08-02");
		assertThat(date, is(new LocalDate(2007, 8, 2)));
	}

	@Test
	public void shouldConvertLocalTimeForPropertyForField() throws NoSuchFieldException, SecurityException {
		assertThat((LocalTime) convertPropertyForField("localTime", "09:30:51"), is(new LocalTime(9, 30, 51)));
		assertThat((LocalTime) convertPropertyForField("localTime", "23:18:41"), is(new LocalTime(23, 18, 41)));
	}

	@Test
	public void shouldConvertBooleanValue() throws NoSuchFieldException, SecurityException {
		assertThat((Boolean) convertPropertyForField("booleanValue", "true"), is(true));
	}

	@Test
	public void shouldConvertDateValue() throws NoSuchFieldException, SecurityException, ParseException {
		Date expected = new SimpleDateFormat("dd-MM-yyyy").parse("9-4-2017");
		assertThat((Date) convertPropertyForField("dateValue", "9-4-2017"), is(expected));
	}
	
	static class TestObject {
		Period period = new Period();
		LocalTime localTime = new LocalTime();
		LocalDate localDate = new LocalDate();
		LocalDateTime localDateTime = new LocalDateTime();
		Date dateValue = new Date();
		boolean booleanValue;
	}

	private Object convertPropertyForField(final String fieldName, final Object value) throws NoSuchFieldException, SecurityException {
		return this.conversionService.convertPropertyForField(TestObject.class.getDeclaredField(fieldName), value);
	}
}
