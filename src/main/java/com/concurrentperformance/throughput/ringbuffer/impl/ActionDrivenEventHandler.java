package com.concurrentperformance.throughput.ringbuffer.impl;

import com.concurrentperformance.throughput.ringbuffer.Bucket;
import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event handler tied into an enumeration held in the bucket. This type of lookup
 * on a fixed array using the enum ordinal is faster than a switch statement.
 *
 * @author Steve Lake
 */
public class ActionDrivenEventHandler<A extends Enum, T extends Bucket<A>> implements EventHandler<T> {

	private final Logger log;

	private final MutatorHandler<T>[] handlers;
	private final EndOfBatchMutatorHandler endOfBatchMutatorHandler;


	public ActionDrivenEventHandler(String logName, Map<A, MutatorHandler<T>> handlers) {
		this(logName, handlers, null);
	}

	public ActionDrivenEventHandler(String logName, Map<A, MutatorHandler<T>> handlers, EndOfBatchMutatorHandler endOfBatchMutatorHandler) {
		checkNotNull(logName, "logName can't be null");
		checkNotNull(handlers, "handlers can't be null");
		checkArgument(handlers.size() > 0, "handlers should not be empty");

		this.endOfBatchMutatorHandler = endOfBatchMutatorHandler;

		log = LoggerFactory.getLogger(this.getClass().getCanonicalName() + "." + logName);

		final Class<? extends Enum> enumClass = handlers.keySet().iterator().next().getClass();

		final int enumLength = enumClass.getEnumConstants().length;
		this.handlers = new MutatorHandler[enumLength];
		for (Map.Entry<A, MutatorHandler<T>> entry : handlers.entrySet()) {
			this.handlers[entry.getKey().ordinal()] = entry.getValue();
		}

		for (int ordinal=0;ordinal<this.handlers.length;ordinal++) {
			checkArgument(this.handlers[ordinal] != null, "Ordinal [" + enumClass.getEnumConstants()[ordinal] + "] does not have a handler");
		}
	}


	@Override
	public void onEvent(T bucket, long sequence, boolean endOfBatch) throws Exception {
		if (log.isTraceEnabled()) {
			log.trace(" Seq [{}], [{}], [{}]", sequence, bucket, endOfBatch?"Batch End":"Mid Batch");
		}
		handlers[bucket.getAction().ordinal()].handle(bucket, sequence);

		if (endOfBatch && endOfBatchMutatorHandler != null) {
			endOfBatchMutatorHandler.handle(sequence);
		}
	}

}
