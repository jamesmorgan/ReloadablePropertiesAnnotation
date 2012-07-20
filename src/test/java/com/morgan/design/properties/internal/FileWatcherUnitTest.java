package com.morgan.design.properties.internal;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.morgan.design.properties.internal.FileWatcher.EventPublisher;

public class FileWatcherUnitTest {

	private static final String DIR = "target/test-classes/test-files/";
	private static final String PROPERTIES = "fileWatcher.properties";
	private static final String DIR2 = "target/test-classes/test-files2/";
	private static final String PROPERTIES2 = "fileWatcher2.properties";

	private Resource actual;
	private CountDownLatch lock = new CountDownLatch(1);

	final Resource[] singleResource = new Resource[] { new FileSystemResource(DIR + PROPERTIES) };

	final Resource[] multipleResources = new Resource[] { new FileSystemResource(DIR + PROPERTIES), new FileSystemResource(DIR2 + PROPERTIES2) };

	@Test(timeout = 1500)
	public final void testWatchingASingle() throws IOException, InterruptedException {
		final FileWatcher fileWatcher = new FileWatcher(this.singleResource, new EventPublisher() {
			@Override
			@SuppressWarnings("unqualified-field-access")
			public void onResourceChanged(final Resource data) {
				actual = data;
				lock.countDown();
			}
		});
		assertNull(this.actual);

		final Thread thread = new Thread(fileWatcher);
		thread.start();

		final File file = new File(DIR + PROPERTIES);
		Files.write("random string", file, Charsets.UTF_8);

		assertTrue(fileWatcher.isRunning());

		this.lock.await(500, TimeUnit.MILLISECONDS);
		assertNotNull(this.actual);

		fileWatcher.stop();
		this.lock.await(500, TimeUnit.MILLISECONDS);
		assertFalse(fileWatcher.isRunning());
	}

	// TODO Implement once created single thread per directory
	@Ignore
	@Test(timeout = 2000)
	public final void testWatchingMulitipleResourcesInDifferingDirectories() throws IOException, InterruptedException {
		final FileWatcher fileWatcher = new FileWatcher(this.multipleResources, new EventPublisher() {
			@Override
			@SuppressWarnings("unqualified-field-access")
			public void onResourceChanged(final Resource data) {
				actual = data;
				lock.countDown();
			}
		});
		assertNull(this.actual);

		final Thread thread = new Thread(fileWatcher);
		thread.start();

		// Change file 2 (Different Directory)
		Files.write("random string", new File(DIR2 + PROPERTIES2), Charsets.UTF_8);

		this.lock.await(500, TimeUnit.MILLISECONDS);
		assertNotNull(this.actual);

		// reset test
		this.actual = null;
		assertNull(this.actual);

		// Change file 1 (Different Directory)
		Files.write("random string", new File(DIR + PROPERTIES), Charsets.UTF_8);

		this.lock.await(500, TimeUnit.MILLISECONDS);
		assertNotNull(this.actual);

		fileWatcher.stop();
		this.lock.await(500, TimeUnit.MILLISECONDS);
		assertFalse(fileWatcher.isRunning());
	}

}
