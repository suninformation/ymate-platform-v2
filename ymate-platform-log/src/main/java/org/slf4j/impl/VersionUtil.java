package org.slf4j.impl;

import org.slf4j.helpers.Util;

import java.lang.reflect.Method;

/**
 * Copied from org.slf4j:slf4j-log4j12-1.7.29
 *
 * @author QOS.ch
 */
public class VersionUtil {

    static final int MINIMAL_VERSION = 5;

    static public int getJavaMajorVersion() {
        String javaVersionString = Util.safeGetSystemProperty("java.version");
        return getJavaMajorVersion(javaVersionString);
    }

    static int getJavaMajorVersion(String versionString) {
        if (versionString == null) {
            return MINIMAL_VERSION;
        }
        if (versionString.startsWith("1.")) {
            return versionString.charAt(2) - '0';
        } else {
            // we running under Java 9 or later
            try {
                Method versionMethod = Runtime.class.getMethod("version");
                Object versionObj = versionMethod.invoke(null);
                Method majorMethod = versionObj.getClass().getMethod("major");
                Integer resultInteger = (Integer) majorMethod.invoke(versionObj);
                return resultInteger.intValue();
            } catch (Exception e) {
                return MINIMAL_VERSION;
            }
        }
    }
}
