package io.goodforgod.slf4j.simplelogger;

import io.goodforgod.graalvm.hint.annotation.InitializationHint;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 21.05.2022
 */
@InitializationHint(value = InitializationHint.InitPhase.BUILD,
        types = {
                LoggerFactory.class,
                LoggerFactoryFriend.class,
                MarkerFactory.class,
                MDC.class,
                MDC.MDCCloseable.class,
                SimpleLoggerConfiguration.class,
                SimpleLogger.class,
                SimpleLoggerLayouts.class,
                JsonLoggerLayouts.class,
        },
        typeNames = { "io.goodforgod.slf4j.simplelogger", "org.slf4j.event", "org.slf4j.helpers", "org.slf4j.spi" })
final class GraalVMHint {

    private GraalVMHint() {}
}
