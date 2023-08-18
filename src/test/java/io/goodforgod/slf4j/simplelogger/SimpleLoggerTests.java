package io.goodforgod.slf4j.simplelogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.*;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;

class SimpleLoggerTests extends Assertions {

    String A_KEY = SimpleLoggerProperties.PREFIX_LOG + "a";
    PrintStream original = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream replacement = new PrintStream(bout);

    @BeforeEach
    public void before() {
        clearProperties();
        System.clearProperty(A_KEY);
    }

    @AfterEach
    public void after() {
        clearProperties();
        System.clearProperty(A_KEY);
        System.setOut(original);
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
        System.clearProperty(SimpleLoggerProperties.FORMAT);
        SimpleLogger.CONFIG.refresh();
    }

    @Test
    void emptyLoggerName() {
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger("a");
        assertEquals(Level.INFO.toInt(), simpleLogger.currentLogLevel);
    }

    @Test
    void offLevel() {
        System.setProperty(A_KEY, "off");
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger("a");
        assertEquals(SimpleLogger.LOG_LEVEL_OFF, simpleLogger.currentLogLevel);
        assertFalse(simpleLogger.isErrorEnabled());
    }

    @Test
    void offLevelFromEnv() {
        System.setProperty(A_KEY, "${A_KEY_LOG_LEVEL}");
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger("a");
        assertEquals(SimpleLogger.LOG_LEVEL_OFF, simpleLogger.currentLogLevel);
        assertFalse(simpleLogger.isErrorEnabled());
    }

    @Test
    void offLevelNonExistEnv() {
        System.setProperty(A_KEY, "${A_KEY_LOG_LEVEL_NON_EXIST}");
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger("a");
        assertEquals(Level.INFO.toInt(), simpleLogger.currentLogLevel);
    }

    @Test
    void offLevelNonExistEnvDefaultValue() {
        System.setProperty(A_KEY, "${A_KEY_LOG_LEVEL_NON_EXIST:OFF}");
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger("a");
        assertEquals(SimpleLogger.LOG_LEVEL_OFF, simpleLogger.currentLogLevel);
        assertFalse(simpleLogger.isErrorEnabled());
    }

    @Test
    void loggerNameWithNoDots_WithLevel() {
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger("a");

        assertEquals(Level.INFO.toInt(), simpleLogger.currentLogLevel);
    }

    @Test
    void loggerNameWithOneDotShouldInheritFromParent() {
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger("a.b");
        assertEquals(Level.INFO.toInt(), simpleLogger.currentLogLevel);
    }

    @Test
    void loggerFactorySetLevel() {
        final SimpleLoggerFactory factory = new SimpleLoggerFactory();

        SimpleLogger.init();
        SimpleLogger simpleLogger = (SimpleLogger) factory.getLogger("x.y");
        assertEquals(Level.INFO.toInt(), simpleLogger.currentLogLevel);

        for (Level lvl : Level.values()) {
            factory.setLogLevel(lvl);
            assertEquals(lvl.toInt(), simpleLogger.currentLogLevel);
        }

        factory.setLogLevel("OFF");
        assertEquals(SimpleLogger.LOG_LEVEL_OFF, simpleLogger.currentLogLevel);
    }

