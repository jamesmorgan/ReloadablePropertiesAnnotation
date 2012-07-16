package com.morgan.design.properties.conversion;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;

public class DefaultPropertyConversionServiceUnitTest {

	private PropertyConversionService conversionService;

	@Before
	public void setUp() throws Exception {
		this.conversionService = new DefaultPropertyConversionService();
	}

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

	static class TestObject {
		Period period = new Period();
		LocalTime localTime = new LocalTime();
		LocalDate localDate = new LocalDate();
		LocalDateTime localDateTime = new LocalDateTime();
		boolean booleanValue;
	}

	private Object convertPropertyForField(final String fieldName, final Object value) throws NoSuchFieldException, SecurityException {
		return this.conversionService.convertPropertyForField(TestObject.class.getDeclaredField(fieldName), value);
	}
}
