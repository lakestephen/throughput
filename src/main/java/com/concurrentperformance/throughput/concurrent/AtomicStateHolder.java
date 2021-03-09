package com.concurrentperformance.throughput.concurrent;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provide richer wrapper around a {@link AtomicReference}. Do not use where
 * the state change is latency critical due to additional overheads of logging
 * and time calculations.
 *
 * @author Steve Lake
 */
public class AtomicStateHolder<T extends Enum<T>> {

	private final Logger log;

	private final String nameForLogging;
	private final AtomicStateHolderObserver<T> observer;
	private final AtomicReference<T> state;
	private volatile long lastStatusChangeMs = System.currentTimeMillis();

	public AtomicStateHolder(T initialState, Logger log, String nameForLogging) {
		this(initialState, log, nameForLogging, null);
	}

	public AtomicStateHolder(T initialState, Logger log, String nameForLogging, AtomicStateHolderObserver<T> observer) {
		checkNotNull(initialState, "initialState must not be null");
		this.state = new AtomicReference<>(initialState);
		this.log = checkNotNull(log, "log must not be null");
		this.nameForLogging = checkNotNull(nameForLogging, "nameForLogging must not be null");
		this.observer = observer;
		notifyObserver(null, initialState);

		log.info("[" +  nameForLogging + "] initial state set to [" + this + "].");
	}

	public T get() {
		return state.get();
	}

	public long getElapsedTimeSinceLastStatusChangeMs() {
		return System.currentTimeMillis() - lastStatusChangeMs;
	}

	public boolean compareAndSet(@Nonnull T expect, @Nonnull T newState) {
		checkNotNull(newState, "newState is not null");
		boolean casSuccess = state.compareAndSet(expect, newState);
		if(casSuccess) {
			log.info("[" +  nameForLogging + "]  [" + expect + "]>>[" + newState + "] CAS. [" + getElapsedTimeSinceLastStatusChangeMs() + "ms] since last status change.");
			lastStatusChangeMs = System.currentTimeMillis();
			notifyObserver(expect, newState);
		}
		else {
			log.info("[" +  nameForLogging + "] FAILED to change state to [" + newState + "]. Expected [" + expect + "], actual [" + get() + "] CAS");
		}
		return casSuccess;
	}

	public boolean compareAndSetOrThrow(@Nonnull T expect, @Nonnull T newValue) {
		boolean casSuccess = compareAndSet(expect, newValue);
		if(!casSuccess) {
			throw new IllegalStateException("[" +  nameForLogging + "] FAILED to change state to [" + newValue + "]. Expected [" + expect + "], actual [" + get() + "] CAS");
		}
		return casSuccess;
	}

	public void set(@Nonnull T newState) {
		checkNotNull(newState, "newState is not null");
		T oldState = state.get();
		state.set(newState);
		log.info("[" +  nameForLogging + "] state change to [" + newState + "] (from [" + oldState + "]) . [" + getElapsedTimeSinceLastStatusChangeMs() + "ms] since last status change.");
		lastStatusChangeMs = System.currentTimeMillis();
		notifyObserver(oldState, newState);
	}

	private void notifyObserver(T oldState, T newState) {
		if (observer != null) {
			observer.atomicStateHolder_notifyStateChange(oldState,  newState);
		}
	}

	@Override
	public boolean equals(Object o) {
		// Delegate to the underlying contained object. Checked for null, and set initial value in constructor.
		return state.get().equals(o);
	}

	@Override
	public int hashCode() {
		// Hash the underlying contained object
		return (state.get() == null) ? 0 : state.get().hashCode();
	}

	@Override
	public String toString() {
		return state.toString();
	}
}
