package com.concurrentperformance.throughput.ringbuffer.impl;

import com.lmax.disruptor.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public class LoggingExceptionHandler implements ExceptionHandler {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void handleEventException(Throwable ex, long sequence, Object event) {
		log.error("Exception processing Seq ["+ sequence + "], [" + event + "]", ex);
	}

	@Override
	public void handleOnStartException(Throwable ex) {
		log.error("Exception during onStart()", ex);
	}

	@Override
	public void handleOnShutdownException(Throwable ex) {
		log.error("Exception during onShutdown()", ex);
	}
}
