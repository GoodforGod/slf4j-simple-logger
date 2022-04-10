package io.goodforgod.slf4j.simplelogger;

import static io.goodforgod.slf4j.simplelogger.SimpleLoggerProperties.PREFIX_LOG;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * <p>
 * Simple implementation of {@link Logger} that sends all enabled log messages, for all defined
 * loggers, to the console ({@code System.out}) or other configured destination.
 * The following system properties are supported to configure the behavior of this logger:
 * </p>
 * <ul>
 * <li><code>org.slf4j.simpleLogger.logFile</code> - The output target which can be the
 * <em>path</em> to a file, or the special values "System.out" and "System.err". Default is
 * "System.out".</li>
 * <li><code>org.slf4j.simpleLogger.logFileWarn</code> - The output target which can be the
 * <em>path</em> to a file, or the special values "System.out" and "System.err" and is used for
 * logger WARN logs. Default is "System.out".</li>
 * <li><code>org.slf4j.simpleLogger.logFileError</code> - The output target which can be the
 * <em>path</em> to a file, or the special values "System.out" and "System.err" and is used for
 * logger ERROR logs. Default is "System.out".</li>
 * <li><code>org.slf4j.simpleLogger.cacheOutputStream</code> - If the output target is set to
 * "System.out" or "System.err" (see preceding entry), by default, logs will be output to the latest
 * value referenced by <code>System.out/err</code> variables. By setting this parameter to true, the
 * output stream will be cached, i.e. assigned once at initialization time and re-used independently
 * of the current value referenced by <code>System.out/err</code>.</li>
 * <li><code>org.slf4j.simpleLogger.defaultLogLevel</code> - Default log level for all instances of
 * SimpleLogger. Must be one of ("trace", "debug", "info", "warn", "error" or "off"). If not
 * specified, defaults to "info".</li>
 * <li><code>org.slf4j.simpleLogger.log.<em>a.b.c</em></code> - Logging detail level for a
 * SimpleLogger instance named "a.b.c". Right-side value must be one of "trace", "debug", "info",
 * "warn", "error" or "off". When a SimpleLogger named "a.b.c" is initialized, its level is assigned
 * from this property. If unspecified, the level of nearest parent logger will be used, and if none
 * is set, then the value specified by <code>org.slf4j.simpleLogger.defaultLogLevel</code> will be
 * used.</li>
 * <li><code>org.slf4j.simpleLogger.dateTimeOutputType</code> - Set datetime output type. Must be
 * one of ("TIME", "DATE_TIME", "UNIX_TIME", "MILLIS_FROM_START"). (default DATE_TIME)</li>
 * <li><code>org.slf4j.simpleLogger.showImplementationVersion</code> - Set to true if to show
 * application implementation version from MANIFEST.MF (default false)</li>
 * <li><code>org.slf4j.simpleLogger.environments</code> - Set environment names to show in output.
 * Envs will be printed out in order they preserve in configuration. (default null)</li>
 * <li><code>org.slf4j.simpleLogger.environmentShowNullable</code> - Set to true to show environment
 * with nullable values. (default false)</li>
 * <li><code>org.slf4j.simpleLogger.environmentShowName</code> - Set to true to show environment
 * names. (default false)</li>
 * <li><code>org.slf4j.simpleLogger.environmentRememberOnStart</code> - Set to true to caches
 * environment values on configuration initialization and then always uses them when logging.
 * (default false)</li>
 * <li><code>org.slf4j.simpleLogger.logNameLength</code> - Set maximum logger name to output and
 * abbreviate if it exceeds length. Abbreviation happened to full logger name:
 * io.goodforgod.internal.logger.example.Application -> i.g.i.logger.example.Application (default
 * null)</li>
 * <li><code>org.slf4j.simpleLogger.showDateTime</code> - Set to <code>true</code> if you want the
 * current date and time to be included in output messages. Default is <code>false</code></li>
 * <li><code>org.slf4j.simpleLogger.dateTimeFormat</code> - The date and time format to be used in
 * the output messages. The pattern describing the date and time format is defined by
 * <a href=
 * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html">
 * (default is uuuu-MM-dd'T'HH:mm:ss.SSS)
 * <code>DateTimeFormatter</code></a>. If the format is not specified or is invalid, the default
 * pattern is used uuuu-MM-dd'T'HH:mm:ss.SSS.</li>
 * <li><code>org.slf4j.simpleLogger.showThreadName</code> -Set to <code>true</code> if you want to
 * output the current thread name. Defaults to <code>true</code>.</li>
 * <li><code>org.slf4j.simpleLogger.showLogName</code> - Set to <code>true</code> if you want the
 * Logger instance name to be included in output messages. Defaults to <code>true</code>.</li>
 * <li><code>org.slf4j.simpleLogger.showShortLogName</code> - Set to <code>true</code> if you want
 * the last component of the name to be included in output messages. Defaults to
 * <code>false</code>.</li>
 * <li><code>org.slf4j.simpleLogger.levelInBrackets</code> - Should the level string be output in
 * brackets? Defaults to <code>false</code>.</li>
 * <li><code>org.slf4j.simpleLogger.warnLevelString</code> - The string value output for the warn
 * level. Defaults to <code>WARN</code>.</li>
 * </ul>
 * <p>
 * In addition to looking for system properties with the names specified above, this implementation
 * also checks for a class loader resource named <code>"simplelogger.properties"</code>, and
 * includes any matching definitions from this resource (if it exists).
 * </p>
 * <p>
 * With no configuration, the default output includes the relative time in milliseconds, thread
 * name, the level, logger name, and the message followed by the line separator for the host. In
 * log4j terms it amounts to the "%r [%t] %level %logger - %m%n" pattern.
 * </p>
 * <p>
 * Sample output follows.
 * </p>
 *
 * <pre>
 * 2022-02-23T15:43:40.331 [INFO] [main] examples.Sort - Populating an array of 2 elements in reverse order.
 * 2022-02-23T15:43:40.332 [INFO] [main] examples.SortAlgo - Entered the sort method.
 * 2022-02-23T15:43:40.333 [INFO] [main] examples.SortAlgo - Dump of integer array:
 * 2022-02-23T15:43:40.334 [INFO] [main] examples.SortAlgo - Element [0] = 0
 * 2022-02-23T15:43:40.335 [INFO] [main] examples.SortAlgo - Element [1] = 1
 * 2022-02-23T15:43:40.336 [INFO] [main] examples.Sort - The next log statement should be an error message.
 * 2022-02-23T15:43:40.337 [ERROR] [main] examples.SortAlgo - Tried to dump an uninitialized array.
 *   at org.log4j.examples.SortAlgo.dump(SortAlgo.java:58)
 *   at org.log4j.examples.Sort.main(Sort.java:64)
 * 2022-02-23T15:43:40.338 [INFO] [main]  examples.Sort - Exiting main method.
 * </pre>
 * <p>
 * This implementation is heavily inspired by
 * <a href="https://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html">SLF4J simple logger</a>.
 * </p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Scott Sanders
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @author C&eacute;drik LIME
 * @author Anton Kurako (GoodforGod)
 * @since 09.10.2021
 */
