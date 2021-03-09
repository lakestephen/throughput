package com.concurrentperformance.throughput.lifecycle.closedown.impl;

import com.concurrentperformance.throughput.lifecycle.Lifecycle;
import com.google.common.collect.ComparisonChain;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO SJL Comment
 *
 * @author Steve Lake
 */
class PriorityLifecycle implements Comparable<PriorityLifecycle> {
	private final Integer priority;
	private final Lifecycle stoppableComponent;
	private final String stoppableComponentConstantToString; //Using the toString as a tie breaker and as part of the definition of the Set. But the tostring may change, so keep the one that was there when we started.


	PriorityLifecycle(Integer priority, Lifecycle stoppableComponent) {
		this.priority = checkNotNull(priority, "priority must not be null");
		this.stoppableComponent = checkNotNull(stoppableComponent, "stoppableComponent must not be null");
		this.stoppableComponentConstantToString = stoppableComponent.toString();
	}

	Lifecycle getStoppableComponent() {
		return stoppableComponent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PriorityLifecycle that = (PriorityLifecycle) o;

		if (!priority.equals(that.priority)) return false;
		if (!stoppableComponentConstantToString.equals(that.stoppableComponentConstantToString)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = priority.hashCode();
		result = 31 * result + stoppableComponent.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return  "Priority[" + priority + "] for [" + stoppableComponent + "]";
	}

	@Override
	public int compareTo(PriorityLifecycle that) {
		return ComparisonChain.start()
				.compare(this.priority, that.priority)
				.compare(this.stoppableComponentConstantToString, that.stoppableComponentConstantToString)
				.result();
	}
}
