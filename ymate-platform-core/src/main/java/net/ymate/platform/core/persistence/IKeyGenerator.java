/*
 * Copyright 2007-2021 the original author or authors.
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
package net.ymate.platform.core.persistence;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.annotation.KeyGenerator;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.PropertyMeta;
import net.ymate.platform.core.persistence.impl.DefaultKeyGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 键值生成器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2021/4/27 1:04 下午
 * @since 2.1.0
 */
@Ignored
public interface IKeyGenerator {

    String UUID = "uuid";

    /**
     * 生成主键值
     *
     * @param owner        所属持久化容器对象
     * @param propertyMeta 字段属性描述对象
     * @param entity       数据实体对象
     * @return 返回生成的键值
     */
    Object generate(IPersistence<?, ?, ?, ?> owner, PropertyMeta propertyMeta, IEntity<?> entity);

    /**
     * 键值生成器类管理器
     */
    class Manager {

        private static final Log LOG = LogFactory.getLog(IKeyGenerator.class);

        private static final Map<String, IKeyGenerator> KEY_GENERATOR_MAP = new ConcurrentHashMap<>();

        static {
            try {
                registerKeyGenerator(UUID, DefaultKeyGenerator.class);
                //
                ClassUtils.ExtensionLoader<IKeyGenerator> extensionLoader = ClassUtils.getExtensionLoader(IKeyGenerator.class, true);
                for (Class<IKeyGenerator> keyGeneratorClass : extensionLoader.getExtensionClasses()) {
                    KeyGenerator keyGeneratorAnn = keyGeneratorClass.getAnnotation(KeyGenerator.class);
                    if (keyGeneratorAnn != null && StringUtils.isNotBlank(keyGeneratorAnn.value())) {
                        registerKeyGenerator(keyGeneratorAnn.value(), keyGeneratorClass);
                    }
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }

        public static void registerKeyGenerator(String name, Class<? extends IKeyGenerator> targetClass) throws Exception {
            if (StringUtils.isNotBlank(name)) {
                String key = name.toLowerCase();
                if (!KEY_GENERATOR_MAP.containsKey(key)) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format("KeyGenerator class [%s:%s] registered.", key, targetClass.getName()));
                    }
                    KEY_GENERATOR_MAP.put(key, targetClass.newInstance());
                }
            }
        }

        public static Set<String> getKeyGeneratorNames() {
            return Collections.unmodifiableSet(KEY_GENERATOR_MAP.keySet());
        }

        public static IKeyGenerator getKeyGenerator(String name) {
            if (StringUtils.isBlank(name)) {
                return null;
            }
            return KEY_GENERATOR_MAP.get(name.toLowerCase());
        }
    }
}
