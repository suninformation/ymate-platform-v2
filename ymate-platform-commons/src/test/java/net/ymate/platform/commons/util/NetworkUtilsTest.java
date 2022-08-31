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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author 刘镇 (suninformation@163.com) on 2022/6/21 00:19
 * @since 2.1.2
 */
public class NetworkUtilsTest {

    private static final Log LOG = LogFactory.getLog(NetworkUtilsTest.class);

    @Test
    public void getHostName() {
        System.out.println(NetworkUtils.IP.getHostName());
    }

    @Test
    public void getHostIPAddresses() throws IOException {
        String[] hostIps = NetworkUtils.IP.getHostIPAddresses();
        LOG.info(Arrays.asList(hostIps));
        LOG.info(NetworkUtils.IP.getLocalIPAddr());
    }
}