package com.concurrentperformance.throughput.apps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;

/**
 * TODO Comments
 *
 * @author Lake
 */
public class ServerApp {

    private static final Logger log = LoggerFactory.getLogger(ServerApp.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        log.info("Starting server");
        disableWarning();

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring/appCtx-server.xml");

        log.info("Server Started");

    }


    static void disableWarning() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);

            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Exception e) {
            // ignore
        }
    }
}
