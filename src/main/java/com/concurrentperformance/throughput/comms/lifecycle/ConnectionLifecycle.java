package com.concurrentperformance.throughput.comms.lifecycle;

import com.concurrentperformance.throughput.identity.Identity;
import com.concurrentperformance.throughput.lifecycle.Lifecycle;
import com.concurrentperformance.throughput.observer.ObserverRegistration;
import org.slf4j.Logger;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.net.Socket;



/**
 * A ConnectionLifecycle manages the lifecycle of a Socket connection. It is used on both
 * the client and the server end of a Socket. Either a new instance can be created
 * with each new Socket connection, or the same instance can be started and stopped
 * with each new Socket connection.
 *
 * This interface is designed to be extended to implement the actual transport behaviour,
 * while maintaining consistent lifecycle control between implementations.
 *
 * @author Steve Lake
 */
@NotThreadSafe
public interface ConnectionLifecycle extends ObserverRegistration<ConnectionLifecycleObserver>, Lifecycle {

	void start(Socket socket) throws IOException;
	void stopWithException(Exception cause, String msg, Logger otherLogger);

	Identity getIAmA();
	Identity getIAmConnectingTo();
}
