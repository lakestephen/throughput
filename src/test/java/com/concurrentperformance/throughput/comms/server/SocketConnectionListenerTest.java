package com.concurrentperformance.throughput.comms.server;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * @author Steve Lake
 */
public class SocketConnectionListenerTest {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Test
	public void serverCanBeConfiguredAndStarted() throws InterruptedException, IOException {
		SocketConnectionReceiver mockReceiver = mock(SocketConnectionReceiver.class);

		SocketConnectionListener connectionListener = new SocketConnectionListener(5001, mockReceiver, "TestThread");
		assertTrue(connectionListener.isClosed());

		connectionListener.start();
		assertFalse(connectionListener.isClosed());
	}

	@Test
	public void acceptsIncomingConnection() throws IOException {
		SocketConnectionReceiver mockReceiver = mock(SocketConnectionReceiver.class);

		final int port = 5003;
		SocketConnectionListener socketConnectionListener = new SocketConnectionListener(port, mockReceiver, "TestThread");
		socketConnectionListener.start();

		//make connection with listening server
		Socket socket = new Socket("127.0.0.1", port);
		assertNotNull(socket);
		assertTrue(socket.isConnected());
		assertTrue(socket.isBound());
		assertFalse(socket.isClosed());
	}


	@Ignore//TODO this is failing on the build server
	@Test(expected = ConnectException.class)
	public void serverCanBeStopped() throws IOException, InterruptedException {
		SocketConnectionListener socketConnectionListener = null;
		final int port = 5004;
		try {
			SocketConnectionReceiver mockReceiver = mock(SocketConnectionReceiver.class);
			socketConnectionListener = new SocketConnectionListener(port, mockReceiver, "TestThread");
			socketConnectionListener.start();
			Thread.sleep(100); // Make sure thread is started
			socketConnectionListener.stop();
		}
		catch (Exception notExpectingAnExceptionHere) {
			fail();
		}

		//fail to make connection with listening server
		Socket socket = new Socket("127.0.0.1", port);
		log.info(socket.toString());
		// To prevent optimiser removing this socket.
		assertTrue(socket.isClosed());
	}

	@Test
	public void returnsFalsePortUnavailable() throws IOException {
		SocketConnectionReceiver mockReceiver = mock(SocketConnectionReceiver.class);

		final int port = 5005;
		SocketConnectionListener connectionListener = new SocketConnectionListener(port, mockReceiver, "TestThread.1");
		connectionListener.start();

		SocketConnectionListener connectionListenerOnSamePort = new SocketConnectionListener(port, mockReceiver, "TestThread.2");
		boolean success = connectionListenerOnSamePort.start();

		assertFalse(success);

	}

	@Test(expected = IllegalStateException.class)
	public void throwsExceptionWhenStartingTwice() throws IOException {
		SocketConnectionListener connectionListener = null;
		try {
			SocketConnectionReceiver mockReceiver = mock(SocketConnectionReceiver.class);
			connectionListener = new SocketConnectionListener(5006, mockReceiver, "TestThread");
			connectionListener.start();

		}
		catch (Exception notExpectingAnExceptionHere) {
			fail();
		}

		connectionListener.start();
	}

	@Test(expected=IllegalArgumentException.class)
	public void throwsExceptionWhenPortSetBelowZero() throws Exception {
		SocketConnectionReceiver mockReceiver = mock(SocketConnectionReceiver.class);

		SocketConnectionListener connectionListener = new SocketConnectionListener(-1, mockReceiver, "TestThread");
		connectionListener.start();
	}

	@Test(expected=IllegalArgumentException.class)
	public void throwsExceptionWhenPortSetAboveMax() throws Exception {
		SocketConnectionReceiver mockReceiver = mock(SocketConnectionReceiver.class);

		SocketConnectionListener connectionListener = new SocketConnectionListener(0xFFFF + 1, mockReceiver, "TestThread");
		connectionListener.start();
	}

	@Test(expected=IllegalArgumentException.class)
	public void throwsExceptionWhenPortSetBelowMin() throws Exception {
		SocketConnectionReceiver mockReceiver = mock(SocketConnectionReceiver.class);

		SocketConnectionListener connectionListener = new SocketConnectionListener(0, mockReceiver, "TestThread");
		connectionListener.start();
	}

	@Test(expected=NullPointerException.class)
	public void throwsExceptionWhenNoHandlerSet() throws Exception {

		SocketConnectionListener connectionListener = new SocketConnectionListener(5008, null, "TestThread");
		connectionListener.start();
	}

}
