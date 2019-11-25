package org.apache.log4j;

import org.apache.log4j.helpers.ThreadLocalMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Copied from org.slf4j:slf4j-log4j12-1.7.29
 *
 * @author QOS.ch
 */
public class MDCFriend {

    public static void fixForJava9() {
        if (MDC.mdc.tlm == null) {
            MDC.mdc.tlm = new ThreadLocalMap();
            MDC.mdc.java1 = false;
            setRemoveMethod(MDC.mdc);
        }
    }

    private static void setRemoveMethod(MDC mdc) {
        try {
            Method removeMethod = ThreadLocal.class.getMethod("remove");
            Field removeMethodField = MDC.class.getDeclaredField("removeMethod");
            removeMethodField.setAccessible(true);
            removeMethodField.set(mdc, removeMethod);
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
    }
}
