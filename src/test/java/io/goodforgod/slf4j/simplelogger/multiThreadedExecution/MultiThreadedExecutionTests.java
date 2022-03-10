package io.goodforgod.slf4j.simplelogger.multiThreadedExecution;

import io.goodforgod.slf4j.simplelogger.LoggerFactoryFriend;
import io.goodforgod.slf4j.simplelogger.SimpleLoggerProperties;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests that output in multi-threaded environments is not mingled.
 * See also https://jira.qos.ch/browse/SLF4J-515
 */
class MultiThreadedExecutionTests {

    static int THREAD_COUNT = 2;
    static long TEST_DURATION_IN_MILLIS = 100;

    private final Thread[] threads = new Thread[THREAD_COUNT];

    private final PrintStream oldOut = System.out;
    StateCheckingPrintStream scps = new StateCheckingPrintStream(oldOut);

    volatile boolean signal = false;

    @BeforeEach
    public void setup() {
        System.setOut(scps);
        System.setProperty(SimpleLoggerProperties.LOG_FILE, "System.out");
        LoggerFactoryFriend.reset();
    }

    @AfterEach
    public void tearDown() throws Exception {
        LoggerFactoryFriend.reset();
        System.clearProperty(SimpleLoggerProperties.LOG_FILE);
        System.setOut(oldOut);
    }

    @Test
    void testConcurrentThreads() throws Throwable {
        WithException withException = new WithException();
        Other other = new Other();
        threads[0] = new Thread(withException);
        threads[1] = new Thread(other);
        threads[0].start();
        threads[1].start();
        Thread.sleep(TEST_DURATION_IN_MILLIS);
        signal = true;
        threads[0].join();
        threads[1].join();

        if (withException.throwable != null) {
            throw withException.throwable;
        }

        if (other.throwable != null) {
            throw other.throwable;
        }
    }

    class WithException implements Runnable {

        volatile Throwable throwable;
        Logger logger = LoggerFactory.getLogger(WithException.class);

        public void run() {
            int i = 0;

            while (!signal) {
                try {
                    logger.info("Hello {}", i, new Throwable("i=" + i));
                    i++;
                } catch (Throwable t) {
                    throwable = t;
                    MultiThreadedExecutionTests.this.signal = true;
                    return;
                }
            }
        }
    }

    class Other implements Runnable {

        volatile Throwable throwable;
        Logger logger = LoggerFactory.getLogger(Other.class);

        public void run() {
            int i = 0;
            while (!signal) {
                try {
                    logger.info("Other {}", i++);
                } catch (Throwable t) {
                    throwable = t;
                    MultiThreadedExecutionTests.this.signal = true;
                    return;
                }
            }
        }
    }
}
