package com.concurrentperformance.throughput.ringbuffer;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public interface Bucket<A extends Enum> {

	A getAction();

	void clean();
}
