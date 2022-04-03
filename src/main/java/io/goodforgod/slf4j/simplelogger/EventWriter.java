package io.goodforgod.slf4j.simplelogger;

/**
 * Responsible for logic handling how writing event to {@link OutputChoice} happens
 *
 * @author Anton Kurako (GoodforGod)
 * @since 03.04.2022
 */
interface EventWriter {

    void write(SimpleLoggingEvent event);
}
