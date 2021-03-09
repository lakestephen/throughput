package com.concurrentperformance.throughput.apps;

import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycle;
import com.concurrentperformance.throughput.comms.server.ServerSocketFactory;
import com.concurrentperformance.throughput.comms.server.impl.SkeletalServerSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * TODO Comments
 *
 * @author Lake
 */
public class MyServerSocketFactory extends SkeletalServerSocketFactory implements ServerSocketFactory {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    protected String getThreadName() {
        return "";
    }

    @Override
    protected ConnectionLifecycle buildConnection(Socket toRemoteClient) throws IOException {
        log.info("New connection");

        ConnectionManager connectionManager = new ConnectionManager(getIAmA());
        connectionManager.start(toRemoteClient);
        return connectionManager;
    }
}
