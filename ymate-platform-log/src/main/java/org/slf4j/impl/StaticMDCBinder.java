package org.slf4j.impl;

import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.spi.MDCAdapter;

/**
 * Copied from org.slf4j:slf4j-log4j12-1.7.26
 *
 * @author QOS.ch
 */
public class StaticMDCBinder {

    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    private StaticMDCBinder() {
    }

    public static StaticMDCBinder getSingleton() {
        return SINGLETON;
    }

    public MDCAdapter getMDCA() {
        return new BasicMDCAdapter();
    }

    public String getMDCAdapterClassStr() {
        return BasicMDCAdapter.class.getName();
    }
}
