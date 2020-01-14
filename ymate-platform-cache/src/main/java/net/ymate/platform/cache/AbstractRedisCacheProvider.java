/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.cache;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.persistence.redis.Redis;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/01/15 01:12
 */
public abstract class AbstractRedisCacheProvider extends AbstractCacheProvider {

    private static final Log LOG = LogFactory.getLog(AbstractRedisCacheProvider.class);

    protected static final IRedisCreator REDIS_CREATOR;

    static {
        IRedisCreator redisCreator = null;
        try {
            redisCreator = ClassUtils.getExtensionLoader(IRedisCreator.class).getExtension();
            if (redisCreator == null) {
                redisCreator = Redis::get;
            }
        } catch (NoClassDefFoundError ignored) {
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        REDIS_CREATOR = redisCreator;
    }
}
