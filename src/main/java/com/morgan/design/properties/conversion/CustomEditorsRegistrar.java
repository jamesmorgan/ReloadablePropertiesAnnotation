package com.morgan.design.properties.conversion;

import java.beans.PropertyEditor;
import java.util.Map;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

public class CustomEditorsRegistrar implements PropertyEditorRegistrar {

	private Map<Class<?>, PropertyEditor> extraEditors;
	
	public CustomEditorsRegistrar(Map<Class<?>, PropertyEditor> extraEditors)
	{
		this.extraEditors = extraEditors;
	}
	
	@Override
	public void registerCustomEditors(PropertyEditorRegistry registry) {
		for (Map.Entry<Class<?>, PropertyEditor> entry : extraEditors.entrySet()) {
			registry.registerCustomEditor(entry.getKey(), entry.getValue());
		}
	}

}
