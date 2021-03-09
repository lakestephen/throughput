package com.concurrentperformance.throughput.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Last resort exception handler.
 *
 * @author Steve Lake
 */
public class ThreadUncaughtExceptionHelper {

	private static final Logger log = LoggerFactory.getLogger(LoggingUncaughtExceptionHandler.class);

	public static void setLoggingDefaultUncaughtException() {
		Thread.setDefaultUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());
	}

	private static class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

		@Override
		public void uncaughtException(Thread thread, Throwable e) {
			try {
				log.error("UNCAUGHT EXCEPTION in thread [" + thread + "]", e);
			} catch (Throwable t) {
				// Swallow!! If any exception escapes here could get infinite loop.
			}
		}
	}
}
