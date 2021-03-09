package com.concurrentperformance.throughput.thread;

import java.net.Socket;

/**
 * Helper Factory that provides a consistent naming for threads
 * based on a passed Object.
 *
 * @author Steve Lake
 */

public class ThreadNameFactory {


	public static String getThreadName(Socket socket) {
		String name;

		if (socket == null) {
			name = "?:?>?";
		}
		else {
			name = socket.getInetAddress() + ":" + socket.getPort() + ">" + socket.getLocalPort();
		}
		return name;
	}

}