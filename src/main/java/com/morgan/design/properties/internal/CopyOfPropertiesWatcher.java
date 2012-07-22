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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CopyOfPropertiesWatcher implements Runnable {

	protected static Logger log = LoggerFactory.getLogger(CopyOfPropertiesWatcher.class);

	public interface EventPublisher {
		void onResourceChanged(Resource resource);
	}

	private final Resource[] locations;
	private final EventPublisher eventPublisher;

	private boolean exit;
	private WatchService watchService;
	private List<Thread> runningWatchers;

	public CopyOfPropertiesWatcher(final Resource[] locations, final EventPublisher eventPublisher) throws IOException {
		this.locations = locations;
		this.eventPublisher = eventPublisher;
		this.exit = false;
		this.runningWatchers = Lists.newArrayList();
		this.watchService = FileSystems.getDefault()
			.newWatchService();
	}

	@Override
	public void run() {
		final Map<Path, List<Resource>> pathsAndResources = findAvailableResourcePaths();

		for (final Path pathToWatch : pathsAndResources.keySet()) {
			final List<Resource> availableResources = pathsAndResources.get(pathToWatch);
			final Thread thread = new Thread(new ResourceWatcher(pathToWatch, availableResources));
			this.runningWatchers.add(thread);
			thread.start();
			log.debug("Starting ResourceWatcher on file {}", availableResources);
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

	public boolean isRunning() {
		return this.exit == false;
	}

	private boolean doNotExit() {
		return !this.exit;
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
				while (doNotExit()) {
					log.debug("Watching for modifcation events from path {}", this.path.toString());
					final WatchKey basePathWatchKey = this.path.register(getWatchService(), ENTRY_MODIFY);

					try {
						final WatchKey watchKey = getWatchService().take(); // Await modification

						for (final WatchEvent<?> event : basePathWatchKey.pollEvents()) {
							final Path watchedPath = (Path) watchKey.watchable();
							final Kind<?> eventKind = event.kind();// returns the event type
							final Path target = (Path) event.context();// returns the context of the event

							log.debug("File modification Event Triggered");
							if (isValidTargetFile(target)) {
								log.debug("Resource Change is a known properties file {}", target.getFileName()
									.toString());
								log.debug("Target [{}]", target);
								log.debug("Event Kind [{}]", eventKind);
								log.debug("Watched Path [{}]", watchedPath);
								publishResourceChangedEvent(getResource(target));
							}
						}
						final boolean valid = null != watchKey && watchKey.reset();
						if (!valid) {
							stop();
							return;
						}
					}
					catch (final InterruptedException e) {
						e.printStackTrace();
						Thread.currentThread()
							.interrupt();
					}
				}
			}
			catch (final Exception e) {
				log.error("Exception thrown when watching resources, fileName: TODO", e);
				stop();
			}
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
