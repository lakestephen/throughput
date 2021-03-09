package com.concurrentperformance.throughput.observer.impl;

import com.concurrentperformance.throughput.lifecycle.Lifecycle;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Helper Baser class that will allow the submission of tasks to be
 * executed asynchronously.
 *
 * @author Steve Lake
 */
public class AsyncObserverRegistration<T> extends DefaultObserverRegistration<T> implements Lifecycle {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final String nameFormat = this.getClass().getSimpleName() + "-notify";
	private final ExecutorService executor = Executors.newSingleThreadExecutor(
			new ThreadFactoryBuilder()
					.setNameFormat(nameFormat) //If using more than one thread, then can ise %d to number them
					.build());

	public void submitTask(Runnable task) {
		executor.execute(task);
	}

	@Override
	public boolean start() {
		return true;
	}

	@Override
	public boolean isRunning() {
		return !executor.isShutdown();
	}

	@Override
	public boolean stop() {
		log.debug("Shutting down executor [" + nameFormat + "]");
		executor.shutdown();
		try {
			executor.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
		}
		return true;
	}
}
