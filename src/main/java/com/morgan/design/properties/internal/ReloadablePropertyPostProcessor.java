package com.morgan.design.properties.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.morgan.design.properties.ReloadableProperty;
import com.morgan.design.properties.bean.BeanPropertyHolder;
import com.morgan.design.properties.bean.PropertyModifiedEvent;
import com.morgan.design.properties.conversion.PropertyConversionService;
import com.morgan.design.properties.event.PropertyChangedEventNotifier;

/**
 * <p>
 * Processes beans on start up injecting field values marked with {@link ReloadableProperty} setting the associated annotated property value with properties
 * configured in a {@link ReadablePropertySourcesPlaceholderConfigurer}.
 * </p>
 * <p>
 * The processor also has the ability to reload/re-inject properties from the configured {@link ReadablePropertySourcesPlaceholderConfigurer} which are changed.
 * Once a property is reloaded the associated bean holding that value will have its property updated, no further bean operations are performed on the reloaded
 * bean.
 * </p>
 * <p>
 * The processor will also substitute any properties with values starting with "${" and ending with "}", none recursive.
 * </p>
 * 
 * @author James Morgan
 */
@Component
public class ReloadablePropertyPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

	protected static Logger log = LoggerFactory.getLogger(ReloadablePropertyPostProcessor.class);

	private final PropertyChangedEventNotifier eventNotifier;
	private final PropertyConversionService propertyConversionService;
	private final ReadablePropertySourcesPlaceholderConfigurer placeholderConfigurer;

	private Map<String, Set<BeanPropertyHolder>> beanPropertySubscriptions = Maps.newHashMap();

	@Autowired
	public ReloadablePropertyPostProcessor(final ReadablePropertySourcesPlaceholderConfigurer placeholderConfigurer,
			final PropertyChangedEventNotifier eventNotifier, final PropertyConversionService conversionService) {
		this.eventNotifier = eventNotifier;
		this.placeholderConfigurer = placeholderConfigurer;
		this.propertyConversionService = conversionService;
	}

	@PostConstruct
	protected void init() {
		log.info("Registering ReloadablePropertyProcessor for properties file changes");
		registerPropertyReloader();
	}

	/**
	 * Utility method to unregister the class from receiving events about property files being changed.
	 */
	public final void unregisterPropertyReloader() {
		log.info("Unregistering ReloadablePropertyProcessor from property file changes");
		this.eventNotifier.unregister(this);
	}

	/**
	 * Utility method to register the class for receiving events about property files being changed, setting up bean re-injection once triggered.
	 */
	public final void registerPropertyReloader() {
		// Setup Guava event bus listener
		this.eventNotifier.register(this);
		// Trigger resource change listener
		this.placeholderConfigurer.startWatching();
	}

	/**
	 * Method subscribing to the {@link PropertyModifiedEvent} utilising the {@link Subscribe} annotation
	 * 
	 * @param event the {@link PropertyModifiedEvent} detailing what's changed
	 */
	@Subscribe
	public void handlePropertyChange(final PropertyModifiedEvent event) {
		for (final BeanPropertyHolder bean : this.beanPropertySubscriptions.get(event.getPropertyName())) {
			updateField(bean, event);
		}
	}

	public void updateField(final BeanPropertyHolder holder, final PropertyModifiedEvent event) {
		final Object beanToUpdate = holder.getBean();
		final Field fieldToUpdate = holder.getField();
		final String canonicalName = beanToUpdate.getClass()
			.getCanonicalName();

		final Object convertedProperty = convertPropertyForField(fieldToUpdate, event.getPropertyName());
		try {
			log.info("Reloading property [{}] on field [{}] for class [{}]", new Object[] { event.getPropertyName(), fieldToUpdate.getName(), canonicalName });
			fieldToUpdate.set(beanToUpdate, convertedProperty);
		}
		catch (final IllegalAccessException e) {
			log.error("Unable to reloading property [{}] on field [{}] for class [{}]\n Exception [{}]",
					new Object[] { event.getPropertyName(), fieldToUpdate.getName(), canonicalName, e.getMessage() });
		}
	}

	@Override
	public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {
		if (log.isDebugEnabled()) {
			log.debug("Setting Reloadable Properties on [{}]", beanName);
		}
		setPropertiesOnBean(bean);
		return true;
	}

	private void setPropertiesOnBean(final Object bean) {
		ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {

			@Override
			public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {

				final ReloadableProperty annotation = field.getAnnotation(ReloadableProperty.class);
				if (null != annotation) {

					ReflectionUtils.makeAccessible(field);
					validateFieldNotFinal(bean, field);

					final Object property = getProperties().get(annotation.value());
					validatePropertyAvailableOrDefaultSet(bean, field, annotation, property);

					if (null != property) {

						log.info("Attempting to convert and set property [{}] on field [{}] for class [{}] to type [{}]",
								new Object[] { property, field.getName(), bean.getClass()
									.getCanonicalName(), field.getType() });

						final Object convertedProperty = convertPropertyForField(field, annotation.value());

						log.info("Setting field [{}] of class [{}] with value [{}]", new Object[] { field.getName(), bean.getClass()
							.getCanonicalName(), convertedProperty });

						field.set(bean, convertedProperty);

						subscribeBeanToPropertyChangedEvent(annotation.value(), new BeanPropertyHolder(bean, field));
					}
					else {
						log.info("Leaving field [{}] of class [{}] with default value", new Object[] { field.getName(), bean.getClass()
							.getCanonicalName() });
					}
				}
			}
		});
	}

	private void validatePropertyAvailableOrDefaultSet(final Object bean, final Field field, final ReloadableProperty annotation, final Object property)
			throws IllegalArgumentException, IllegalAccessException {
		if (null == property && fieldDoesNotHaveDefault(field, bean)) {
			throw new BeanInitializationException(String.format("No property found for field annotated with @ReloadableProperty, "
				+ "and no default specified. Property [%s] of class [%s] requires a property named [%s]", field.getName(), bean.getClass()
				.getCanonicalName(), annotation.value()));
		}
	}

	private void validateFieldNotFinal(final Object bean, final Field field) {
		if (Modifier.isFinal(field.getModifiers())) {
			throw new BeanInitializationException(String.format("Unable to set field [%s] of class [%s] as is declared final", field.getName(), bean.getClass()
				.getCanonicalName()));
		}
	}

	private boolean fieldDoesNotHaveDefault(final Field field, final Object value) throws IllegalArgumentException, IllegalAccessException {
		try {
			return (null == field.get(value));
		}
		catch (final NullPointerException e) {
			return true;
		}
	}

	private void subscribeBeanToPropertyChangedEvent(final String property, final BeanPropertyHolder fieldProperty) {
		if (!this.beanPropertySubscriptions.containsKey(property)) {
			this.beanPropertySubscriptions.put(property, new HashSet<BeanPropertyHolder>());
		}
		this.beanPropertySubscriptions.get(property)
			.add(fieldProperty);
	}

	// ///////////////////////////////////
	// Utility methods for class access //
	// ///////////////////////////////////

	private Object convertPropertyForField(final Field field, final Object property) {
		return this.propertyConversionService.convertPropertyForField(field, resolverProperty(property));
	}

	private Object resolverProperty(final Object property) {
		return this.placeholderConfigurer.resolveProperty(property);
	}

	private Properties getProperties() {
		return this.placeholderConfigurer.getProperties();
	}
}
