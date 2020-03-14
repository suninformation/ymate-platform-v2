package org.slf4j.impl;

import org.slf4j.spi.MDCAdapter;

/**
 * Copied from org.slf4j:slf4j-log4j12-1.7.29
 *
 * @author QOS.ch
 */
public class StaticMDCBinder {

    /**
     * The unique instance of this class.
     */
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    private StaticMDCBinder() {
    }

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
     * Currently this method always returns an instance of {@link StaticMDCBinder}.
     *
     * @return instance of MDCAdapter
     */
    public MDCAdapter getMDCA() {
        return new Log4jMDCAdapter();
    }

    public String getMDCAdapterClassStr() {
        return Log4jMDCAdapter.class.getName();
    }
}
