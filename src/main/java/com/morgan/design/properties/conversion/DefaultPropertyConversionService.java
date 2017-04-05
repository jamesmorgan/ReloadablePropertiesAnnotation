package com.morgan.design.properties.conversion;

import java.lang.reflect.Field;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.morgan.design.util.JodaUtils;

/**
 * Default implementation of {@link PropertyConversionService}, attempting to convert an object otherwise utilising {@link SimpleTypeConverter} if no matching
 * converter is found.
 * 
 * @author James Morgan
 */
@Component
public class DefaultPropertyConversionService implements PropertyConversionService {

	@Autowired
	private ConfigurableBeanFactory configurableBeanFactory;
	private static TypeConverter DEFAULT;
	private static Map<Class<? extends Object>, Function<Object, ?>> CONVERTS = Maps.newHashMap();
	static {
		CONVERTS.put(Period.class, new PeriodConverter());
		CONVERTS.put(LocalDateTime.class, new LocalDateTimeConverter());
		CONVERTS.put(LocalDate.class, new LocalDateConverter());
		CONVERTS.put(LocalTime.class, new LocalTimeConverter());
	}
	
	@PostConstruct
	public void init() {
		DEFAULT = configurableBeanFactory.getTypeConverter();
	}
	
	@Override
	public Object convertPropertyForField(final Field field, final Object property) {
		try {
			return Functions.forMap(CONVERTS, new DefaultConverter(field.getType()))
				.apply(field.getType())
				.apply(property);
		}
		catch (final Throwable e) {
			throw new BeanInitializationException(String.format("Unable to convert property for field [%s].  Value [%s] cannot be converted to [%s]",
					field.getName(), property, field.getType()), e);
		}
	}

	private static class DefaultConverter implements Function<Object, Object> {
		private final Class<?> type;

		public DefaultConverter(final Class<?> type) {
			this.type = type;
		}

		@Override
		public Object apply(final Object input) {
			return DEFAULT.convertIfNecessary(input, this.type);
		}
	}

	private static class PeriodConverter implements Function<Object, Period> {
		@Override
		public Period apply(final Object input) {
			return JodaUtils.timeStringToPeriodOrNull((String) input);
		}
	}

	private static class LocalDateTimeConverter implements Function<Object, LocalDateTime> {
		@Override
		public LocalDateTime apply(final Object input) {
			return JodaUtils.timestampStringToLocalDateTimeOrNull((String) input);
		}
	}

	private static class LocalDateConverter implements Function<Object, LocalDate> {
		@Override
		public LocalDate apply(final Object input) {
			return JodaUtils.dateStringToLocalDateOrNull((String) input);
		}
	}

	private static class LocalTimeConverter implements Function<Object, LocalTime> {
		@Override
		public LocalTime apply(final Object input) {
			return JodaUtils.timeStringToLocalTimeOrNull((String) input);
		}
	}
}
