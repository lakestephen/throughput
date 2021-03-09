package com.concurrentperformance.throughput.retry.impl;

import com.concurrentperformance.throughput.retry.RetryStrategy;

import javax.annotation.concurrent.Immutable;

/**
 * A retry strategy with a linear back off curve.
 *
 * @author Steve Lake
 */
@Immutable
public class BackOffRetryStrategy implements RetryStrategy {

	@Override
	public int getRetryCount() {
		return 5;
	}

	@Override
	public int getPauseMilliseconds(int attempt) {
		return 500 + (2000 * attempt);
	}
}
