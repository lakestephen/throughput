package com.concurrentperformance.throughput.comms.server.closedown;

import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycle;
import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycleObserver;
import com.concurrentperformance.throughput.comms.server.ServerSocketFactory;
import com.concurrentperformance.throughput.comms.server.ServerSocketFactoryObserver;
import com.concurrentperformance.throughput.lifecycle.closedown.Closedown;

/**
 * Keeps the closedown component updated with connections
 * that have started but not stopped.
 *
 * @author Steve Lake
 */
public class ServerSocketFactoryClosedownUpdater {

	private Closedown closedown;
	private int priority = 100;

	public void setServerSocketFactory(ServerSocketFactory serverSocketFactory) {
		serverSocketFactory.registerObserver(new ServerSocketFactoryObserver() {
			@Override
			public void serverSocketFactoryObserver_notifyNewConnection(ConnectionLifecycle connection) {
				newConnection(connection);
			}
		});
	}

	private void newConnection(ConnectionLifecycle connection) {
		closedown.add(connection, priority);
		connection.registerObserver(new ConnectionLifecycleObserver() {
			@Override
			public void connectionLifecycleObserver_started(ConnectionLifecycle connection) {
			}

			@Override
			public void connectionLifecycleObserver_stopped(ConnectionLifecycle connection, boolean expected) {
				closedown.remove(connection);
			}
		});
	}

	public void setClosedown(Closedown closedown) {
		this.closedown = closedown;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
}
