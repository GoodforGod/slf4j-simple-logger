package io.goodforgod.slf4j.simplelogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests that detecting logger name mismatches works and doesn't cause problems or trigger if
 * disabled.
 * <p>
 * This test can't live inside slf4j-api because the NOP Logger doesn't remember its name.
 *
 * @author Alexander Dorokhine
 * @author Ceki G&uuml;lc&uuml;
 */
class DetectLoggerNameMismatchTests extends Assertions {

    private static final String MISMATCH_STRING = "Detected logger name mismatch";

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final PrintStream oldErr = System.err;

    @BeforeEach
    public void setUp() {
        System.setErr(new PrintStream(byteArrayOutputStream));
    }

    @AfterEach
    public void tearDown() {
        setTrialEnabled(false);
        System.setErr(oldErr);
    }

    /*
     * Pass in the wrong class to the Logger with the check disabled, and make sure there are no errors.
     */
    @Test
    void testNoTriggerWithoutProperty() {
        setTrialEnabled(false);
        Logger logger = LoggerFactory.getLogger(String.class);
        assertEquals("java.lang.String", logger.getName());
        assertMismatchDetected(false);
    }

    /*
     * Pass in the wrong class to the Logger with the check enabled, and make sure there ARE errors.
     */
    @Test
    void testTriggerWithProperty() {
        setTrialEnabled(true);
        LoggerFactory.getLogger(String.class);
        String s = String.valueOf(byteArrayOutputStream);
        assertMismatchDetected(true);
    }

    /*
     * Checks the whole error message to ensure all the names show up correctly.
     */
    @Test
    void testTriggerWholeMessage() {
        setTrialEnabled(true);
        LoggerFactory.getLogger(String.class);
        final String res = byteArrayOutputStream.toString().strip();
        final String[] lines = res.split("\n");
        assertEquals(
                "SLF4J: Detected logger name mismatch. Given name: \"java.lang.String\"; computed name: \"io.goodforgod.slf4j.simplelogger.DetectLoggerNameMismatchTests\".",
                lines[0].strip());
        assertEquals("SLF4J: See https://www.slf4j.org/codes.html#loggerNameMismatch for an explanation", lines[1].strip());
    }

    /*
     * Checks that there are no errors with the check enabled if the class matches.
     */
    @Test
    void testPassIfMatch() {
        setTrialEnabled(true);
        Logger logger = LoggerFactory.getLogger(DetectLoggerNameMismatchTests.class);
        assertEquals(DetectLoggerNameMismatchTests.class.getName(), logger.getName());
        assertMismatchDetected(false);
    }

    private void assertMismatchDetected(boolean mismatchDetected) {
        assertEquals(mismatchDetected, String.valueOf(byteArrayOutputStream).contains(MISMATCH_STRING));
    }

    @Test
    void verifyLoggerDefinedInBaseWithOverridenGetClassMethod() {
        setTrialEnabled(true);
        Square square = new Square();
        assertEquals(Square.class.getName(), square.logger.getName());
        assertMismatchDetected(false);
    }

    private static void setTrialEnabled(boolean enabled) {
        // The system property is read into a static variable at initialization time
        // so we cannot just reset the system property to test this feature.
        // Therefore we set the variable directly.
        final Field field = Arrays.stream(LoggerFactory.class.getDeclaredFields())
                .filter(f -> f.getName().equals("DETECT_LOGGER_NAME_MISMATCH"))
                .findFirst()
                .orElseThrow();

        try {
            field.setAccessible(true);
            field.set(LoggerFactory.class, enabled);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}

// Used for testing that inheritance is ignored by the checker.
class ShapeBase {

    public Logger logger = LoggerFactory.getLogger(getClass());
}

class Square extends ShapeBase {}
