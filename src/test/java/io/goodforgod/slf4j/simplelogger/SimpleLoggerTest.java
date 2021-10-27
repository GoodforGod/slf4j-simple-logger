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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleLoggerTest extends Assertions {

    String A_KEY = SimpleLoggerProperties.LOG_KEY_PREFIX + "a";
    PrintStream original = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream replacement = new PrintStream(bout);

    @BeforeEach
    public void before() {
        System.setProperty(A_KEY, "info");
    }

    @AfterEach
    public void after() {
        System.clearProperty(A_KEY);
        System.clearProperty(SimpleLoggerProperties.CACHE_OUTPUT_STREAM_STRING_KEY);
        System.setErr(original);
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
    void checkUseOfLastSystemStreamReference() {
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        System.setErr(replacement);
        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        assertEquals("[Test worker] INFO io.goodforgod.slf4j.simplelogger.SimpleLoggerTest - hello", res);
    }

    @Test
    void checkUseOfCachedOutputStream() {
        System.setErr(replacement);
        System.setProperty(SimpleLoggerProperties.CACHE_OUTPUT_STREAM_STRING_KEY, "true");
        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());
        // change reference to original before logging
        System.setErr(original);

        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();
        assertEquals("[Test worker] INFO io.goodforgod.slf4j.simplelogger.SimpleLoggerTest - hello", res);
    }
}
