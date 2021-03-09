package com.concurrentperformance.throughput.comms.lifecycle.closedown;

import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycle;
import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycleObserver;
import com.concurrentperformance.throughput.lifecycle.closedown.Closedown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor a specific vital connection whos loss will trigger a closedown.
 *
 * @author Steve Lake
 */
public class ConnectionClosedownMonitor {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private Closedown closedown;

	public void setConnection(ConnectionLifecycle connection) {
		log.info("Monitoring vital connection [" + connection  + "]");
		connection.registerObserver(new ConnectionLifecycleObserver() {
			@Override
			public void connectionLifecycleObserver_started(ConnectionLifecycle connection) {
			}

			@Override
			public void connectionLifecycleObserver_stopped(ConnectionLifecycle closingConnection, boolean expected) {
				closedown(closingConnection);
			}
		});
	}

	private void closedown(ConnectionLifecycle closingConnection) {
		String msg = "Vital connection [" + closingConnection + "] has stopped.";
		closedown.closedown(msg);
	}

	public void setClosedown(Closedown closedown) {
		this.closedown = closedown;
	}
}
