package com.concurrentperformance.throughput.lifecycle.closedown;

import com.concurrentperformance.throughput.lifecycle.Lifecycle;

/**
 * Manage a set of lifecycle components and action the close()
 * method on each in turn when the Closedown.closedown() method is called.
 *
 * @author Steve Lake
 */
public interface Closedown {

	void closedown(final String reason);

	/**
	 * Add a component that will be stopped when the closedown() method
	 * is called.
	 * The priority is used to determine the shutdown order during the shutdown process.
	 * Components will be shut down from the lowest number to the lowest.
	 *
	 * @param stoppableComponent
	 * @param priority
	 */
	void add(Lifecycle stoppableComponent, Integer priority);

	/**
	 * Remove a component.
	 *
	 * @param stoppableComponent
	 */
	void remove(Lifecycle stoppableComponent);
}
