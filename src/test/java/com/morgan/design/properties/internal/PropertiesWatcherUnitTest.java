package com.morgan.design.properties.internal;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.morgan.design.properties.internal.PropertiesWatcher.EventPublisher;

public class PropertiesWatcherUnitTest {

	private static final int _1_SEC = 1000;
	private static final int _2_SEC = 2000;

	private Resource actual;
	private CountDownLatch lock = new CountDownLatch(0);

	final File propertiesFile1 = new File("target/test-classes/test-files/fileWatcher.properties");
	final File propertiesFile2 = new File("target/test-classes/test-files/different_fileWatcher.properties");
	final File propertiesFile3 = new File("target/test-classes/test-files2/fileWatcher2.properties");

	final Resource[] singleResource = new Resource[] { new FileSystemResource(this.propertiesFile1) };
	final Resource[] multiResourceSameDir = new Resource[] { new FileSystemResource(this.propertiesFile1), new FileSystemResource(this.propertiesFile2) };
	final Resource[] multiResourceDifferentDir = new Resource[] { new FileSystemResource(this.propertiesFile1), new FileSystemResource(this.propertiesFile3) };

	@Test
	public final void testWatchingASingle() throws IOException, InterruptedException {
		resetCountDownLatch(this.singleResource.length);

		final PropertiesWatcher propertiesWatcher = createPropertiesWatcher(this.singleResource);
		confirmTestDataNotSet();

		startPropertiesWatcher(propertiesWatcher);
		wait(_1_SEC);

		modifyPropertiesFile(this.propertiesFile1);
		wait(_2_SEC);
		confirmPropertiesFileModified(this.propertiesFile1);

		propertiesWatcher.stop();
		wait(_2_SEC);
	}

	@Test
	public final void testWatchingMulitipleResourcesInDifferingDirectories() throws IOException, InterruptedException {
		resetCountDownLatch(this.multiResourceDifferentDir.length);

		final PropertiesWatcher propertiesWatcher = createPropertiesWatcher(this.multiResourceDifferentDir);
		confirmTestDataNotSet();

		startPropertiesWatcher(propertiesWatcher);
		wait(_1_SEC);

		modifyPropertiesFile(this.propertiesFile3);
		wait(_2_SEC);
		confirmPropertiesFileModified(this.propertiesFile3);

		resetTestData();
		confirmTestDataNotSet();

		modifyPropertiesFile(this.propertiesFile1);
		wait(_2_SEC);
		confirmPropertiesFileModified(this.propertiesFile1);

		propertiesWatcher.stop();
		wait(_1_SEC);
	}

	@Test
	public final void testModifyingADifferentResoruceInSameDirectory() throws IOException, InterruptedException {
		resetCountDownLatch(this.multiResourceSameDir.length);

		final PropertiesWatcher propertiesWatcher = createPropertiesWatcher(this.multiResourceSameDir);
		confirmTestDataNotSet();

		startPropertiesWatcher(propertiesWatcher);
		wait(_1_SEC);

		modifyPropertiesFile(this.propertiesFile1);
		wait(_2_SEC);
		confirmPropertiesFileModified(this.propertiesFile1);

		resetTestData();
		confirmTestDataNotSet();

		modifyPropertiesFile(this.propertiesFile2);
		wait(_2_SEC);
		confirmPropertiesFileModified(this.propertiesFile2);

		propertiesWatcher.stop();
		wait(_1_SEC);
	}

	private void resetCountDownLatch(final int count) {
		this.lock = new CountDownLatch(count);
	}

	private void startPropertiesWatcher(final PropertiesWatcher propertiesWatcher) {
		Executors.newSingleThreadExecutor()
			.execute(propertiesWatcher);
	}

	private void resetTestData() {
		this.actual = null;
	}

	private void confirmTestDataNotSet() {
		assertNull(this.actual);
	}

	private void confirmPropertiesFileModified(final File file) {
		assertNotNull(this.actual);
		assertThat(this.actual.getFilename(), is(file.getName()));
	}

	private void wait(final int millis) throws InterruptedException {
		this.lock.await(millis, TimeUnit.MILLISECONDS);
	}

	private void modifyPropertiesFile(final File file) throws IOException {
		Files.write("random string", file, Charsets.UTF_8);
	}

	private PropertiesWatcher createPropertiesWatcher(final Resource[] resources) throws IOException {
		final PropertiesWatcher propertiesWatcher = new PropertiesWatcher(resources, new EventPublisher() {
			@Override
			@SuppressWarnings("unqualified-field-access")
			public void onResourceChanged(final Resource data) {
				actual = data;
				lock.countDown();
			}
		});
		return propertiesWatcher;
	}
}
