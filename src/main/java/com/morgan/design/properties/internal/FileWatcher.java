package com.morgan.design.properties.internal;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class FileWatcher implements Runnable {

	protected static Logger log = LoggerFactory.getLogger(FileWatcher.class);

	public interface EventPublisher {
		void onResourceChanged(Resource resource);
	}

	private final Resource[] locations;
	private final EventPublisher eventPublisher;

	private boolean exit;
	private WatchService watchService;

	public FileWatcher(final Resource[] locations, final EventPublisher eventPublisher) throws IOException {
		this.locations = locations;
		this.eventPublisher = eventPublisher;
		this.exit = false;
		this.watchService = FileSystems.getDefault()
			.newWatchService();
	}

	@Override
	public void run() {
		while (doNotExit()) {
			for (final Resource resource : this.locations) {
				try {
					final Path path = Paths.get(resource.getFile()
						.getParentFile()
						.toURI());

					final WatchKey basePathWatchKey = path.register(this.watchService, ENTRY_MODIFY);

					final String resourceName = resource.getFilename();

					try {
						final WatchKey watchKey = this.watchService.take(); // Await modification

						for (final WatchEvent<?> event : basePathWatchKey.pollEvents()) {
							final Path watchedPath = (Path) watchKey.watchable();
							final Kind<?> eventKind = event.kind();// returns the event type
							final Path target = (Path) event.context();// returns the context of the event

							if (isSameTargetFile(resourceName, target)) {
								log.debug("File modification heard");
								log.debug("Target [{}]", target);
								log.debug("Event Kind [{}]", eventKind);
								log.debug("Watched Path [{}]", watchedPath);
								this.eventPublisher.onResourceChanged(resource);
							}
						}
						final boolean valid = null != watchKey && watchKey.reset();
						if (!valid) {
							break;
						}
					}
					catch (final InterruptedException e) {
						Thread.currentThread()
							.interrupt();
					}
				}
				catch (final Exception e) {
					log.error("Exception thrown when watching resources, fileName: " + resource.getFilename(), e);
					stop();
				}
			}
		}
	}

	public void stop() {
		try {
			log.debug("Stopping file watcher");
			this.watchService.close();
		}
		catch (final IOException e) {
			log.error("Unable to stop file watcher", e);
		}
		this.exit = true;
	}

	private boolean doNotExit() {
		return !this.exit;
	}

	public boolean isRunning() {
		return this.exit == false;
	}

	private boolean isSameTargetFile(final String resourceName, final Path target) {
		return target.getFileName()
			.toString()
			.equals(resourceName);
	}
}
