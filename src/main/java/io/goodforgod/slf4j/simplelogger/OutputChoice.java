package io.goodforgod.slf4j.simplelogger;

import java.io.PrintStream;

/**
 * Provides {@link PrintStream} where event will be written
 *
 * @author Anton Kurako (GoodforGod)
 * @since 03.04.2022
 */
interface OutputChoice {

    PrintStream getStream();
}
