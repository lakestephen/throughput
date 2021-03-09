package com.concurrentperformance.throughput.retry;

/**
 * A plug-able strategy for defining the parameters for a retry.
 *
 * @author Steve Lake
 */
public interface RetryStrategy {

	int getRetryCount();

	int getPauseMilliseconds(int attempt);
}
