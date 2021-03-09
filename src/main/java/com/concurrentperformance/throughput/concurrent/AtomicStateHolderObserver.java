package com.concurrentperformance.throughput.concurrent;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
public interface AtomicStateHolderObserver<T extends Enum<T>> {
	void atomicStateHolder_notifyStateChange(T oldState, T newState);
}
