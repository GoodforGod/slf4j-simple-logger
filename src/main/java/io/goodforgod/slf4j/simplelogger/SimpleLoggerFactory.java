package io.goodforgod.slf4j.simplelogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * An implementation of {@link ILoggerFactory} which always returns {@link SimpleLogger} instances.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Anton Kurako (GoodforGod)
 * @since 09.10.2021
 */
public final class SimpleLoggerFactory implements ILoggerFactory {

    private final ConcurrentMap<String, SimpleLogger> loggerMap;

    public SimpleLoggerFactory() {
        this.loggerMap = new ConcurrentHashMap<>();
        SimpleLogger.lazyInit();
    }

    /**
     * Return an appropriate {@link SimpleLogger} instance by name.
     */
    @Override
    public Logger getLogger(String name) {
        return loggerMap.computeIfAbsent(name, k -> new SimpleLogger(name));
    }

    public void setLogLevel(String logLevel) {
        setLogLevel(logLevel, l -> true);
    }

    public void setLogLevel(String logLevel, Predicate<Logger> loggerPredicate) {
        if (logLevel != null && loggerPredicate != null) {
            for (SimpleLogger logger : loggerMap.values()) {
                if (loggerPredicate.test(logger)) {
                    logger.setCurrentLogLevel(logLevel);
                }
            }
        }
    }

    public void setLogLevel(Level logLevel) {
        setLogLevel(logLevel, l -> true);
    }

    public void setLogLevel(Level logLevel, Predicate<Logger> loggerPredicate) {
        setLogLevel(logLevel.name(), loggerPredicate);
    }

    /**
     * Refresh loggers
     */
    void refresh() {
        SimpleLogger.CONFIG.refresh();
        for (SimpleLogger logger : loggerMap.values()) {
            logger.computeCurrentLogLevel();
        }
    }

    /**
     * Clear the internal logger cache.
     * This method is intended to be called by classes (in the same package) for testing purposes. This
     * method is internal. It can be modified, renamed or removed at any time without notice.
     * You are strongly discouraged from calling this method in production code.
     */
    void reset() {
        loggerMap.clear();
    }
}
