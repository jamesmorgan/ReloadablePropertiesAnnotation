package com.morgan.design.util;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.common.base.Strings;

public class JodaUtils {

	public static final int MYSQL_SUNDAY = 0;

	public static final LocalTime END_OF_DAY = new LocalTime(23, 59, 59, 0);

	public static final LocalTime START_OF_DAY = new LocalTime(0, 0, 0, 0);

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

	private static final PeriodFormatter DEFAULT_PERIOD_FORMATTER = new PeriodFormatterBuilder().printZeroAlways()
		.minimumPrintedDigits(2)
		.appendHours()
		.appendSeparator(":")
		.appendMinutes()
		.appendSeparator(":")
		.appendSeconds()
		.toFormatter();

	private static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm:ss");

	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

	private static final DateTimeFormatter TIMESTAMP_FORMAT_WITH_TRAILING_ZERO = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.0");

	private JodaUtils() {
		throw new IllegalStateException("Constructor is private");
	}

	/**
	 * Returns a {@link LocalDate} representation of the given {@link String}.
	 * 
	 * @param dateString The date as a {@link String} in the form "yyyy-MM-dd"
	 * @param defaultValue The value to return if invoked with null
	 * @return new {@link LocalDate} representation of the dateString
	 */
	public static LocalDate dateStringToLocalDateOrDefaultValue(final String dateString, final LocalDate defaultValue) {
		return Strings.isNullOrEmpty(dateString)
				? defaultValue
				: new LocalDate(DATE_FORMAT.parseMillis(dateString));
	}

	/**
	 * Returns a {@link LocalDate} representation of the given {@link String}.
	 * 
	 * @param dateString The date as a {@link String} in the form "yyyy-MM-dd"
	 * @return new {@link LocalDate} representation of the dateString
	 */
	public static LocalDate dateStringToLocalDateOrNull(final String dateString) {
		return dateStringToLocalDateOrDefaultValue(dateString, null);
	}

	/**
	 * Converts the given {@link String} in MySQL datetime format to a {@link LocalDateTime}.
	 * 
	 * @param timestampString The date and time in "yyyy-MM-dd HH:mm:ss" format
	 * @param defaultValue The value to return if the timestamp is null
	 * @return A {@link LocalDateTime} representation of the given {@link String}
	 * @throws IllegalArgumentException when given an invalid timestamp
	 */
	public static LocalDateTime timestampStringToLocalDateTimeOrDefaultValue(final String timestampString, final LocalDateTime defaultValue) {
		if (Strings.isNullOrEmpty(timestampString)) {
			return defaultValue;
		}
		return (timestampString.endsWith(".0"))
				? new LocalDateTime(TIMESTAMP_FORMAT_WITH_TRAILING_ZERO.parseMillis(timestampString))
				: new LocalDateTime(TIMESTAMP_FORMAT.parseMillis(timestampString));
	}

	/**
	 * Converts the given {@link String} in MySQL datetime format to a {@link LocalDateTime}.
	 * 
	 * @param timestampString The date and time in "yyyy-MM-dd HH:mm:ss" format
	 * @return A {@link LocalDateTime} representation of the given {@link String}
	 * @throws IllegalArgumentException when given an invalid timestamp
	 */
	public static LocalDateTime timestampStringToLocalDateTimeOrNull(final String timestampString) {
		return timestampStringToLocalDateTimeOrDefaultValue(timestampString, null);
	}

	/**
	 * Converts the given {@link String} in time format to a {@link LocalTime}.
	 * 
	 * @param timeString The date and time in "HH:mm:ss" format
	 * @param defaultValue The value to return if the timestamp is null
	 * @return A {@link LocalTime} representation of the given {@link String}
	 * @throws IllegalArgumentException when given an invalid time string
	 */
	public static LocalTime timeStringToLocalTimeOrDefaultValue(final String timeString, final LocalTime defaultValue) {
		if (Strings.isNullOrEmpty(timeString)) {
			return defaultValue;
		}
		return DEFAULT_TIME_FORMATTER.parseDateTime(timeString)
			.toLocalTime();
	}

	/**
	 * Converts the given {@link String} in time format to a {@link LocalTime}.
	 * 
	 * @param timeString The date and time in "HH:mm:ss" format
	 * @return A {@link LocalTime} representation of the given {@link String}
	 * @throws IllegalArgumentException when given an invalid time string
	 */
	public static LocalTime timeStringToLocalTimeOrNull(final String timeString) {
		return timeStringToLocalTimeOrDefaultValue(timeString, null);
	}

	/**
	 * Converts a time string to a {@link Period}.
	 * 
	 * @param input The time in HH:mm:ss format
	 * @param defaultValue The value to use if the input is null or blank
	 * @return The {@link Period} represented by the given {@link String}
	 * @throws IllegalArgumentException if there is an error parsing the {@link String}
	 */
	public static Period timeStringToPeriodOrDefaultValue(final String input, final Period defaultValue) {
		return (Strings.isNullOrEmpty(input))
				? defaultValue
				: DEFAULT_PERIOD_FORMATTER.parsePeriod(input);
	}

	/**
	 * Converts a time string to a {@link Period}.
	 * 
	 * @param input The time in HH:mm:ss format
	 * @return The {@link Period} represented by the given {@link String}
	 * @throws IllegalArgumentException if there is an error parsing the {@link String}
	 */
	public static Period timeStringToPeriodOrNull(final String input) {
		return timeStringToPeriodOrDefaultValue(input, null);
	}

}
