package com.concurrentperformance.throughput.comms.server;

import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycle;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public interface ServerSocketFactoryObserver {

	void serverSocketFactoryObserver_notifyNewConnection(ConnectionLifecycle connection);

}
