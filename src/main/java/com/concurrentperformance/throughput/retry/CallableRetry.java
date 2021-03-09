package com.concurrentperformance.throughput.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Callable that can decorate an existing Callable with the ability to retry
 * with a retry strategy. When the task throws an exception, a try is consumed.
 * If the final attempt throws an exception, the exception is propagated.
 *
 * @author Steve Lake
 */
public class CallableRetry<V> implements Callable<V> {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Callable<V> task;
	private final RetryStrategy retryStrategy;
	private final String logInfo;

	public CallableRetry(Callable<V> task, RetryStrategy retryStrategy, String logInfo) {
		this.task = checkNotNull(task , "task must not be null");
		this.retryStrategy = checkNotNull(retryStrategy , "retryStrategy must not be null");
		this.logInfo = checkNotNull(logInfo , "logInfo must not be null");
	}

	@Override
	public V call() throws Exception {
		final int retryCount = retryStrategy.getRetryCount();
		for (int attempt=0;attempt< retryCount;attempt++) {
			try {
				return task.call();
			} catch (Exception e) {
				handleException(retryCount, attempt, e);
			}
		}
		// Should not get here
		throw new RuntimeException(logInfo + " retry had failed.");
	}

	private void handleException(int retryCount, int attempt, Exception e) throws Exception {
		String msg = logInfo + " attempt [" + (attempt +1) + "/" + retryCount + "] failed. ";
		String cause = "Cause [" + e + "]. ";
		boolean lastTry = (attempt == retryCount-1);
		if (lastTry) {
			log.info(msg + cause);
			throw e;
		}
		else {
			log.info(msg + " Retry in [" + retryStrategy.getPauseMilliseconds(attempt) + "]ms. " + cause);
			try {
				Thread.sleep(retryStrategy.getPauseMilliseconds(attempt));
			} catch (InterruptedException ie) {
				// We don't much care if the retry is early
			}
		}
	}
}
