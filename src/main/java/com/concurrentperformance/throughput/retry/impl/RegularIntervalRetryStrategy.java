package com.concurrentperformance.throughput.retry.impl;

import com.concurrentperformance.throughput.retry.RetryStrategy;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A configurable retry that will pause the same number of milliseconds for each attempt
 *
 * @author Steve Lake
 */
public class RegularIntervalRetryStrategy implements RetryStrategy {

	private final int retryCount ;
	private final int pauseMilliseconds;

	public RegularIntervalRetryStrategy(int retryCount, int pauseMilliseconds) {
		checkArgument(retryCount > 0, "retryCount must be positive.");
		checkArgument(retryCount > 0, "pauseMilliseconds must be positive.");

		this.retryCount = retryCount;
		this.pauseMilliseconds = pauseMilliseconds;
	}

	@Override
	public int getRetryCount() {
		return retryCount;
	}

	@Override
	public int getPauseMilliseconds(int attempt) {
		return pauseMilliseconds;
	}
}
