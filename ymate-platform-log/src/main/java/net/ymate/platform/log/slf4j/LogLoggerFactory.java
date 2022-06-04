/*
 * Copyright 2007-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.platform.log.slf4j;

import net.ymate.platform.commons.ReentrantLockHelper;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.Util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/4 上午11:28
 */
public class LogLoggerFactory implements ILoggerFactory {

    private static final String LOG4J_DELEGATION_LOOP_URL = "http://www.slf4j.org/codes.html#log4jDelegationLoop";

    // Copied from org.slf4j:slf4j-reload4j:1.7.36
    // check for delegation loops
    static {
        try {
            Class.forName("org.apache.log4j.Log4jLoggerFactory");
            String part1 = "Detected both log4j-over-slf4j.jar AND bound slf4j-reload4j.jar on the class path, preempting StackOverflowError. ";
            String part2 = "See also " + LOG4J_DELEGATION_LOOP_URL + " for more details.";

            Util.report(part1);
            Util.report(part2);
            throw new IllegalStateException(part1 + part2);
        } catch (ClassNotFoundException ignored) {
        }
    }

    private static final Map<String, Logger> LOGGER_CACHE = new ConcurrentHashMap<>();

    @Override
    public Logger getLogger(String name) {
        Logger logger;
        try {
            logger = ReentrantLockHelper.putIfAbsentAsync(LOGGER_CACHE, name, () -> new LogLogger(name));
        } catch (Exception e) {
            logger = new LogLogger(name);
        }
        return logger;
    }
}
