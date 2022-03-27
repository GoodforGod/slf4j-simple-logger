package io.goodforgod.slf4j.simplelogger;

/**
 * Used to print part of logger layout
 *
 * @author Anton Kurako (GoodforGod)
 * @since 14.03.2022
 */
interface Layout extends Comparable<Layout> {

    /**
     * @param event to log
     */
    void print(SimpleLoggingEvent event);

    /**
     * @return order layout compared to all others layouts
     */
    int order();

    @Override
    default int compareTo(Layout o) {
        return Integer.compare(order(), o.order());
    }
}
