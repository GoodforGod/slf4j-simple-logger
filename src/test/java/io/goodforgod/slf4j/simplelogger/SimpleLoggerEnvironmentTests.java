package io.goodforgod.slf4j.simplelogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleLoggerEnvironmentTests extends Assertions {

    String A_KEY = SimpleLoggerProperties.PREFIX_LOG + "a";
    PrintStream original = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream replacement = new PrintStream(bout);

    @BeforeEach
    public void before() {
        System.setProperty(A_KEY, "info");
        clearProperties();
    }

    @AfterEach
    public void after() {
        System.clearProperty(A_KEY);
        System.setOut(original);
        clearProperties();
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
    }

    @Test
    void environmentLogging() {
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_SHORT_LOG_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENTS, "JAVA_HOME");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "false");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        System.setOut(replacement);
        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        assertTrue(res.matches("INFO \\[.*] SimpleLoggerEnvironmentTests - hello"), res);
    }

    @Test
    void environmentLoggingWithName() {
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_SHORT_LOG_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENTS, "JAVA_HOME");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "false");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        System.setOut(replacement);
        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        assertTrue(res.matches("INFO \\[JAVA_HOME=.*] SimpleLoggerEnvironmentTests - hello"), res);
    }

    @Test
    void environmentLoggingShowNullable() {
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_SHORT_LOG_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NULLABLE, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENTS, "NON_EXISTING_ENV");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "false");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        System.setOut(replacement);
        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        assertTrue(res.matches("INFO \\[NON_EXISTING_ENV=null] SimpleLoggerEnvironmentTests - hello"), res);
    }

    @Test
    void environmentLoggingRememberOnStartNullable() {
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_SHORT_LOG_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NULLABLE, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_REMEMBER_ON_START, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENTS, "NON_EXISTING_ENV");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "false");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        System.setOut(replacement);
        simpleLogger.warn("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        assertTrue(res.matches("WARN \\[NON_EXISTING_ENV=null] SimpleLoggerEnvironmentTests - hello"), res);
    }

    @Test
    void environmentLoggingDontShowNullable() {
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_SHORT_LOG_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NULLABLE, "false");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENTS, "NON_EXISTING_ENV");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "false");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        System.setOut(replacement);
        simpleLogger.error("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        assertTrue(res.matches("ERROR SimpleLoggerEnvironmentTests - hello"), res);
    }
}
