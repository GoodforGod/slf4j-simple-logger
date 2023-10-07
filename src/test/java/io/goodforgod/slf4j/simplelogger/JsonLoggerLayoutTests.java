package io.goodforgod.slf4j.simplelogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;

class JsonLoggerLayoutTests extends Assertions {

    String A_KEY = SimpleLoggerProperties.PREFIX_LOG + "a";
    PrintStream original = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream replacement = new PrintStream(bout);

    @BeforeEach
    public void before() {
        clearProperties();
        System.setProperty(A_KEY, "info");
        System.setProperty(SimpleLoggerProperties.FORMAT, SimpleLoggerProperties.OutputFormat.JSON.name());
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
        System.setProperty(A_KEY, "${A_KEY_LOG_LEVEL_NON_EXIST:off}");
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

        final int dateTimeStart = res.indexOf("timestamp\":\"");
        final int shift = "timestamp\":\"".length();
        final int dateTimeEnd = res.indexOf('"', dateTimeStart + shift);
        final String allExceptTime = res.substring(dateTimeEnd + 1);
        final String time = res.substring(dateTimeStart + shift, dateTimeEnd);
        assertTrue(time.matches("\\d\\d\\d\\d"));
        assertEquals(
                ",\"level\":\"INFO\",\"logger\":\"io.goodforgod.slf4j.simplelogger.JsonLoggerLayoutTests\",\"message\":\"hello\"}",
                allExceptTime);
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

        final int dateTimeStart = res.indexOf("timestamp\":\"");
        final int shift = "timestamp\":\"".length();
        final int dateTimeEnd = res.indexOf('"', dateTimeStart + shift);
        final String allExceptTime = res.substring(dateTimeEnd + 1);
        final String time = res.substring(dateTimeStart + shift, dateTimeEnd);
        assertNotEquals(0L, Long.parseLong(time));
        assertEquals(
                ",\"level\":\"INFO\",\"logger\":\"io.goodforgod.slf4j.simplelogger.JsonLoggerLayoutTests\",\"message\":\"hello\"}",
                allExceptTime);
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

        final int dateTimeStart = res.indexOf("timestamp\":\"");
        final int shift = "timestamp\":\"".length();
        final int dateTimeEnd = res.indexOf('"', dateTimeStart + shift);
        final String allExceptTime = res.substring(dateTimeEnd + 1);
        final String time = res.substring(dateTimeStart + shift, dateTimeEnd);
        assertTrue(time.matches("\\d\\d:\\d\\d:\\d\\d"));
        assertEquals(
                ",\"level\":\"INFO\",\"logger\":\"io.goodforgod.slf4j.simplelogger.JsonLoggerLayoutTests\",\"message\":\"hello\"}",
                allExceptTime);
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

        final int dateTimeStart = res.indexOf("timestamp\":\"");
        final int shift = "timestamp\":\"".length();
        final int dateTimeEnd = res.indexOf('"', dateTimeStart + shift);
        final String allExceptTime = res.substring(dateTimeEnd + 1);
        final String unixTime = res.substring(dateTimeStart + shift, dateTimeEnd);
        assertNotEquals(0L, Long.parseLong(unixTime));
        assertTrue(Long.parseLong(unixTime) < 1_000_000);
        assertEquals(
                ",\"level\":\"INFO\",\"logger\":\"io.goodforgod.slf4j.simplelogger.JsonLoggerLayoutTests\",\"message\":\"hello\"}",
                allExceptTime);
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

        assertEquals("{\"level\":\"WARN\",\"logger\":\"i.g.s.s.JsonLoggerLayoutTests\",\"message\":\"hello\"}", res);
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

        assertEquals(
                "{\"level\":\"INFO\",\"thread\":\"Test worker\",\"logger\":\"io.goodforgod.slf4j.simplelogger.JsonLoggerLayoutTests\",\"message\":\"hello\"}",
                res);
    }

    @Test
    void throwableOutput() throws JSONException {
        System.setOut(replacement);
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "true");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "false");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());
        // change reference to original before logging

        simpleLogger.error("hello", new RuntimeException(new IllegalStateException("Ops")));
        replacement.flush();
        final String res = bout.toString().strip();

        final JSONObject o = (JSONObject) JSONParser.parseJSON(res);
        assertEquals("java.lang.IllegalStateException: Ops", o.getString("exception"));
        final JSONArray stacktrace = o.getJSONArray("stacktrace");
        assertEquals("java.lang.IllegalStateException: Ops", stacktrace.getJSONObject(0).getString("message"));
        assertEquals("io.goodforgod.slf4j.simplelogger.JsonLoggerLayoutTests", stacktrace.getJSONObject(0).getString("clazz"));
        assertTrue(stacktrace.getJSONObject(0).getString("method").startsWith("throwableOutput"));
    }

    @Test
    void testMDClogging() throws JSONException {
        System.setOut(replacement);
        System.setProperty(SimpleLoggerProperties.SHOW_MDC, "true");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        Logger logger = LoggerFactory.getLogger("testMarker");

        SimpleLogger.init();

        MDC.put("k", "v");
        logger.info("hello {}", "world");
        MDC.clear();

        replacement.flush();

        final String res = bout.toString().strip();
        final JSONObject json = (JSONObject) JSONParser.parseJSON(res);

        assertEquals("v", json.optJSONObject("context").optString("k"), json.toString());
    }
}
