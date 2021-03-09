package com.concurrentperformance.throughput.ringbuffer.impl;

import com.concurrentperformance.throughput.ringbuffer.Bucket;
import com.lmax.disruptor.EventHandler;

/**
 * Follow the main calculation around the ring buffer and make sure all refs
 * are nulled to allow GC to happen.
 *
 * @author Steve Lake
 */
public class BucketCleanupEventHandler<T extends Bucket> implements EventHandler<T> {

	@Override
	public void onEvent(T bucket, long sequence, boolean endOfBatch) throws Exception {
		bucket.clean();
	}
}