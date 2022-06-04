/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.slf4j.impl;

import org.slf4j.spi.MDCAdapter;

/**
 * This implementation is bound to {@link Reload4jMDCAdapter}.
 * <p>
 * Copied from org.slf4j:slf4j-reload4j:1.7.36
 *
 * @author Ceki G&uuml;lc&uuml;
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
     * Currently this method always returns an instance of
     * {@link StaticMDCBinder}.
     */
    public MDCAdapter getMDCA() {
        return new Reload4jMDCAdapter();
    }

    public String getMDCAdapterClassStr() {
        return Reload4jMDCAdapter.class.getName();
    }
}