    @Test
    void checkUseShowDateTimeFormat() {
        System.setOut(replacement);
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "true");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "true");
        System.setProperty(SimpleLoggerProperties.DATE_TIME_FORMAT, "uuuu");
        System.setProperty(SimpleLoggerProperties.DATE_TIME_OUTPUT_TYPE, "DATE_TIME");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        final int dateTimeAfter = res.indexOf(' ');
        final String allExceptDateTime = res.substring(dateTimeAfter + 1);
        final String dateTime = res.substring(0, dateTimeAfter);
        assertTrue(dateTime.matches("\\d\\d\\d\\d"));
        assertEquals("[INFO] io.goodforgod.slf4j.simplelogger.SimpleLoggerTests - hello", allExceptDateTime);
    }

    @Test
    void checkUseShowUnixTimeFormat() {
        System.setOut(replacement);
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "true");
        System.setProperty(SimpleLoggerProperties.DATE_TIME_OUTPUT_TYPE, "UNIX_TIME");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "true");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        final int dateTimeAfter = res.indexOf(' ');
        final String allExceptTime = res.substring(dateTimeAfter + 1);
        final String unixTime = res.substring(0, dateTimeAfter);
        assertNotEquals(0L, Long.parseLong(unixTime));
        assertEquals("[INFO] io.goodforgod.slf4j.simplelogger.SimpleLoggerTests - hello", allExceptTime);
    }

    @Test
    void checkUseShowTimeFormat() {
        System.setOut(replacement);
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "true");
        System.setProperty(SimpleLoggerProperties.DATE_TIME_OUTPUT_TYPE, "TIME");
        System.setProperty(SimpleLoggerProperties.DATE_TIME_FORMAT, "HH:mm:ss");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "true");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        final int dateTimeAfter = res.indexOf(' ');
        final String allExceptTime = res.substring(dateTimeAfter + 1);
        final String time = res.substring(0, dateTimeAfter);
        assertTrue(time.matches("\\d\\d:\\d\\d:\\d\\d"));
        assertEquals("[INFO] io.goodforgod.slf4j.simplelogger.SimpleLoggerTests - hello", allExceptTime);
    }

    @Test
    void checkUseShowStartTimeFormat() throws InterruptedException {
        System.setOut(replacement);
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "true");
        System.setProperty(SimpleLoggerProperties.DATE_TIME_OUTPUT_TYPE, "MILLIS_FROM_START");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "true");
        System.setProperty(SimpleLoggerProperties.DEFAULT_LOG_LEVEL, "INFO");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        Thread.sleep(5);

        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        final int dateTimeAfter = res.indexOf(' ');
        final String allExceptTime = res.substring(dateTimeAfter + 1);
        final String unixTime = res.substring(0, dateTimeAfter);
        assertNotEquals(0L, Long.parseLong(unixTime));
        assertTrue(Long.parseLong(unixTime) < 1_000_000);
        assertEquals("[INFO] io.goodforgod.slf4j.simplelogger.SimpleLoggerTests - hello", allExceptTime);
    }

    @Test
    void checkUseShowLogNameLength() {
        System.setOut(replacement);
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "true");
        System.setProperty(SimpleLoggerProperties.SHOW_LOG_NAME_LENGTH, "36");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        simpleLogger.warn("hello");
        replacement.flush();
        final String res = bout.toString().strip();

        assertEquals("[WARN] i.g.s.simplelogger.SimpleLoggerTests - hello", res);
    }

    @Test
    void markersShown() {
        System.setOut(replacement);
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "true");
        System.setProperty(SimpleLoggerProperties.SHOW_LOG_NAME_LENGTH, "36");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        Marker myMarker = MarkerFactory.getMarker("MY_MARKER");
        myMarker.add(MarkerFactory.getMarker("MY_INNER_MARKER"));
        simpleLogger.warn(myMarker, "hello");
        replacement.flush();
        final String res = bout.toString().strip();

        assertEquals("[WARN] [markers=MY_MARKER,MY_INNER_MARKER] i.g.s.simplelogger.SimpleLoggerTests - hello", res);
    }

    @Test
    void checkUseOfCachedOutputStream() {
        System.setOut(replacement);
        System.setProperty(SimpleLoggerProperties.CACHE_OUTPUT_STREAM_STRING, "true");
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "true");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "false");
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());
        // change reference to original before logging
        System.setOut(original);

        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();

        assertEquals("INFO [Test worker] io.goodforgod.slf4j.simplelogger.SimpleLoggerTests - hello", res);
    }

    @Test
    void throwableOutput() {
        System.setOut(replacement);
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "true");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "false");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());
        // change reference to original before logging

        simpleLogger.error("hello", new RuntimeException("Ops"));
        replacement.flush();
        final String res = bout.toString().strip();

        final String[] splitted = res.split(System.lineSeparator());
        assertEquals("ERROR [Test worker] io.goodforgod.slf4j.simplelogger.SimpleLoggerTests - hello", splitted[0]);
        assertEquals("java.lang.RuntimeException: Ops", splitted[1]);
        assertTrue(splitted[2].trim().startsWith("at io.goodforgod.slf4j.simplelogger.SimpleLoggerTests.throwableOutput"));
    }
}
