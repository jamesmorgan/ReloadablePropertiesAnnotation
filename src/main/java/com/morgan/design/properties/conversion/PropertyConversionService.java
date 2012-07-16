package com.morgan.design.properties.conversion;

import java.lang.reflect.Field;

/**
 * Interface intended for use by any class willing to convert the given property {@link Object} which potentially requires conversion before being set on the
 * given {@link Field}
 * 
 * @author James Morgan
 */
public interface PropertyConversionService {

	/**
	 * @param field the destination filed to set the property on
	 * @param property the property to be converted for the given field
	 * @return the potentially converted field
	 */
	Object convertPropertyForField(final Field field, final Object property);
}
