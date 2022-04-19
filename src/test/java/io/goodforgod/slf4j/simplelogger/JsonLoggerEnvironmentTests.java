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

class JsonLoggerEnvironmentTests extends Assertions {

    PrintStream original = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream replacement = new PrintStream(bout);

    @BeforeEach
    public void before() {
        clearProperties();
    }

    @AfterEach
    public void after() {
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
        System.clearProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NAME);
        System.clearProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NULLABLE);
        System.clearProperty(SimpleLoggerProperties.ENVIRONMENT_REMEMBER_ON_START);
        System.clearProperty(SimpleLoggerProperties.ENVIRONMENTS);
        System.clearProperty(SimpleLoggerProperties.FORMAT);
    }

    @Test
    void environmentLoggingWithName() throws JSONException {
        System.setProperty(SimpleLoggerProperties.FORMAT, SimpleLoggerProperties.OutputFormat.JSON.name());
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_SHORT_LOG_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENTS, "A_KEY_LOG_LEVEL");
        System.setProperty(SimpleLoggerProperties.FORMAT, SimpleLoggerProperties.OutputFormat.JSON.name());

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        System.setOut(replacement);
        simpleLogger.info("hello");
        replacement.flush();
        final String res = bout.toString().strip();

        final JSONObject o = (JSONObject) JSONParser.parseJSON(res);
        final JSONArray environment = o.getJSONArray("environment");
        final JSONObject env = environment.getJSONObject(0);
        assertEquals("A_KEY_LOG_LEVEL", env.getString("name"));
        assertEquals("off", env.getString("value"));
    }

    @Test
    void environmentLoggingShowNullable() throws JSONException {
        System.setProperty(SimpleLoggerProperties.FORMAT, SimpleLoggerProperties.OutputFormat.JSON.name());
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

        final JSONObject o = (JSONObject) JSONParser.parseJSON(res);
        final JSONArray environment = o.getJSONArray("environment");
        final JSONObject env = environment.getJSONObject(0);
        assertEquals("NON_EXISTING_ENV", env.getString("name"));
        assertEquals("null", env.getString("value"));
    }

    @Test
    void environmentLoggingRememberOnStartNullable() throws JSONException {
        System.setProperty(SimpleLoggerProperties.FORMAT, SimpleLoggerProperties.OutputFormat.JSON.name());
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_SHORT_LOG_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NULLABLE, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_REMEMBER_ON_START, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENTS, "NON_EXISTING_ENV");
        System.setProperty(SimpleLoggerProperties.FORMAT, SimpleLoggerProperties.OutputFormat.JSON.name());

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        System.setOut(replacement);
        simpleLogger.warn("hello");
        replacement.flush();
        final String res = bout.toString().strip();

        final JSONObject o = (JSONObject) JSONParser.parseJSON(res);
        final JSONArray environment = o.getJSONArray("environment");
        final JSONObject env = environment.getJSONObject(0);
        assertEquals("NON_EXISTING_ENV", env.getString("name"));
        assertEquals("null", env.getString("value"));
    }

    @Test
    void environmentLoggingDontShowNullable() throws JSONException {
        System.setProperty(SimpleLoggerProperties.FORMAT, SimpleLoggerProperties.OutputFormat.JSON.name());
        System.setProperty(SimpleLoggerProperties.SHOW_THREAD_NAME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_DATE_TIME, "false");
        System.setProperty(SimpleLoggerProperties.SHOW_SHORT_LOG_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NAME, "true");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENT_SHOW_NULLABLE, "false");
        System.setProperty(SimpleLoggerProperties.ENVIRONMENTS, "NON_EXISTING_ENV");
        System.setProperty(SimpleLoggerProperties.FORMAT, SimpleLoggerProperties.OutputFormat.JSON.name());

        SimpleLogger.init();
        SimpleLogger simpleLogger = new SimpleLogger(this.getClass().getName());

        System.setOut(replacement);
        simpleLogger.error("hello");
        replacement.flush();
        final String res = bout.toString().strip();

        final JSONObject o = (JSONObject) JSONParser.parseJSON(res);
        final JSONArray environment = o.getJSONArray("environment");
        assertEquals(0, environment.length());
    }
}
