package com.concurrentperformance.throughput.comms.lifecycle;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public interface ConnectionLifecycleObserver {

	void connectionLifecycleObserver_started(ConnectionLifecycle connection);

	void connectionLifecycleObserver_stopped(ConnectionLifecycle connection, boolean expected);
}
