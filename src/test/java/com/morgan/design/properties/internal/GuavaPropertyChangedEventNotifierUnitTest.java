package com.morgan.design.properties.internal;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.eventbus.EventBus;
import com.morgan.design.properties.bean.PropertyModifiedEvent;

@RunWith(JMock.class)
@SuppressWarnings("unqualified-field-access")
public class GuavaPropertyChangedEventNotifierUnitTest {

	Mockery context = new JUnit4Mockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	private GuavaPropertyChangedEventNotifier eventNotifier;

	private EventBus eventBus;

	@Before
	public void setUp() throws Exception {
		this.eventBus = this.context.mock(EventBus.class);
		this.eventNotifier = new GuavaPropertyChangedEventNotifier(this.eventBus);
	}

	@Test
	public void shouldPostWhenEventFound() {
		final PropertyModifiedEvent event = new PropertyModifiedEvent("", new Object(), new Object());
		this.context.checking(new Expectations() {
			{
				oneOf(eventBus).post(event);
			}
		});
		this.eventNotifier.post(event);
	}

	@Test
	public void shouldRegisterNewRegistery() {
		final ReloadablePropertyPostProcessor registery = new ReloadablePropertyPostProcessor(null, null, null);
		this.context.checking(new Expectations() {
			{
				oneOf(eventBus).register(registery);
			}
		});
		this.eventNotifier.register(registery);
	}

	@Test
	public void shouldUnregisterRegistery() {
		final ReloadablePropertyPostProcessor registery = new ReloadablePropertyPostProcessor(null, null, null);
		this.context.checking(new Expectations() {
			{
				oneOf(eventBus).unregister(registery);
			}
		});
		this.eventNotifier.unregister(registery);
	}
}
