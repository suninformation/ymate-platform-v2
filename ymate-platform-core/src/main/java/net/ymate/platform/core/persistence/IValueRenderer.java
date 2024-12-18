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

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.annotation.ValueRenderer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 属性值自定义渲染器
 *
 * @author 刘镇 (suninformation@163.com) on 2021/12/22 5:19 下午
 * @since 2.1.0
 */
@Ignored
public interface IValueRenderer {

    /**
     * @since 2.1.3
     */
    static Object processValueRenderer(ClassUtils.BeanWrapper<?> targetWrapper, Field field, Object originValue) {
        if (field != null && originValue != null) {
            ValueRenderer valueRendererAnn = field.getAnnotation(ValueRenderer.class);
            if (valueRendererAnn != null && !ArrayUtils.isEmpty(valueRendererAnn.value())) {
                for (Class<? extends IValueRenderer> valueRendererClass : valueRendererAnn.value()) {
                    if (valueRendererClass != null) {
                        IValueRenderer valueRenderer = IValueRenderer.Manager.getValueRenderer(valueRendererClass);
                        if (valueRenderer != null) {
                            originValue = valueRenderer.render(targetWrapper, field, originValue, valueRendererAnn.params());
                        }
                    }
                }
            }
        }
        return originValue;
    }

    /**
     * 执行渲染操作
     *
     * @param targetType  目标属性类型
     * @param originValue 原始属性值
     * @return 返回渲染后的属性值
     */
    default Object render(Class<?> targetType, Object originValue) {
        return BlurObject.bind(originValue).toObjectValue(targetType);
    }

    /**
     * 执行渲染操作
     *
     * @param targetWrapper 目标对象包装器
     * @param field         目标属性对象
     * @param originValue   原始属性值
     * @param params        自定义参数集合
     * @return 返回渲染后的属性值
     * @since 2.1.3
     */
    default Object render(ClassUtils.BeanWrapper<?> targetWrapper, Field field, Object originValue, String[] params) {
        return render(field.getType(), originValue);
    }

    /**
     * 渲染器类管理器
     */
    class Manager {

        private static final Log LOG = LogFactory.getLog(IValueRenderer.class);

        private static final Map<String, IValueRenderer> VALUE_RENDERER_MAP = new ConcurrentHashMap<>();

        static {
            try {
                ClassUtils.ExtensionLoader<IValueRenderer> extensionLoader = ClassUtils.getExtensionLoader(IValueRenderer.class, true);
                for (Class<IValueRenderer> valueRendererClass : extensionLoader.getExtensionClasses()) {
                    registerValueRenderer(valueRendererClass);
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }

        public static void registerValueRenderer(Class<? extends IValueRenderer> targetClass) throws Exception {
            String key = targetClass.getName();
            if (!VALUE_RENDERER_MAP.containsKey(key)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("ValueRenderer class [%s:%s] registered.", key, targetClass.getName()));
                }
                VALUE_RENDERER_MAP.put(key, targetClass.newInstance());
            }
        }

        public static IValueRenderer getValueRenderer(Class<? extends IValueRenderer> rendererClass) {
            return VALUE_RENDERER_MAP.get(rendererClass.getName());
        }
    }
}
