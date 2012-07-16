package com.morgan.design.util;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Test;

/**
 * @author James Morgan
 */
public class JodaUtilsUnitTest {

	@After
	public void tearDown() {
		DateTimeUtils.setCurrentMillisSystem();
	}

	@Test
	public void shouldNotConvertDateStringToLocalDateForBlankDate() {
		assertThat(JodaUtils.dateStringToLocalDateOrNull(null), is(nullValue()));
		assertThat(JodaUtils.dateStringToLocalDateOrNull(""), is(nullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotConvertDateStringToLocalDateForInvalidDate() {
		JodaUtils.dateStringToLocalDateOrNull("12:34:01");
	}

	@Test
	public void shouldConvertDateStringToLocalDateForValidDate() {
		assertThat(JodaUtils.dateStringToLocalDateOrNull("2007-08-02"), is(new LocalDate(2007, 8, 2)));
		assertThat(JodaUtils.dateStringToLocalDateOrNull("2007-9-9"), is(new LocalDate(2007, 9, 9)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenConvertingInvalidTimestampStringToLocalDateTime() {
		JodaUtils.timestampStringToLocalDateTimeOrNull("2007-13-40 25:61:61");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenConvertingNonTimestampStringToLocalDateTime() {
		JodaUtils.timestampStringToLocalDateTimeOrNull("balderdash");
	}

	@Test
	public void canConvertTimestampStringToLocalDateTimeWithExtraZero() {
		assertThat("The method should return null when invoked with null", JodaUtils.timestampStringToLocalDateTimeOrNull(null), is(nullValue()));

		LocalDateTime actual = JodaUtils.timestampStringToLocalDateTimeOrNull("2006-05-27 16:03:34.0");
		assertThat("The expected outcome was not returned", actual, is(new LocalDateTime(2006, 5, 27, 16, 3, 34)));

		actual = JodaUtils.timestampStringToLocalDateTimeOrNull("2007-8-1 3:2:4.0");
		assertThat("The expected outcome was not returned", actual, is(new LocalDateTime(2007, 8, 1, 3, 2, 4)));
	}

	@Test
	public void shouldConvertTimeStringIntoPeriod() {
		assertThat(JodaUtils.timeStringToPeriodOrNull("24:00:00"), is(new Period(24, 0, 0, 0)));
		assertThat(JodaUtils.timeStringToPeriodOrNull("48:00:00"), is(new Period(48, 0, 0, 0)));
		assertThat(JodaUtils.timeStringToPeriodOrNull("72:00:00"), is(new Period(72, 0, 0, 0)));

		assertThat(JodaUtils.timeStringToPeriodOrNull("24:30:00"), is(new Period(24, 30, 0, 0)));
		assertThat(JodaUtils.timeStringToPeriodOrNull("48:30:00"), is(new Period(48, 30, 0, 0)));
		assertThat(JodaUtils.timeStringToPeriodOrNull("72:30:00"), is(new Period(72, 30, 0, 0)));

		assertThat(JodaUtils.timeStringToPeriodOrNull("24:30:15"), is(new Period(24, 30, 15, 0)));
		assertThat(JodaUtils.timeStringToPeriodOrNull("48:30:15"), is(new Period(48, 30, 15, 0)));
		assertThat(JodaUtils.timeStringToPeriodOrNull("72:30:15"), is(new Period(72, 30, 15, 0)));

		assertThat(JodaUtils.timeStringToPeriodOrNull("124:30:00"), is(new Period(124, 30, 0, 0)));
		assertThat(JodaUtils.timeStringToPeriodOrNull("00:00:00"), is(new Period(0, 0, 0, 0)));

		assertThat(JodaUtils.timeStringToPeriodOrNull(""), is(nullValue()));
		assertThat(JodaUtils.timeStringToPeriodOrNull(null), is(nullValue()));
	}

}
