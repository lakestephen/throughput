package com.concurrentperformance.throughput.comms.lifecycle.impl;

import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycle;
import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycleObserver;
import com.concurrentperformance.throughput.observer.impl.DefaultObserverRegistration;

/**
 * Manage the ConnectionLifecycleListeners.
 *
 * @author Steve Lake
 */
public abstract class ConnectionNotification extends DefaultObserverRegistration<ConnectionLifecycleObserver>
		implements ConnectionLifecycle {

	protected void notifyStarted() {
		submitTask(() -> {
			for (ConnectionLifecycleObserver observer : getObservers()) {
				observer.connectionLifecycleObserver_started(ConnectionNotification.this);
			}
		});
	}

	protected void notifyStopped(final boolean expected) {
		submitTask(() -> {
			for (ConnectionLifecycleObserver observer : getObservers()) {
				observer.connectionLifecycleObserver_stopped(ConnectionNotification.this, expected);
			}
		});
	}
}
