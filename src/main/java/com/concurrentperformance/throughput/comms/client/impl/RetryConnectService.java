package com.concurrentperformance.throughput.comms.client.impl;

import com.concurrentperformance.throughput.comms.client.ConnectHelper;
import com.concurrentperformance.throughput.comms.client.ConnectService;
import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycle;
import com.concurrentperformance.throughput.retry.CallableRetry;
import com.concurrentperformance.throughput.retry.RetryStrategy;
import com.concurrentperformance.throughput.retry.impl.RegularIntervalRetryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.Callable;

import static com.concurrentperformance.throughput.precondition.PortPrecondition.checkPort;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public class RetryConnectService implements ConnectService { //TODO where is this class used? and how should teh connection be started?

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private String serverHost = "127.0.0.1";
	private int serverPort;
	private ConnectionLifecycle connection;
	private ConnectHelper connectHelper = new DefaultConnectHelper();
	private RetryStrategy retryStrategy = new RegularIntervalRetryStrategy(10, 1000);

	@Override
	public boolean start()  {
		checkNotNull(connection , "connection must not be null");
		checkNotNull(retryStrategy, "retryStrategy must not be null");
		checkNotNull(connectHelper, "connectHelper must not be null");
		checkNotNull(serverHost, "serverHost must not be null");
		checkPort(serverPort, "serverPort");

		Callable<Void> connectTask = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Socket socketToServer = connectHelper.startSocketToServer(serverHost, serverPort);
				connection.start(socketToServer);
				return null;
			}
		};

		CallableRetry<Void> callableRetry = new CallableRetry<Void>(connectTask, retryStrategy, "Connect");

		try {
			callableRetry.call();
		} catch(Exception e) {
			log.warn("Connect failed", e);
		}

		return isRunning();
	}

	@Override
	public boolean isRunning() {
		return connection.isRunning();
	}

	@Override
	public boolean stop() {
		checkNotNull(connection , "connection must not be null");
		connection.stop();
		return true;
	}

	/**
	 * Defaults to loopback '127.0.0.1'
	 * @param serverHost
	 */
	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public void setConnection(ConnectionLifecycle connection) {
		this.connection = connection;
	}

	public void setConnectHelper(ConnectHelper connectHelper) {
		this.connectHelper = connectHelper;
	}
}
