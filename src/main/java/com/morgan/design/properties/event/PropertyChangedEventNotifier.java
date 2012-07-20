package com.morgan.design.properties.event;

import com.morgan.design.properties.bean.PropertyModifiedEvent;
import com.morgan.design.properties.internal.ReloadablePropertyPostProcessor;

public interface PropertyChangedEventNotifier {

	void post(PropertyModifiedEvent propertyChangedEvent);

	void unregister(ReloadablePropertyPostProcessor reloadablePropertyProcessor);

	void register(ReloadablePropertyPostProcessor reloadablePropertyProcessor);

}
