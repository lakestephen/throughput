package com.concurrentperformance.throughput.comms.server;

import com.concurrentperformance.throughput.comms.client.ConnectHelper;
import com.concurrentperformance.throughput.comms.client.impl.DefaultConnectHelper;
import com.concurrentperformance.throughput.lifecycle.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static com.concurrentperformance.throughput.precondition.PortPrecondition.checkPort;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A socket based server that will listen on a port and then hand
 * off the new connection to the passed in SocketConnectionReceiver
 *
 * @author Steve Lake
 */
@ThreadSafe
public class SocketConnectionListener implements Lifecycle {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final int port;
    private final SocketConnectionReceiver receiver;
	private final String threadName;

	private final ConnectHelper connectHelper = new DefaultConnectHelper() ; //TODO inject
    private volatile ServerSocket listeningSocket;

	/**
	 * @param port between 1 and 0xFFFF
	 * @param handler Not null
	 * @param threadName Not null
	 */
	public SocketConnectionListener(int port, SocketConnectionReceiver handler, String threadName) {

		this.port = checkPort(port, "port");
		this.receiver = checkNotNull(handler, "Handler must not be null");
		this.threadName = checkNotNull(threadName, "threadName must not be null.");
	}

	@Override
	public boolean start() {
		checkState(!isRunning(), "can't start as already running");
		try {
			listeningSocket = new ServerSocket(port);
		    log.info("Starting listening for " + receiver);
		    new Thread(new ListenerTask(), threadName).start();
			return true;
		}
		catch (IOException e) {
			log.warn("Can't Listen on port [" + port + "]", e);
			return false;
		}
    }

	@Override
	public boolean isRunning() {
		return (listeningSocket != null);
	}

	@Override
	public boolean stop() {
		checkState(isRunning(), "can't stop as not running");
		try {
			listeningSocket.close();
			listeningSocket = null;
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

    protected boolean isClosed() { //TODO do we need this?
        boolean closed = true;
        final ServerSocket listeningSocket = this.listeningSocket;

        if (listeningSocket != null) {
            closed = listeningSocket.isClosed();
        }

        return closed;
    }

	private class ListenerTask implements Runnable {

        public void run() {
            while (isRunning()) {
                try {
                    log.debug("Listening [" + listeningSocket + "] for [" + receiver + "]");
                    Socket newConnection = listeningSocket.accept();
	                connectHelper.setStandardSettings(newConnection);

                    log.info("Handing off new connection [" + newConnection + "] to [" + receiver + "]");
                    receiver.receiveSocketConnection(newConnection);

                } catch (IOException e) {
	                if (!isClosed()) {
                        log.error("Error accepting connection", e);
	                }
                } catch (Exception e) {
	                log.error("Serious Error accepting connection (Is closing down?)", e);
                }

            }

            log.info("Listening socket closed.");
        }
    }
}