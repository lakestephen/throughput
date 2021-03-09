package com.concurrentperformance.throughput.ringbuffer.impl;


import com.concurrentperformance.throughput.lifecycle.Lifecycle;
import com.concurrentperformance.throughput.ringbuffer.Bucket;
import com.concurrentperformance.throughput.ringbuffer.RingBuffer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public class LmaxRingBuffer<T extends Bucket> implements RingBuffer<T>, LmaxRingBufferMBean, Lifecycle {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private String name;
	private int ringSize;
	private List<EventHandler<T>> handlers;
	private EventFactory<T> eventFactory;

	public static final int DEFAULT_CHECK_INTERVAL_SECONDS = 10;

	private ExecutorService executorService;
	private Disruptor<T> disruptor;
	private AtomicBoolean running = new AtomicBoolean(false);

	public boolean start() {
		checkNotNull(handlers, "handlers must not be null");
		checkNotNull(eventFactory, "eventFactory must not be null");
		checkNotNull(name, "name must not be null");

		executorService = buildExecutor(handlers.size());
		disruptor = buildDisruptor(executorService);
		disruptor.start();
		running.set(true);

		return true;
	}

	private ExecutorService buildExecutor(int size) {
		return Executors.newFixedThreadPool(size,
				new ThreadFactoryBuilder().
						setNameFormat(name + "_%d").build());
	}

	private Disruptor<T> buildDisruptor(Executor executorService) {
		Disruptor<T> disruptor =
				new Disruptor<>(eventFactory,
								ringSize,
								executorService,
								ProducerType.MULTI,
								new BlockingWaitStrategy());

		disruptor.handleExceptionsWith(new LoggingExceptionHandler());

		// The first handler is attached to the disruptor
		EventHandlerGroup<T> handlerGroup = disruptor.handleEventsWith(handlers.get(0));
		// All others are attached to the previous handlerGroup.
		for (int i=1;i<handlers.size();i++) {
			final EventHandler<T> handler = handlers.get(i);
			handlerGroup = handlerGroup.then(handler);
		}

		return disruptor;
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	@Override
	public boolean stop() {

		try {
			disruptor.shutdown(5, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			log.warn("[{}] Disruptor did not fully shut down. ",this.toString());
		}

		executorService.shutdown();
		try {
			final boolean terminated = executorService.awaitTermination(5, TimeUnit.SECONDS);
			if (!terminated) {
				log.warn("[{}] Disruptor executorService did not fully shut down. ",this.toString());
			}
		} catch (InterruptedException e) {
			log.error("TODO ", e);
		}

		running.set(false);
		return true;
	}

	@Override
	public void publishEvent(EventTranslator<T> eventTranslator) {
		disruptor.publishEvent(eventTranslator);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRingSize(int ringSize) {
		this.ringSize = ringSize;
	}

	public void setEventHandlers(List<EventHandler<T>> handlers) {
		this.handlers = handlers;
	}

	public void setEventFactory(EventFactory<T> eventFactory) {
		this.eventFactory = eventFactory;
	}

	@Override
	public String toString() {
		return "LmaxRingBuffer{" + name + "}";
	}

	@Override
	public String getCapacity() {
		final long remainingCapacity = disruptor.getRingBuffer().remainingCapacity();
		return remainingCapacity + "/" + ringSize;
	}
}
