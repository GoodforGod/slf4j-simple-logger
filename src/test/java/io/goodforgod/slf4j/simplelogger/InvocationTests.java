package io.goodforgod.slf4j.simplelogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.*;

/**
 * Test whether invoking the SLF4J API causes problems or not.
 * 
 * @author Ceki Gulcu
 */
class InvocationTests extends Assertions {

    PrintStream original = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream replacement = new PrintStream(bout);

    @BeforeEach
    public void setUp() {}

    @AfterEach
    public void tearDown() {
        System.setOut(original);
    }

    @Test
    void testDebugLevelIsNotPrintedCauseOff() {
        System.setOut(replacement);

        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        Logger logger = LoggerFactory.getLogger("test1");
        logger.debug("Hello world.");

        replacement.flush();

        final String res = bout.toString().strip();
        assertTrue(res.isBlank());
    }

    @Test
    void testThrowable() {
        int i1 = 1;
        int i2 = 2;
        int i3 = 3;
        Exception e = new Exception("This is a test exception.");

        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        Logger logger = LoggerFactory.getLogger("test2");

        System.setOut(replacement);

        logger.debug("Hello world 1.");
        logger.debug("Hello world {}", i1);
        logger.debug("val={} val={}", i1, i2);
        logger.debug("val={} val={} val={}", new Object[] { i1, i2, i3 });

        logger.debug("Hello world 2", e);
        logger.info("Hello world 2.");

        logger.warn("Hello world 3.");
        logger.warn("Hello world 3", e);

        logger.error("Hello world 4.");
        logger.error("Hello world {}", 3);
        logger.error("Hello world 4.", e);

        replacement.flush();

        final String res = bout.toString().strip();
        assertTrue(res.startsWith("[INFO] test2 - Hello world 2.\r\n" +
                "[WARN] test2 - Hello world 3.\r\n" +
                "[WARN] test2 - Hello world 3"));
    }

    @Test
    void testNullParameter_BUG78() {
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        Logger logger = LoggerFactory.getLogger("testNullParameter_BUG78");
        String[] parameters = null;
        String msg = "hello {}";

        System.setOut(replacement);

        logger.info(msg, (Object[]) parameters);

        replacement.flush();

        final String res = bout.toString().strip();
        assertEquals("[INFO] testNullParameter_BUG78 - hello {}", res);
    }

    @Test
    void testNull() {
        System.setOut(replacement);

        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        Logger logger = LoggerFactory.getLogger("testNull");
        logger.debug(null);
        logger.info(null);
        logger.warn(null);
        logger.error(null);

        Exception e = new Exception("This is a test exception.");
        logger.debug(null, e);
        logger.info(null, e);
        logger.warn(null, e);
        logger.error(null, e);

        replacement.flush();

        final String res = bout.toString().strip();
        assertTrue(res.startsWith("[INFO] testNull - null\r\n" +
                "[WARN] testNull - null\r\n" +
                "[ERROR] testNull - null\r\n" +
                "[INFO] testNull - null"));
    }

    @Test
    void testMarker() {
        System.setOut(replacement);

        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        Logger logger = LoggerFactory.getLogger("testMarker");
        Marker blue = MarkerFactory.getMarker("BLUE");
        logger.debug(blue, "hello");
        logger.info(blue, "hello");
        logger.warn(blue, "hello");
        logger.error(blue, "hello");

        logger.debug(blue, "hello {}", "world");
        logger.info(blue, "hello {}", "world");
        logger.warn(blue, "hello {}", "world");
        logger.error(blue, "hello {}", "world");

        logger.debug(blue, "hello {} and {} ", "world", "universe");
        logger.info(blue, "hello {} and {} ", "world", "universe");
        logger.warn(blue, "hello {} and {} ", "world", "universe");
        logger.error(blue, "hello {} and {} ", "world", "universe");

        replacement.flush();

        final String res = bout.toString().strip();
        assertEquals("[INFO] testMarker - hello\r\n" +
                "[WARN] testMarker - hello\r\n" +
                "[ERROR] testMarker - hello\r\n" +
                "[INFO] testMarker - hello world\r\n" +
                "[WARN] testMarker - hello world\r\n" +
                "[ERROR] testMarker - hello world\r\n" +
                "[INFO] testMarker - hello world and universe \r\n" +
                "[WARN] testMarker - hello world and universe \r\n" +
                "[ERROR] testMarker - hello world and universe", res);
    }

    @Test
    void testMDC() {
        MDC.put("k", "v");
        assertNull(MDC.get("k"));
        MDC.remove("k");
        assertNull(MDC.get("k"));
        MDC.clear();
    }
}
