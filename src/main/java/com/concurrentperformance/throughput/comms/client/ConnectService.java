package com.concurrentperformance.throughput.comms.client;

import com.concurrentperformance.throughput.lifecycle.Lifecycle;

/**
 * For use when you want control from the client end of starting and stopping
 * the connection to the server. If you want an automatic connection, then
 * use the ClientSocketFactory to connect and then inject the socket into the
 * connection.
 *
 * @author Steve Lake
 */
public interface ConnectService extends Lifecycle {

}
