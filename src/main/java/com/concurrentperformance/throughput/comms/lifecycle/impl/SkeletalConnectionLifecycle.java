package com.concurrentperformance.throughput.comms.lifecycle.impl;

import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycle;
import com.concurrentperformance.throughput.concurrent.AtomicStateHolder;
import com.concurrentperformance.throughput.identity.Identity;
import com.concurrentperformance.throughput.thread.ThreadNameFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Phaser;

import static com.concurrentperformance.throughput.precondition.IdentityPrecondition.checkIdentity;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Partial implementation of the ConnectionLifecycle interface. Provides a state
 * driven framework that allows the implementer to decide what protocol is used,
 * and then to use that protocol to transport the state messages from one
 * end of the socket to the other,
 */
public abstract class SkeletalConnectionLifecycle extends ConnectionNotification implements ConnectionLifecycle {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Identity iAmA;
	private volatile Identity iAmConnectingTo;

	private volatile Socket socket;
	private volatile String socketName;

	private final Phaser handshakeReceivedFromRemote = new Phaser(2);

	public enum ConnectionState {
		STOPPED,
		STARTING,
		RUNNING,
		IMMINENT_STOP_FROM_REMOTE,
		STOPPING;
	}

	private final AtomicStateHolder<ConnectionState> state;

	protected SkeletalConnectionLifecycle(Identity iAmA) {
		this.iAmA = checkNotNull(iAmA, "iAmA must not be null");
		checkIdentity(iAmA, "iAmA must be a legal format");

		state = new AtomicStateHolder<>(ConnectionState.STOPPED, log, "Connection:" + iAmA);
	}

	@Override
	public boolean start() {
		throw new UnsupportedOperationException("Use void start(Socket socket) instead");
	}

	@Override
	public void start(Socket socket) throws IOException {
		log.info("About to start connection [" + this + "] with [" + socket + "]. State[" + state + "]");

		checkState(handshakeReceivedFromRemote.getUnarrivedParties() == 2, "handshakeReceivedFromRemote.getUnarrivedParties() must be 2");
		this.socket = checkNotNull(socket, "socket must not be null [" + this + "]");
		this.socketName = ThreadNameFactory.getThreadName(socket);

		// We change state after the check on socket being null, as we can cope better with a non null socket than an inconsistent state
		state.compareAndSetOrThrow(ConnectionState.STOPPED, ConnectionState.STARTING);

		startComponents(socket, socketName);

		log.debug("Sending handshake");
		sendHandshakeToRemote(iAmA);

		log.debug("Waiting for remote handshake");
		waitForHandshakeFromRemote();

		log.debug("Updating thread names.");
		updateThreadName();

		state.compareAndSetOrThrow(ConnectionState.STARTING, ConnectionState.RUNNING);
		log.info("Started connection [" + this + "] with [" + socket + "]. State[" + state + "]");

		notifyStarted();
	}

	protected abstract void startComponents(Socket socket, String baseThreadName) throws IOException;

	protected abstract void sendHandshakeToRemote(Identity iAmA) throws IOException;

	protected void receiveHandshakeFromRemote(Identity iAmConnectingTo) {
		this.iAmConnectingTo = iAmConnectingTo;
		handshakeReceivedFromRemote.arrive();
		log.info("Handshake received [" + this + "]");
	}

	private void waitForHandshakeFromRemote() {
		handshakeReceivedFromRemote.arriveAndAwaitAdvance();
		log.trace("Handshake received - continuing");
	}

	@Override
	public boolean stop() {
		/*
		 * We return if we are stopped, or stopping because of two related cases:
		 * 1) We have an abnormal stop, and that may cause both the input and the output
		 * streams to throw exceptions, and we only want a single abnormalStop triggered
		 * else the second may interfere with the state transition of the first.
		 * 2) We have a normal stop, but killing the socket causes 1).
		 */
		if (isStoppedOrStopping()) {
			log.info("ConnectionLifecycle already stopped [" + this + "]");
			return true;
		}
		log.info("External initiated stop [" + this + "]");

		signalImminentStopToRemoteSocket();
		doStop(true);
		return true;
	}

	protected abstract void signalImminentStopToRemoteSocket();

	public void imminentStop() {
		log.info("Imminent Stop [" + this + "]");
		state.set(ConnectionState.IMMINENT_STOP_FROM_REMOTE);
	}

	public void stopWithException(Exception cause, String msg, Logger otherLogger) {
		/*
		 * We do a normal stop if in IMMINENT_STOP_FROM_REMOTE state as this is
		 * an expected stop that the other side told us about.
		 */
		if (state.equals(ConnectionState.IMMINENT_STOP_FROM_REMOTE)) {
			otherLogger.debug("Expected " + msg);
			log.info("Stopping connection [" + this + "]");
			doStop(true);
		}
		/*
		 * We return if we are stopped or stopping because of two related cases:
		 * 1) We have an abnormal stop and that may cause both the input and the output
		 * streams to throw exceptions and we only want a single abnormalStop triggered
		 * else the second may interfere with the stat transition of thr first.
		 * 2) We have a normal stop, but killing the socket causes 1).
		 */

		else if (isStoppedOrStopping()) {
			otherLogger.debug("An expected " + msg);
			return;
		}
		else {
			otherLogger.error(msg, cause);
			log.error("Abnormal stop for [" + this + "]" );
			doStop(false);
		}
	}

	private void doStop(boolean expected) {
		// several threads may compete for this method, so it (and any methods it calls)
		// has to handle multiple overlapping executions.
		state.set(ConnectionState.STOPPING);

		stopSocket();
		stopComponents();

		state.set(ConnectionState.STOPPED);

		notifyStopped(expected);

		iAmConnectingTo = null;

		log.info("Stopped [" + this + "]" );
	}

	protected abstract void stopComponents();

	private void stopSocket() {
		log.debug("Socket stopping [" + this + "]" );
		final Socket socketLocal = this.socket;
		if (socketLocal != null) {
			try {
				socketLocal.close();
			} catch (IOException e) {
				log.error("Problem closing socket socket [" + socketLocal + "] in stop. [" + this + "]", e);
			}
		}
		this.socket = null;
		socketName = null;
		log.debug("Socket stopped [" + this + "]" );
	}

	public boolean isStoppedOrStopping() {
		boolean stoppedOrStopping = state.equals(ConnectionState.STOPPED) ||
									state.equals(ConnectionState.STOPPING);
		return stoppedOrStopping;
	}

	@Override
	public boolean isRunning() {
		boolean stopped = state.equals(ConnectionState.STOPPED);
		return !stopped;
	}

	private void updateThreadName() {
		String baseThreadName = "";
		if (socketName != null) {
			baseThreadName += socketName;
		}
		if (socketName != null && iAmConnectingTo != null) {
			baseThreadName += ":";
		}
		if (iAmConnectingTo != null) {
			baseThreadName += iAmConnectingTo;
		}

		doUpdateThreadName(baseThreadName);
	}

	protected abstract void doUpdateThreadName(String baseThreadName);

	@Override
	public Identity getIAmA() {
		return iAmA;
	}

	@Override
	public Identity getIAmConnectingTo() {
		return iAmConnectingTo;
	}

	@Override
	public String toString() { 
		return "" + this.getClass().getSimpleName() +
				" [" + iAmA  + ">" + iAmConnectingTo +"," + state + "]";
	}
}