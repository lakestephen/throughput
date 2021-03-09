package com.concurrentperformance.throughput.ringbuffer.impl;

import com.concurrentperformance.throughput.ringbuffer.Bucket;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public interface MutatorHandler<T extends Bucket> {

	void handle(T bucket, long sequence);
}
