package com.morgan.design.properties.internal;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

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

	public FileWatcher(final Resource[] locations, final EventPublisher eventPublisher) {
		this.locations = locations;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void run() {
		while (true) {
			for (final Resource resource : this.locations) {
				try {
					final Path path = Paths.get(resource.getFile()
						.getParentFile()
						.toURI());
					final WatchService watchService = FileSystems.getDefault()
						.newWatchService();

					final WatchKey basePathWatchKey = path.register(watchService, ENTRY_MODIFY);

					final String resourceName = resource.getFilename();

					try {
						final WatchKey watchKey = watchService.take(); // Await modification

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
				}
			}
		}
	}

	private boolean isSameTargetFile(final String resourceName, final Path target) {
		return target.getFileName()
			.toString()
			.equals(resourceName);
	}
}
