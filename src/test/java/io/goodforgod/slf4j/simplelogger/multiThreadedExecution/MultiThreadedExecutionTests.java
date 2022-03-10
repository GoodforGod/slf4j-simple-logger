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
        clearProperties();
        System.setOut(scps);
        System.setProperty(SimpleLoggerProperties.LOG_FILE, "System.out");
        LoggerFactoryFriend.reset();
    }

    @AfterEach
    public void tearDown() {
        LoggerFactoryFriend.reset();
        System.clearProperty(SimpleLoggerProperties.LOG_FILE);
        System.setOut(oldOut);
    }

    public static void clearProperties() {
        System.clearProperty(SimpleLoggerProperties.CACHE_OUTPUT_STREAM_STRING);
        System.clearProperty(SimpleLoggerProperties.SHOW_LOG_NAME_LENGTH);
        System.clearProperty(SimpleLoggerProperties.SHOW_THREAD_NAME);
        System.clearProperty(SimpleLoggerProperties.SHOW_DATE_TIME);
        System.clearProperty(SimpleLoggerProperties.SHOW_SHORT_LOG_NAME);
        System.clearProperty(SimpleLoggerProperties.SHOW_IMPLEMENTATION_VERSION);
        System.clearProperty(SimpleLoggerProperties.DATE_TIME_FORMAT);
        System.clearProperty(SimpleLoggerProperties.DATE_TIME_OUTPUT_TYPE);
        System.clearProperty(SimpleLoggerProperties.DEFAULT_LOG_LEVEL);
        System.clearProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NAME);
        System.clearProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NULLABLE);
        System.clearProperty(SimpleLoggerProperties.ENVIRONMENT_REMEMBER_ON_START);
        System.clearProperty(SimpleLoggerProperties.ENVIRONMENTS);
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
