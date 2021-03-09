package com.concurrentperformance.throughput.apps;

import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * TODO Comments
 *
 * @author Lake
 */
public class ClientApp {

    private static final Logger log = LoggerFactory.getLogger(ClientApp.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        log.info("Starting client");

        ServerApp.disableWarning();

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring/appCtx-client.xml");

        log.info("Client Started");

        log.info("Starting Test");

        DataspaceGetter  dataspaceGetter = applicationContext.getBean(ConnectionManager.class);
        log.info("Payload Size(bytes) \tMessage Count  \tAverage time(uS)  \tTotal Time(nS) \tTotal Bytes \tThroughput (MB/s)");
        int iterations = 1;
        doParallelTest(dataspaceGetter,      1_000,1_000_000, iterations);
        doParallelTest(dataspaceGetter,     10_000,  100_000, iterations);
        doParallelTest(dataspaceGetter,    100_000,   10_000, iterations);
        doParallelTest(dataspaceGetter,  1_000_000,    1_000, iterations);
        doParallelTest(dataspaceGetter, 10_000_000,      100, iterations);

    }

    private static void doParallelTest(DataspaceGetter dataspaceGetter, long sizeBytes, long messageCount, int iterations) throws InterruptedException, ExecutionException {
        for (int iteration = 0; iteration < iterations; iteration++) {

            long startNano = System.nanoTime();
            List<ListenableFuture<Dataspace>> futures = new ArrayList<>();
            for (int i = 0; i < messageCount; i++) {
                futures.add(dataspaceGetter.getDataspace(i, sizeBytes));
            }

            for (ListenableFuture<Dataspace> future : futures) {
                future.get();
            }
            long elapsedNano = System.nanoTime() - startNano;

            long totalBytes = sizeBytes * messageCount;
            log.info("{}\t{}\t{}\t{}\t{}\t{}",
                    sizeBytes,
                    messageCount,
                    NANOSECONDS.toMicros(elapsedNano / messageCount),
                    NANOSECONDS.toNanos(elapsedNano),
                    totalBytes,
                    String.format("%.2f",((double) totalBytes / (double) elapsedNano) * 1000.0));
        }
    }

}
