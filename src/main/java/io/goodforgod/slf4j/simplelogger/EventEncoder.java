package io.goodforgod.slf4j.simplelogger;

/**
 * Responsible for encoding event into byte array
 *
 * @author Anton Kurako (GoodforGod)
 * @since 03.04.2022
 */
interface EventEncoder {

    byte[] encode(SimpleLoggingEvent event);
}