public final class SimpleLogger extends MarkerIgnoringBase {

    static final int LOG_LEVEL_OFF = Level.ERROR.toInt() + 10;

    static final SimpleLoggerConfiguration CONFIG = new SimpleLoggerConfiguration();

    private static boolean isInitialized = false;

    static void lazyInit() {
        if (isInitialized) {
            return;
        }

        isInitialized = true;
        init();
    }

    /**
     * external software might be invoking this method directly. Do not rename or change its semantics.
     */
    static void init() {
        CONFIG.init();
    }

    /**
     * The current log level
     */
    int currentLogLevel;
    final int originalLogLevel;

    /**
     * The short name of this simple log instance
     */
    final String logNameShort;
    final String logName;

    /**
     * Package access allows only {@link SimpleLoggerFactory} to instantiate SimpleLogger instances.
     */
    SimpleLogger(String name) {
        this.name = name;
        this.logNameShort = name.substring(name.lastIndexOf('.') + 1);
        this.logName = CONFIG.computeLogName(name);

        final String levelString = recursivelyComputeLevelString();
        this.currentLogLevel = (levelString != null)
                ? SimpleLoggerConfiguration.tryStringToLevel(levelString).orElse(Level.INFO.toInt())
                : CONFIG.getDefaultLogLevel();
        this.originalLogLevel = this.currentLogLevel;
    }

    void setCurrentLogLevel(String logLevel) {
        this.currentLogLevel = SimpleLoggerConfiguration.tryStringToLevel(logLevel)
                .orElse(this.currentLogLevel);
    }

