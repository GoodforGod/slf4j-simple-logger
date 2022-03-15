package org.slf4j.impl;

import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;

/**
 * This implementation is bound to {@link NOPMDCAdapter}.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Anton Kurako (GoodforGod)
 * @since 09.10.2021
 */
public final class StaticMDCBinder {

    /**
     * The unique instance of this class.
     */
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    private StaticMDCBinder() {}

    /**
     * Return the singleton of this class.
     * 
     * @return the StaticMDCBinder singleton
     * @since 1.7.14
     */
    public static StaticMDCBinder getSingleton() {
        return SINGLETON;
    }

    /**
     * @return Currently this method always returns an instance of {@link StaticMDCBinder}.
     */
    public MDCAdapter getMDCA() {
        return new NOPMDCAdapter();
    }

    public String getMDCAdapterClassStr() {
        return NOPMDCAdapter.class.getName();
    }
}
