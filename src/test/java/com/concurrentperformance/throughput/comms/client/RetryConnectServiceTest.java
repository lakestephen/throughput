package com.concurrentperformance.throughput.comms.client;

import com.concurrentperformance.throughput.comms.client.impl.RetryConnectService;
import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycle;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Steve Lake
 */
public class RetryConnectServiceTest {

	@Test(expected = IllegalArgumentException.class)
	public void wontStartWithoutValidPort() {
		RetryConnectService service = new RetryConnectService();
		service.setConnection(mock(ConnectionLifecycle.class));
		service.setConnectHelper(mock(ConnectHelper.class));
		service.setServerPort(0);
		service.start();
	}

	@Test(expected = NullPointerException.class)
	public void wontStartWithoutValidConnection() {
		RetryConnectService service = new RetryConnectService();
		service.setConnectHelper(mock(ConnectHelper.class));
		service.setServerPort(123);
		service.start();
	}

	@Test
	public void attemptsConnectionToCorrectHostAndPort() throws IOException {
		RetryConnectService service = new RetryConnectService();
		ConnectHelper connectHelper = mock(ConnectHelper.class);
		service.setConnectHelper(connectHelper);
		ConnectionLifecycle connectionLifecycle = mock(ConnectionLifecycle.class);
		service.setConnection(connectionLifecycle);
		service.setServerHost("TEST");
		service.setServerPort(123);
		service.start();

		verify(connectHelper).startSocketToServer("TEST", 123);

	}

	@Test
	public void attemptsRetryWhenConnectFails() throws IOException {
		RetryConnectService service = new RetryConnectService();
		ConnectHelper connectHelper = mock(ConnectHelper.class);
		when(connectHelper.startSocketToServer("TEST", 123))
				.thenThrow(IOException.class)
				.thenReturn(mock(Socket.class));

		service.setConnectHelper(connectHelper);
		ConnectionLifecycle connectionLifecycle = when(mock(ConnectionLifecycle.class).isRunning()).thenReturn(true).getMock();
		service.setConnection(connectionLifecycle);
		service.setServerHost("TEST");
		service.setServerPort(123);
		boolean result = service.start();
		assertTrue(result);

		verify(connectHelper, times(2)).startSocketToServer("TEST", 123);

	}

	@Test
	public void returnsFalseWhenConnectFailsWithoutRecovery() throws IOException {
		RetryConnectService service = new RetryConnectService();
		ConnectHelper connectHelper = mock(ConnectHelper.class);
		when(connectHelper.startSocketToServer("TEST", 123))
				.thenThrow(IOException.class);
		service.setConnectHelper(connectHelper);
		ConnectionLifecycle connectionLifecycle = mock(ConnectionLifecycle.class);
		service.setConnection(connectionLifecycle);
		service.setServerHost("TEST");
		service.setServerPort(123);
		boolean result = service.start();
		assertFalse(result);
		verify(connectHelper, times(10)).startSocketToServer("TEST", 123);

	}

}