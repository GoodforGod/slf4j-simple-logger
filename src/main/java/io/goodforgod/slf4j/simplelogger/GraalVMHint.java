package io.goodforgod.slf4j.simplelogger;

import io.goodforgod.graalvm.hint.annotation.InitializationHint;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;
import org.slf4j.impl.StaticMDCBinder;
import org.slf4j.impl.StaticMarkerBinder;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 21.05.2022
 */
@InitializationHint(value = InitializationHint.InitPhase.BUILD,
        types = {
                LoggerFactory.class,
                StaticLoggerBinder.class,
                StaticMarkerBinder.class,
                StaticMDCBinder.class,
                SimpleLoggerConfiguration.class,
                SimpleLogger.class,
                SimpleLoggerLayouts.class,
                JsonLoggerLayouts.class,
        },
        typeNames = "io.goodforgod.slf4j.simplelogger")
final class GraalVMHint {

    private GraalVMHint() {}
}
