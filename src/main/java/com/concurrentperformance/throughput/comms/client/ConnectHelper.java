package com.concurrentperformance.throughput.comms.client;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public interface ConnectHelper {

	Socket startSocketToServer(String serverHost, int serverPort) throws IOException;

	void setStandardSettings(Socket socket) throws SocketException;
}
