package com.concurrentperformance.throughput.comms.client.impl;

import com.concurrentperformance.throughput.comms.client.ConnectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * A helper class to ensure consistent configuration and handling of socket connections
 * across the application.
 *
 * @author Steve Lake
 */
public class DefaultConnectHelper implements ConnectHelper {

	private static final Logger log = LoggerFactory.getLogger(ConnectHelper.class);

	@Override
	public Socket startSocketToServer(String serverHost, int serverPort) throws IOException {

		Socket socketToServer = null;
		// open a socket connection
		try {
			log.info("Opening new Socket to [" + serverHost + ":" + serverPort + "]");
			socketToServer = new Socket(serverHost, serverPort);
		} catch (IOException e) {
			log.warn("Can't open socket to [" + serverHost + ":" + serverPort + "] cause [" + e.getMessage() + "]" );
			throw e;
		}

		setStandardSettings(socketToServer);
		log.info("Started socket to server [" + socketToServer + "]");

		return socketToServer;
	}

	@Override
	public void setStandardSettings(Socket socket) throws SocketException {
		socket.setTcpNoDelay(true);
		socket.setKeepAlive(true);
	}
}