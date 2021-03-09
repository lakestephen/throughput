package com.concurrentperformance.throughput.precondition;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public class PortPrecondition {

	public static int checkPort(int port, @Nullable String errorMessage) {
		checkArgument((port > 0 && port <= 0xFFFF), errorMessage + " [" + port + "] should be between 1 and 65535");
		return port;
	}
}
