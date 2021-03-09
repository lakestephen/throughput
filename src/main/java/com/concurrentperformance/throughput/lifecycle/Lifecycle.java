package com.concurrentperformance.throughput.lifecycle;

/**
 * Common interface for components that can be started and stopped.
 *
 * @author Steve Lake
 */
public interface Lifecycle {

	/**
	 * Start this component.
	 * @return true if successful
	 */
	boolean start();

	/**
	 * @return true if running
	 */
	boolean isRunning();

	/**
	 * Stop this component.
	 * @return true if successful
	 */
	boolean stop();
}
