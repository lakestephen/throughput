package com.concurrentperformance.throughput.comms.server;

import java.net.Socket;

/**
 * A handler interface that SocketConnectionListener will use to
 * pass back  newly created sockets.
 *
 * @author Steve Lake
 */
public interface SocketConnectionReceiver {

	/**
	 * Pass a new socket back.
	 * @param socketConnection
	 */
    void receiveSocketConnection(final Socket socketConnection);
}
