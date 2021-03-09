package com.concurrentperformance.throughput.comms.server.impl;

import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycle;
import com.concurrentperformance.throughput.comms.server.ServerSocketFactory;
import com.concurrentperformance.throughput.comms.server.ServerSocketFactoryObserver;
import com.concurrentperformance.throughput.comms.server.SocketConnectionListener;
import com.concurrentperformance.throughput.comms.server.SocketConnectionReceiver;
import com.concurrentperformance.throughput.identity.Identity;
import com.concurrentperformance.throughput.observer.impl.DefaultObserverRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

import static com.concurrentperformance.throughput.precondition.PortPrecondition.checkPort;
import static com.google.common.base.Preconditions.checkState;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public abstract class SkeletalServerSocketFactory extends DefaultObserverRegistration<ServerSocketFactoryObserver>
		implements ServerSocketFactory, SocketConnectionReceiver {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private String iAmListeningFor;
	private Identity iAmA;
	private int port;
	private volatile int connectionLauncherIndex = 0;

	private SocketConnectionListener socketConnectionListener;

	@Override
	public boolean start() {
		// build and start the socketConnectionListener that will listen on the port.
		checkPort(port, "port");
		checkState(iAmListeningFor != null, "iAmListeningFor must not be null");
		checkState(iAmA != null, "iAmA must not be null");
		checkState(!isRunning(), "can't start as already running");

		socketConnectionListener = new SocketConnectionListener(port, this, getThreadName());
		return socketConnectionListener.start();
	}

	@Override
	public boolean isRunning() {
		return (socketConnectionListener != null && socketConnectionListener.isRunning());
	}

	@Override
	public boolean stop() {
		checkState(isRunning(), "can't stop as not running");
		return socketConnectionListener.stop();
	}

	protected abstract String getThreadName();

	@Override
	public void receiveSocketConnection(final Socket toRemoteClient) {

		// Create a new thread to launch the connection. This protects the Socket listening thread
		// from getting permenantly blocked if anything goes wrong with the creation of the socket connection
		// Instead, we block this throw-away thread. Yes, it is more expensive, but much safer.

		final String threadName = iAmA.toString() + ">" + iAmListeningFor + "-Launch-" + connectionLauncherIndex++;
		new Thread(new Runnable() {
			@Override
			public void run() {
				log.info("Starting connection [{}]", threadName);
				try {
					ConnectionLifecycle connection = buildConnection(toRemoteClient);
					notifyNewConnection(connection);
				}
				catch (Exception e) {
					log.error("Problem starting connection", e);
				}
				log.info("Completed starting connection [{}]", threadName);
			}

		}, threadName).start();



	}

	protected abstract ConnectionLifecycle buildConnection(Socket toRemoteClient) throws IOException;

	protected void notifyNewConnection(final ConnectionLifecycle connection) {
		submitTask(new Runnable() {
			@Override
			public void run() {
				for (ServerSocketFactoryObserver observer : getObservers()) {
					observer.serverSocketFactoryObserver_notifyNewConnection(connection);
				}
			}
		});
	}

	protected String getIAmListeningFor() {
		return iAmListeningFor;
	}

	public void setIAmListeningFor(String iAmListeningFor) {
		this.iAmListeningFor = iAmListeningFor;
	}

	protected Identity getIAmA() {
		return iAmA;
	}

	public void setIAmA(Identity iAmA) {
		this.iAmA = iAmA;
	}

	protected int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "[" + getIAmListeningFor() + "] connections on port [" + getPort() + "] for [" + this.getClass().getSimpleName() + "]";
	}
}