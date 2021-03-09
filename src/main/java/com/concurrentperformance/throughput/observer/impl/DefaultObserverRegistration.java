package com.concurrentperformance.throughput.observer.impl;

import com.concurrentperformance.throughput.observer.ObserverRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper base class for managing observers.
 *
 * @author Steve Lake
 */
public abstract class DefaultObserverRegistration<T> implements ObserverRegistration<T> {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private Set<T> observers =
			Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());

	@Override
	public void registerObserver(T observer) {
		final boolean added = observers.add(observer);
		if (!added) {
			log.warn("Duplicate registerObserver of [" + observer + "]");
		}
	}

	@Override
	public void deregisterObserver(T observer) {
		observers.remove(observer);
	}

	public Set<T> getObservers() {
		return observers;
	}

	public void submitTask(Runnable task) {
		// Execute synchronously, but provides a common interface for subclasses.
		task.run();
	}
}
