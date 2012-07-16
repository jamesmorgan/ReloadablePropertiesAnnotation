package com.morgan.design.properties.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;
import com.morgan.design.properties.bean.PropertyModifiedEvent;

@Component
public class GuavaPropertyChangedEventNotifier implements PropertyChangedEventNotifier {

	private final EventBus eventBus;

	@Autowired
	public GuavaPropertyChangedEventNotifier(@Qualifier("propertiesEventBus") final EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void post(final PropertyModifiedEvent propertyChangedEvent) {
		this.eventBus.post(propertyChangedEvent);
	}

	@Override
	public void unregister(final ReloadablePropertyPostProcessor ReloadablePropertyPostProcessor) {
		this.eventBus.unregister(ReloadablePropertyPostProcessor);
	}

	@Override
	public void register(final ReloadablePropertyPostProcessor ReloadablePropertyPostProcessor) {
		this.eventBus.register(ReloadablePropertyPostProcessor);
	}

}
