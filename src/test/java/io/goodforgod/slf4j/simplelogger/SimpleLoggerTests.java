/**
 * Copyright (c) 2004-2012 QOS.ch All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.goodforgod.slf4j.simplelogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.*;
import org.slf4j.event.Level;

class SimpleLoggerTests extends Assertions {

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
        System.setErr(original);
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
    void emptyLoggerName() {
        SimpleLogger simpleLogger = new SimpleLogger("a");
        assertEquals("info", simpleLogger.recursivelyComputeLevelString());
    }

    @Test
    void offLevel() {
        System.setProperty(A_KEY, "off");
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger("a");
        assertEquals("off", simpleLogger.recursivelyComputeLevelString());
        assertFalse(simpleLogger.isErrorEnabled());
    }

    @Test
    void loggerNameWithNoDots_WithLevel() {
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger("a");

        assertEquals("info", simpleLogger.recursivelyComputeLevelString());
    }

    @Test
    void loggerNameWithOneDotShouldInheritFromParent() {
        SimpleLogger simpleLogger = new SimpleLogger("a.b");
        assertEquals("info", simpleLogger.recursivelyComputeLevelString());
    }

    @Test
    void loggerNameWithNoDots_WithNoSetLevel() {
        SimpleLogger simpleLogger = new SimpleLogger("x");
        assertNull(simpleLogger.recursivelyComputeLevelString());
    }

    @Test
    void loggerNameWithOneDot_NoSetLevel() {
        SimpleLogger simpleLogger = new SimpleLogger("x.y");
        assertNull(simpleLogger.recursivelyComputeLevelString());
    }

    @Test
    void loggerFactorySetLevel() {
        final SimpleLoggerFactory factory = new SimpleLoggerFactory();

        SimpleLogger simpleLogger = (SimpleLogger) factory.getLogger("x.y");
        assertEquals(SimpleLogger.LOG_LEVEL_INFO, simpleLogger.currentLogLevel);

        for (Level lvl : Level.values()) {
            factory.setLogLevel(lvl);
            assertEquals(lvl.toInt(), simpleLogger.currentLogLevel);
        }

        factory.setLogLevel("OFF");
        assertEquals(SimpleLogger.LOG_LEVEL_OFF, simpleLogger.currentLogLevel);
    }

    @Test
    void checkUseOfLastSystemStreamReference() {
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "true");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_SHORT_LOG_NAME, "true");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "false");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        System.setErr(replacement);
        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        assertEquals("INFO [Test worker] SimpleLoggerTests - hello", res);
    }

    @Test
    void checkUseShowDateTimeFormat() {
        System.setErr(replacement);
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
        System.setErr(replacement);
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
    void checkUseShowStartTimeFormat() {
        System.setErr(replacement);
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "true");
        System.setProperty(SimpleLoggerProperties.DATE_TIME_OUTPUT_TYPE, "MILLIS_FROM_START");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "true");
        System.setProperty(SimpleLoggerProperties.DEFAULT_LOG_LEVEL, "INFO");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

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
        System.setErr(replacement);
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "true");
        System.setProperty(SimpleLoggerProperties.SHOW_LOG_NAME_LENGTH, "36");

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        assertEquals("[INFO] i.g.s.simplelogger.SimpleLoggerTests - hello", res);
    }

    @Test
    void checkUseOfCachedOutputStream() {
        System.setErr(replacement);
        System.setProperty(SimpleLoggerProperties.CACHE_OUTPUT_STREAM_STRING, "true");
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "true");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.LEVEL_IN_BRACKETS, "false");
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());
        // change reference to original before logging
        System.setErr(original);

        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        assertEquals("INFO [Test worker] io.goodforgod.slf4j.simplelogger.SimpleLoggerTests - hello", res);
    }
}
