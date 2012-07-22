package com.morgan.design.properties.internal;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.google.common.collect.Maps;

public class PropertiesWatcher implements Runnable {

	protected static Logger log = LoggerFactory.getLogger(PropertiesWatcher.class);

	public interface EventPublisher {
		void onResourceChanged(Resource resource);
	}

	private final Resource[] locations;
	private final EventPublisher eventPublisher;

	private WatchService watchService;
	final ExecutorService service;

	public PropertiesWatcher(final Resource[] locations, final EventPublisher eventPublisher) throws IOException {
		this.locations = locations;
		this.eventPublisher = eventPublisher;
		this.watchService = FileSystems.getDefault()
			.newWatchService();
		this.service = Executors.newCachedThreadPool();
	}

	@Override
	public void run() {
		final Map<Path, List<Resource>> pathsAndResources = findAvailableResourcePaths();
		for (final Path pathToWatch : pathsAndResources.keySet()) {
			final List<Resource> availableResources = pathsAndResources.get(pathToWatch);
			log.debug("Starting ResourceWatcher on file {}", availableResources);
			this.service.submit(new ResourceWatcher(pathToWatch, availableResources));
		}
	}

	public void stop() {
		try {
			log.debug("Closing File Watching Service");
			this.watchService.close();

			log.debug("Shuting down Thread Service");
			this.service.shutdownNow();
		}
		catch (final IOException e) {
			log.error("Unable to stop file watcher", e);
		}
	}

	private Map<Path, List<Resource>> findAvailableResourcePaths() {
		final Map<Path, List<Resource>> map = Maps.newHashMap();
		for (final Resource resource : this.locations) {
			final Path resourceParentPath = getResourceParentPath(resource);
			if (null == map.get(resourceParentPath)) {
				map.put(resourceParentPath, new ArrayList<Resource>());
			}
			map.get(resourceParentPath)
				.add(resource);
		}
		return map;
	}

	private Path getResourceParentPath(final Resource resource) {
		try {
			return Paths.get(resource.getFile()
				.getParentFile()
				.toURI());
		}
		catch (final IOException e) {
			log.error("Unable to get resource path", e);
		}
		return null;
	}

	private void publishResourceChangedEvent(final Resource resource) {
		this.eventPublisher.onResourceChanged(resource);
	}

	private WatchService getWatchService() {
		return this.watchService;
	}

	private class ResourceWatcher implements Runnable {

		private final Path path;
		private final List<Resource> resources;

		public ResourceWatcher(final Path path, final List<Resource> resources) {
			this.path = path;
			this.resources = resources;
		}

		@Override
		public void run() {
			try {
				log.debug("START");
				log.debug("Watching for modifcation events for path {}", this.path.toString());
				while (!Thread.currentThread()
					.isInterrupted()) {
					final WatchKey pathBeingWatched = this.path.register(getWatchService(), ENTRY_MODIFY);

					WatchKey watchKey = null;
					try {
						watchKey = getWatchService().take();
					}
					catch (final ClosedWatchServiceException | InterruptedException e) {
						log.debug("END");
						Thread.currentThread()
							.interrupt();
					}

					if (watchKey != null) {
						for (final WatchEvent<?> event : pathBeingWatched.pollEvents()) {
							log.debug("File modification Event Triggered");
							final Path target = path(event.context());
							if (isValidTargetFile(target)) {
								final Path watchedPath = path(watchKey.watchable());
								final Kind<?> eventKind = event.kind();

								logNewEvent(watchedPath, eventKind, target);
								publishResourceChangedEvent(getResource(target));
							}
						}
						if (!watchKey.reset()) {
							log.debug("END");
							Thread.currentThread()
								.interrupt();
							return;
						}
					}
				}
			}
			catch (final Exception e) {
				log.error("Exception thrown when watching resources, path {}\nException:", this.path.toString(), e.getMessage());
				stop();
			}
		}

		private void logNewEvent(final Path watchedPath, final Kind<?> eventKind, final Path target) {
			log.debug("Watched Resource changed, modified file [{}]", target.getFileName()
				.toString());
			log.debug("  Event Kind [{}]", eventKind);
			log.debug("      Target [{}]", target);
			log.debug("Watched Path [{}]", watchedPath);
		}

		private Path path(final Object object) {
			return (Path) object;
		}

		private boolean isValidTargetFile(final Path target) {
			for (final Resource resource : this.resources) {
				if (pathMatchesResource(target, resource)) {
					return true;
				}
			}
			return false;
		}

		public Resource getResource(final Path target) {
			for (final Resource resource : this.resources) {
				if (pathMatchesResource(target, resource)) {
					return resource;
				}
			}
			return null;
		}

		private boolean pathMatchesResource(final Path target, final Resource resource) {
			return target.getFileName()
				.toString()
				.equals(resource.getFilename());
		}
	}

}
