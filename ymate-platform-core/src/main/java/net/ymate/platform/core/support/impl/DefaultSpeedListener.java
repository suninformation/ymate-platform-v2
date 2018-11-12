/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.core.support.impl;

import net.ymate.platform.core.support.ISpeedListener;
import net.ymate.platform.core.support.Speedometer;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/13 1:01 AM
 * @version 1.0
 */
public class DefaultSpeedListener implements ISpeedListener {

    private static final Log _LOG = LogFactory.getLog(DefaultSpeedListener.class);

    private Speedometer __speedometer;

    public DefaultSpeedListener(Speedometer speedometer) {
        if (speedometer == null) {
            throw new NullArgumentException("speedometer");
        }
        __speedometer = speedometer;
    }

    @Override
    public void listen(long speed, long averageSpeed, long maxSpeed, long minSpeed) {
        _LOG.info("Speedometer [" + __speedometer.name() + "] {speed=" + speed + ", averageSpeed=" + averageSpeed + ", maxSpeed=" + maxSpeed + ", minSpeed=" + minSpeed + "}");
    }
}
