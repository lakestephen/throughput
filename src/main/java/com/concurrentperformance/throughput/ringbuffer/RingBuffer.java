package com.concurrentperformance.throughput.ringbuffer;

import com.lmax.disruptor.EventTranslator;

/**
 * An abstraction of the LMAX ring buffer concept.
 *
 * @author Steve Lake
 */
public interface RingBuffer<T extends Bucket> {

	void publishEvent(EventTranslator<T> eventTranslator);

}
