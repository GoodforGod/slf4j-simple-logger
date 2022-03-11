package io.goodforgod.slf4j.simplelogger.multiThreadedExecution;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerAccessingThread extends Thread {

    private static int LOOP_LEN = 64;

    final CyclicBarrier barrier;
    final int count;
    final AtomicLong eventCount;
    List<Logger> loggerList;

    public LoggerAccessingThread(final CyclicBarrier barrier,
                                 List<Logger> loggerList,
                                 final int count,
                                 final AtomicLong eventCount) {
        this.barrier = barrier;
        this.loggerList = loggerList;
        this.count = count;
        this.eventCount = eventCount;
    }

    public void run() {
        try {
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String loggerNamePrefix = this.getClass().getName();
        for (int i = 0; i < LOOP_LEN; i++) {
            Logger logger = LoggerFactory.getLogger(loggerNamePrefix + "-" + count + "-" + i);
            loggerList.add(logger);
            Thread.yield();
            logger.info("in run method");
            eventCount.getAndIncrement();
        }
    }
}
