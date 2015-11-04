/*
 * Copyright 2007-2016 the original author or authors.
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

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.StructuredDataMessage;
import org.slf4j.ext.EventData;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/4 上午11:33
 * @version 1.0
 */
public class EventDataConverter {

    public Message convertEvent(final String message, final Object[] objects, final Throwable throwable) {
        try {
            final EventData data = objects != null && objects[0] instanceof EventData ?
                    (EventData) objects[0] : new EventData(message);
            final StructuredDataMessage msg =
                    new StructuredDataMessage(data.getEventId(), data.getMessage(), data.getEventType());
            for (final Map.Entry<String, Object> entry : data.getEventMap().entrySet()) {
                final String key = entry.getKey();
                if (EventData.EVENT_TYPE.equals(key) || EventData.EVENT_ID.equals(key)
                        || EventData.EVENT_MESSAGE.equals(key)) {
                    continue;
                }
                msg.put(key, String.valueOf(entry.getValue()));
            }
            return msg;
        } catch (final Exception ex) {
            return new ParameterizedMessage(message, objects, throwable);
        }
    }
}
