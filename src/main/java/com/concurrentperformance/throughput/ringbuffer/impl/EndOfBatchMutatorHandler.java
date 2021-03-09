package com.concurrentperformance.throughput.ringbuffer.impl;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public interface EndOfBatchMutatorHandler {

	void handle(long sequence);

}
