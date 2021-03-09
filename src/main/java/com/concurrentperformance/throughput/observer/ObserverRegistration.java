package com.concurrentperformance.throughput.observer;

/**
 * Generic Observer registration.
 *
 * @author Steve Lake
 */
public interface ObserverRegistration<T> {

	void registerObserver(T observer);

	void deregisterObserver(T observer);
}
