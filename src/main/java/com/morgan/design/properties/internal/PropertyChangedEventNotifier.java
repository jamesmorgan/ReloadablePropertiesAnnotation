package com.morgan.design.properties.internal;

import com.morgan.design.properties.bean.PropertyModifiedEvent;

public interface PropertyChangedEventNotifier {

	void post(PropertyModifiedEvent propertyChangedEvent);

	void unregister(ReloadablePropertyPostProcessor reloadablePropertyProcessor);

	void register(ReloadablePropertyPostProcessor reloadablePropertyProcessor);

}