    String recursivelyComputeLevelString() {
        String tempName = name;
        String levelString = null;
        int indexOfLastDot = tempName.length();
        while ((levelString == null) && (indexOfLastDot > -1)) {
            tempName = tempName.substring(0, indexOfLastDot);
            levelString = CONFIG.getStringProperty(PREFIX_LOG + tempName);
            indexOfLastDot = tempName.lastIndexOf('.');
        }
        return levelString;
    }

    /**
     * This is our internal implementation for logging regular (non-parameterized) log messages.
     *
     * @param level     One of the LOG_LEVEL_XXX constants defining the log level
     * @param message   The message itself
     * @param throwable The exception whose stack trace should be logged
     */
    private void log(Level level, String message, Throwable throwable) {
        final SimpleLoggingEvent event = (CONFIG.isShowShortLogName())
                ? new SimpleLoggingEvent(logNameShort, level, message, throwable)
                : new SimpleLoggingEvent(logName, level, message, throwable);

        final List<Layout> layouts = CONFIG.getLayouts();
        for (Layout layout : layouts) {
            layout.print(event);
        }

        final EventWriter eventWriter = CONFIG.getEventWriter(event.level());
        eventWriter.write(event);
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     *
     * @param level  to log
     * @param format to parse message
     * @param arg1   to format
     */
    private void formatAndLog(Level level, String format, Object arg1) {
        if (!isLevelEnabled(level)) {
            return;
        }

        final FormattingTuple tp = MessageFormatter.format(format, arg1);
        log(level, tp.getMessage(), null);
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     *
     * @param level  to log
     * @param format to parse message
     * @param arg1   to format
     * @param arg2   to format
     */
    private void formatAndLog(Level level, String format, Object arg1, Object arg2) {
        if (!isLevelEnabled(level)) {
            return;
        }

        final FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
        log(level, tp.getMessage(), null);
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     *
     * @param level     to log
     * @param format    to parse message
     * @param arguments a list of 3 ore more arguments
     */
    private void formatAndLog(Level level, String format, Object... arguments) {
        if (!isLevelEnabled(level)) {
            return;
        }

        FormattingTuple tp = MessageFormatter.formatArray(format, arguments);
        log(level, tp.getMessage(), tp.getThrowable());
    }

    /**
     * Is the given log level currently enabled?
     * log level are numerically ordered so can use simple numeric comparison
     *
     * @param logLevel is this level enabled?
     * @return true if enabled
     */
    private boolean isLevelEnabled(Level logLevel) {
        return (logLevel.toInt() >= currentLogLevel);
    }

    /**
     * Are {@code trace} messages currently enabled?
     */
    public boolean isTraceEnabled() {
        return isLevelEnabled(Level.TRACE);
    }

    /**
     * A simple implementation which logs messages of level TRACE according to the format outlined
     * above.
     */
    public void trace(String msg) {
        if (!isLevelEnabled(Level.TRACE)) {
            return;
        }

        log(Level.TRACE, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level TRACE according to the
     * format outlined above.
     */
    public void trace(String format, Object param1) {
        formatAndLog(Level.TRACE, format, param1);
    }

    /**
     * Perform double parameter substitution before logging the message of level TRACE according to the
     * format outlined above.
     */
    public void trace(String format, Object param1, Object param2) {
        formatAndLog(Level.TRACE, format, param1, param2);
    }

    /**
     * Perform double parameter substitution before logging the message of level TRACE according to the
     * format outlined above.
     */
    public void trace(String format, Object... argArray) {
        formatAndLog(Level.TRACE, format, argArray);
    }

    /**
     * Log a message of level TRACE, including an exception.
     */
    public void trace(String msg, Throwable throwable) {
        if (!isLevelEnabled(Level.TRACE)) {
            return;
        }

        log(Level.TRACE, msg, throwable);
    }

    /**
     * Are {@code debug} messages currently enabled?
     */
    public boolean isDebugEnabled() {
        return isLevelEnabled(Level.DEBUG);
    }

    /**
     * A simple implementation which logs messages of level DEBUG according to the format outlined
     * above.
     */
    public void debug(String msg) {
        if (!isLevelEnabled(Level.DEBUG)) {
            return;
        }

        log(Level.DEBUG, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level DEBUG according to the
     * format outlined above.
     */
    public void debug(String format, Object param1) {
        formatAndLog(Level.DEBUG, format, param1);
    }

    /**
     * Perform double parameter substitution before logging the message of level DEBUG according to the
     * format outlined above.
     */
    public void debug(String format, Object param1, Object param2) {
        formatAndLog(Level.DEBUG, format, param1, param2);
    }

    /**
     * Perform double parameter substitution before logging the message of level DEBUG according to the
     * format outlined above.
     */
    public void debug(String format, Object... argArray) {
        formatAndLog(Level.DEBUG, format, argArray);
    }

    /**
     * Log a message of level DEBUG, including an exception.
     */
    public void debug(String msg, Throwable throwable) {
        if (!isLevelEnabled(Level.DEBUG)) {
            return;
        }

        log(Level.DEBUG, msg, throwable);
    }

    /**
     * Are {@code info} messages currently enabled?
     */
    public boolean isInfoEnabled() {
        return isLevelEnabled(Level.INFO);
    }

    /**
     * A simple implementation which logs messages of level INFO according to the format outlined above.
     */
    public void info(String msg) {
        if (!isLevelEnabled(Level.INFO)) {
            return;
        }

        log(Level.INFO, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level INFO according to the
     * format outlined above.
     */
    public void info(String format, Object arg) {
        formatAndLog(Level.INFO, format, arg);
    }

    /**
     * Perform double parameter substitution before logging the message of level INFO according to the
     * format outlined above.
     */
    public void info(String format, Object arg1, Object arg2) {
        formatAndLog(Level.INFO, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level INFO according to the
     * format outlined above.
     */
    public void info(String format, Object... argArray) {
        formatAndLog(Level.INFO, format, argArray);
    }

    /**
     * Log a message of level INFO, including an exception.
     */
    public void info(String msg, Throwable throwable) {
        if (!isLevelEnabled(Level.INFO)) {
            return;
        }

        log(Level.INFO, msg, throwable);
    }

    /**
     * Are {@code warn} messages currently enabled?
     */
    public boolean isWarnEnabled() {
        return isLevelEnabled(Level.WARN);
    }

    /**
     * A simple implementation which always logs messages of level WARN according to the format outlined
     * above.
     */
    public void warn(String msg) {
        if (!isLevelEnabled(Level.WARN)) {
            return;
        }

        log(Level.WARN, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level WARN according to the
     * format outlined above.
     */
    public void warn(String format, Object arg) {
        formatAndLog(Level.WARN, format, arg);
    }

    /**
     * Perform double parameter substitution before logging the message of level WARN according to the
     * format outlined above.
     */
    public void warn(String format, Object arg1, Object arg2) {
        formatAndLog(Level.WARN, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level WARN according to the
     * format outlined above.
     */
    public void warn(String format, Object... argArray) {
        formatAndLog(Level.WARN, format, argArray);
    }

    /**
     * Log a message of level WARN, including an exception.
     */
    public void warn(String msg, Throwable throwable) {
        if (!isLevelEnabled(Level.WARN)) {
            return;
        }

        log(Level.WARN, msg, throwable);
    }

    /**
     * Are {@code error} messages currently enabled?
     */
    public boolean isErrorEnabled() {
        return isLevelEnabled(Level.ERROR);
    }

    /**
     * A simple implementation which always logs messages of level ERROR according to the format
     * outlined above.
     */
    public void error(String msg) {
        if (!isLevelEnabled(Level.ERROR)) {
            return;
        }

        log(Level.ERROR, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level ERROR according to the
     * format outlined above.
     */
    public void error(String format, Object arg) {
        formatAndLog(Level.ERROR, format, arg);
    }

    /**
     * Perform double parameter substitution before logging the message of level ERROR according to the
     * format outlined above.
     */
    public void error(String format, Object arg1, Object arg2) {
        formatAndLog(Level.ERROR, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level ERROR according to the
     * format outlined above.
     */
    public void error(String format, Object... argArray) {
        formatAndLog(Level.ERROR, format, argArray);
    }

    /**
     * Log a message of level ERROR, including an exception.
     */
    public void error(String msg, Throwable throwable) {
        if (!isLevelEnabled(Level.ERROR)) {
            return;
        }

        log(Level.ERROR, msg, throwable);
    }

    public void log(LoggingEvent event) {
        final Level levelInt = event.getLevel();
        if (!isLevelEnabled(levelInt)) {
            return;
        }

        FormattingTuple tp = MessageFormatter.format(event.getMessage(), event.getArgumentArray());
        log(levelInt, tp.getMessage(), event.getThrowable());
    }
}
