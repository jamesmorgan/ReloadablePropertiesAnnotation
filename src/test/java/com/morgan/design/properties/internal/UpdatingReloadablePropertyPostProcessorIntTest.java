package com.morgan.design.properties.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.morgan.design.properties.testBeans.ReloadingAutowiredPropertyBean;

@ContextConfiguration(locations = { "classpath:/spring/spring-reloading-reloadablePropertyPostProcessorIntTest.xml" })
public class UpdatingReloadablePropertyPostProcessorIntTest extends AbstractJUnit4SpringContextTests {

	private static final String DIR = "target/test-classes/test-files/";
	private static final String PROPERTIES = "reloading.properties";

	@Autowired
	private ReloadingAutowiredPropertyBean bean;

	private Properties loadedProperties;

	@Before
	public void setUp() throws IOException {
		this.loadedProperties = PropertiesLoaderUtils.loadAllProperties(PROPERTIES);
		assertThat(this.bean.getStringProperty(), is("Injected String Value"));
        assertThat(this.bean.getCompositeStringProperty(), is("Hello, World!"));
	}

	@After
	public void cleanUp() throws Exception {
		this.loadedProperties.setProperty("dynamicProperty.stringValue", "Injected String Value");
        this.loadedProperties.setProperty("dynamicProperty.baseStringValue", "World");
        this.loadedProperties.setProperty("dynamicProperty.compoiteStringValue", "Hello, ${dynamicProperty.baseStringValue}!");

		final OutputStream newOutputStream = Files.newOutputStream(new File(DIR + PROPERTIES).toPath(), new OpenOption[] {});
		this.loadedProperties.store(newOutputStream, null);

		Thread.sleep(500); // this is a hack -> I need to find an alternative

		assertThat(this.bean.getStringProperty(), is("Injected String Value"));
        assertThat(this.bean.getCompositeStringProperty(), is("Hello, World!"));
	}

	@Test
	public void shouldReloadAlteredStringProperty() throws Exception {
		assertThat(this.bean.getStringProperty(), is("Injected String Value"));

        this.loadedProperties.setProperty("dynamicProperty.stringValue", "Altered Injected String Value");

		final File file = new File(DIR + PROPERTIES);
		final OutputStream newOutputStream = Files.newOutputStream(file.toPath(), new OpenOption[] {});
		this.loadedProperties.store(newOutputStream, null);
		newOutputStream.flush();
		newOutputStream.close();

		Thread.sleep(500); // this is a hack -> I need to find an alternative

		assertThat(this.bean.getStringProperty(), is("Altered Injected String Value"));
	}

    @Test
    public void shouldReloadAlteredCompositeStringProperty() throws Exception {
        assertThat(this.bean.getCompositeStringProperty(), is("Hello, World!"));

        this.loadedProperties.setProperty("dynamicProperty.compoiteStringValue", "Goodbye, ${dynamicProperty.baseStringValue}!");
        assertThat(this.loadedProperties.getProperty("dynamicProperty.compoiteStringValue"), is("Goodbye, ${dynamicProperty.baseStringValue}!"));

        final File file = new File(DIR + PROPERTIES);
        final OutputStream newOutputStream = Files.newOutputStream(file.toPath(), new OpenOption[] {});
        this.loadedProperties.store(newOutputStream, null);
        newOutputStream.flush();
        newOutputStream.close();

        Thread.sleep(1000); // this is a hack -> I need to find an alternative

        assertThat(this.bean.getCompositeStringProperty(), is("Goodbye, World!"));
    }
}
