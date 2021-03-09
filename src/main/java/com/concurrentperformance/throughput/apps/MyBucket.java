package com.concurrentperformance.throughput.apps;

import com.concurrentperformance.throughput.ringbuffer.Bucket;

/**
 * TODO comments???
 *
 * @author Steve Lake
 */
public class MyBucket implements Bucket<Action> {

    private Action action;

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public void clean() {

    }
}
