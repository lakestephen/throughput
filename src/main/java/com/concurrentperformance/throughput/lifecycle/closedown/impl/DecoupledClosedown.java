package com.concurrentperformance.throughput.lifecycle.closedown.impl;

import com.concurrentperformance.throughput.lifecycle.Lifecycle;
import com.concurrentperformance.throughput.lifecycle.closedown.Closedown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkState;

/**
 * Default implementation of the Closedown interface that uses a seperate thread to
 * do the closedown work. There is also protection against a duplicate shutdown,
 * where a shutdown action causes a further shutdown request.
 *
 * @author Steve Lake
 */
public class DecoupledClosedown implements Closedown {

	public static final String SHUTDOWN_HOOK_NAME = "closedown - shutdown hook";
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final ConcurrentMap<Lifecycle, Integer> stoppableComponentsWithPriority = new ConcurrentHashMap<>();

	private final AtomicBoolean closedownStarted = new AtomicBoolean(false);

	DecoupledClosedown() {
		Thread closedownThread = new Thread(SHUTDOWN_HOOK_NAME) {
			public void run() {
				stopAllComponents("shutdown hook");
			}
		};
		closedownThread.setDaemon(true);

		Runtime.getRuntime().addShutdownHook(closedownThread);
	}


	@Override
	public void closedown(final String reason) {
		Thread closedownThread = new Thread( new Runnable() {
			@Override
			public void run() {
				stopAllComponents(reason);
			}
		}, "closedown");
		closedownThread.setDaemon(true);
		closedownThread.start();
	}

	private void stopAllComponents(String reason) {
		// We only want to do the close down once, so use compareAndSet to prevent duplicate invocations.
		boolean shouldDoClosedown = closedownStarted.compareAndSet(false, true);
		if (!shouldDoClosedown) {
			return;
		}

		// Take copy to prevent the list being modified by the closedown process.
		final TreeSet<PriorityLifecycle> copy = new TreeSet<>();
		for (Map.Entry<Lifecycle, Integer> lifecycleEntry : stoppableComponentsWithPriority.entrySet()) {
			copy.add(new PriorityLifecycle(lifecycleEntry.getValue(), lifecycleEntry.getKey()));
		}

		checkState(stoppableComponentsWithPriority.size() == copy.size(), " Check hashcodes");

		log.info("Closing down [" + reason+ "]. The following components will be stopped in priority order [" + copy + "]");
		for (PriorityLifecycle stoppable : copy) {
			try {
				long start = System.currentTimeMillis();
				final Lifecycle stoppableComponent = stoppable.getStoppableComponent();
				if (stoppableComponent.isRunning()) {
					log.info("**Closing down [" + stoppable + "].");
					stoppableComponent.stop();
					log.info("Closing down took [" + (System.currentTimeMillis() - start)+ "]ms,  [" + stoppable + "].");
				}
				else {
					log.info("Not closing down [" + stoppable + "] as not running.");
				}
				stoppableComponentsWithPriority.remove(stoppableComponent);
			}
			catch (Exception e) {
				log.info("Exception while closing down [" + stoppable + "]. This can be normal when components are half started", e);
			}
		}

		if (stoppableComponentsWithPriority.size() > 0) {
			log.error("Remaining closedown objects [" + stoppableComponentsWithPriority + "]");
		}

		// NOTE: as this is a daemon thread, it may not get this far.
		// Although I think there is special dispensation for shutdown hook threads.
		// We give any non-daemon threads a final grace to exit.
		boolean outstandingDaemonThreads = true;
		for (int i=0;i<10;i++) {
			log.info("[" + i + "] Closedown complete. Remaining active threads " + Thread.getAllStackTraces().keySet());
			outstandingDaemonThreads = hasOutstandingDaemonThreads();
			if (!outstandingDaemonThreads) {
				break;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error("", e);
			}
		}

		if (outstandingDaemonThreads) {
			if (inShutdownHook()) {
				log.warn("Allow shutdown hook to exit.");
			}
			else {
				log.warn("Forcing Closedown with System.exit(0)");
				System.exit(0);
			}
		}
		else {
			log.info("All daemon threads exited. Goodbye.");
		}

	}

	private boolean inShutdownHook() {
		return Thread.currentThread().getName().equals(SHUTDOWN_HOOK_NAME);

	}

	private boolean hasOutstandingDaemonThreads() {
		boolean outstandingDaemonThreads = false;
		for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
			if (!entry.getKey().isDaemon()) {
				log.info("Non-daemon thread [" + entry.getKey().getName() + "] is still active");
				log.debug("   with stack trace [" + Arrays.toString(entry.getValue())+ "]");
				outstandingDaemonThreads = true;
			}
		}
		return outstandingDaemonThreads;
	}

	@Override
	public void add(Lifecycle stoppableComponent, Integer priority) {
		log.info("Stoppable Components add [" + stoppableComponent + "]");
		stoppableComponentsWithPriority.put(stoppableComponent, priority);
		logStoppableComponents();
	}

	@Override
	public void remove(Lifecycle stoppableComponent) {
		if (stoppableComponentsWithPriority.containsKey(stoppableComponent)) {
			log.info("Stoppable Components remove [{}]", stoppableComponent);
			stoppableComponentsWithPriority.remove(stoppableComponent);
		} else {
			log.warn("Cant remove stoppable components [{}]", stoppableComponent);
		}
		logStoppableComponents();
	}

	public void setStoppableComponentsWithPriority(Map<Lifecycle, Integer> stoppableComponentsWithPriority) {
		this.stoppableComponentsWithPriority.putAll(stoppableComponentsWithPriority);
		logStoppableComponents();
	}

	private void logStoppableComponents() {
		log.debug("Stoppable Components [" + stoppableComponentsWithPriority + "]");
	}

}
