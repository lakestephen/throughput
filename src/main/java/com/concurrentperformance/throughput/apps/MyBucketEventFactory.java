package com.concurrentperformance.throughput.apps;

import com.lmax.disruptor.EventFactory;

/**
 * TODO comments???
 *
 * @author Steve Lake
 */
public class MyBucketEventFactory implements EventFactory<MyBucket> {

    @Override
    public MyBucket newInstance() {
        return new MyBucket();
    }
}
