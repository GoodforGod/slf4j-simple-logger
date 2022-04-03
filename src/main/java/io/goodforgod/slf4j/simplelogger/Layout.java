package io.goodforgod.slf4j.simplelogger;

/**
 * Responsible for printing part of logger layout
 *
 * @author Anton Kurako (GoodforGod)
 * @since 14.03.2022
 */
interface Layout extends Comparable<Layout> {

    /**
     * @param event to transform into part of the logging message layout
     */
    void print(SimpleLoggingEvent event);

    /**
     * @return order layout positioned according to others layouts
     */
    int order();

    @Override
    default int compareTo(Layout o) {
        return Integer.compare(order(), o.order());
    }
}
