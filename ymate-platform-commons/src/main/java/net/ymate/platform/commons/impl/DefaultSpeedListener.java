/*
 * Copyright 2007-2019 the original author or authors.
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
package net.ymate.platform.commons.impl;

import net.ymate.platform.commons.ISpeedListener;
import net.ymate.platform.commons.Speedometer;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/13 1:01 AM
 */
public class DefaultSpeedListener implements ISpeedListener {

    private static final Log LOG = LogFactory.getLog(DefaultSpeedListener.class);

    private final Speedometer speedometer;

    public DefaultSpeedListener(Speedometer speedometer) {
        if (speedometer == null) {
            throw new NullArgumentException("speedometer");
        }
        this.speedometer = speedometer;
    }

    @Override
    public void listen(long speed, long averageSpeed, long maxSpeed, long minSpeed) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Speedometer [%s] {speed=%d, averageSpeed=%d, maxSpeed=%d, minSpeed=%d}", speedometer.name(), speed, averageSpeed, maxSpeed, minSpeed));
        }
    }
}
