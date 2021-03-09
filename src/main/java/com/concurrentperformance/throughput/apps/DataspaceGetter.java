package com.concurrentperformance.throughput.apps;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO comments???
 *
 * @author Steve Lake
 */
public interface DataspaceGetter {
    ListenableFuture<Dataspace> getDataspace(long dataspaceId, long sizeBytes) throws InterruptedException;
}
