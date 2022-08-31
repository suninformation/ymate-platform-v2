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
package net.ymate.platform.commons.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2022/6/5 23:02
 * @since 2.1.2
 */
public class UUIDUtilsTest {

    private static final Log LOG = LogFactory.getLog(UUIDUtilsTest.class);

    @Test
    public void generateCharUUID() {
        String uuid = UUIDUtils.generateCharUUID(this);
        LOG.info("CharUUID: " + uuid);
        assertNotNull(uuid);
    }

    @Test
    public void generateNumberUUID() {
        String uuid = UUIDUtils.generateNumberUUID(this);
        LOG.info("NumberUUID: " + uuid);
        assertNotNull(uuid);
        assertTrue(StringUtils.isNumeric(uuid));
    }

    @Test
    public void generatePrefixHostUUID() {
        String uuid = UUIDUtils.generatePrefixHostUUID(this);
        LOG.info("PrefixHostUUID: " + uuid);
        assertNotNull(uuid);
    }

    @Test
    public void generateRandomUUID() {
        String uuid = UUIDUtils.generateRandomUUID();
        LOG.info("RandomUUID: " + uuid);
        assertNotNull(uuid);
    }

    @Test
    public void UUID() {
        String uuid = UUIDUtils.UUID();
        LOG.info("UUID: " + uuid);
        assertNotNull(uuid);
    }

    @Test
    public void randomStr() {
        String uuidStr = UUIDUtils.randomStr(10, false);
        LOG.info("Str: " + uuidStr);
        assertNotNull(uuidStr);
        assertFalse(StringUtils.isNumeric(uuidStr));
        //
        String numStr = UUIDUtils.randomStr(10, true);
        LOG.info("Str onlyNum: " + numStr);
        assertNotNull(numStr);
        assertTrue(StringUtils.isNumeric(numStr));
    }

    @Test
    public void randomLong() {
        long uuid = UUIDUtils.randomLong(10, 50);
        LOG.info("Long: " + uuid);
        assertTrue(uuid >= 10 && uuid <= 50);
    }

    @Test
    public void randomInt() {
        int uuid = UUIDUtils.randomInt(10, 50);
        LOG.info("Int: " + uuid);
        assertTrue(uuid >= 10 && uuid <= 50);
    }
}