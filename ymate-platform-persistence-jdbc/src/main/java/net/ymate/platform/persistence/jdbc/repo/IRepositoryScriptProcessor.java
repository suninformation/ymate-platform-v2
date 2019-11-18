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
package net.ymate.platform.persistence.jdbc.repo;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.serialize.SerializerManager;
import net.ymate.platform.persistence.jdbc.repo.annotation.RepositoryScriptProcessor;
import net.ymate.platform.persistence.jdbc.repo.impl.DefaultRepositoryScriptProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储器脚本处理器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2017/12/10 下午10:20
 */
@Ignored
public interface IRepositoryScriptProcessor {

    /**
     * 初始化脚本处理器
     *
     * @param scriptStatement 脚本代码段
     * @throws Exception 可能产生的任何异常
     */
    void init(String scriptStatement) throws Exception;

    /**
     * 执行处理器
     *
     * @param name   方法名称
     * @param params 参数集合Ω
     * @return 返回最终预执行SQL语句
     * @throws Exception 可能产生的任何异常
     */
    String process(String name, Object... params) throws Exception;

    /**
     * 判断是否支持结果数据过滤
     *
     * @return 返回true表示支持结果数据过滤
     */
    boolean isFilterable();

    /**
     * 执行结果数据过滤
     *
     * @param results 待过滤结果对象
     * @return 返回过滤后的结果对象
     */
    Object doFilter(Object results);

    /**
     * 存储器脚本处理器类管理器
     */
    class Manager {

        private static final Log LOG = LogFactory.getLog(SerializerManager.class);

        private static final Map<String, IRepositoryScriptProcessor> SCRIPT_PROCESSORS = new ConcurrentHashMap<>();

        static {
            SCRIPT_PROCESSORS.put(DefaultRepositoryScriptProcessor.JAVASCRIPT.toLowerCase(), new DefaultRepositoryScriptProcessor());
            //
            try {
                ClassUtils.ExtensionLoader<IRepositoryScriptProcessor> extensionLoader = ClassUtils.getExtensionLoader(IRepositoryScriptProcessor.class, true);
                for (Class<IRepositoryScriptProcessor> scriptProcessorClass : extensionLoader.getExtensionClasses()) {
                    RepositoryScriptProcessor scriptProcessorAnn = scriptProcessorClass.getAnnotation(RepositoryScriptProcessor.class);
                    if (scriptProcessorAnn != null) {
                        registerScriptProcessor(scriptProcessorAnn.value(), scriptProcessorClass);
                    }
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }

        public static void registerScriptProcessor(String name, Class<? extends IRepositoryScriptProcessor> targetClass) throws Exception {
            String key = StringUtils.defaultIfBlank(name, targetClass.getName()).toLowerCase();
            ReentrantLockHelper.putIfAbsentAsync(SCRIPT_PROCESSORS, key, targetClass::newInstance);
        }

        public static IRepositoryScriptProcessor getDefaultScriptProcessor() {
            return getScriptProcessor(DefaultRepositoryScriptProcessor.JAVASCRIPT);
        }

        public static IRepositoryScriptProcessor getScriptProcessor(Class<? extends IRepositoryScriptProcessor> clazz) {
            if (clazz == null) {
                return null;
            }
            return SCRIPT_PROCESSORS.get(clazz.getName().toLowerCase());
        }

        public static IRepositoryScriptProcessor getScriptProcessor(String name) {
            if (StringUtils.isBlank(name)) {
                return null;
            }
            return SCRIPT_PROCESSORS.get(name.toLowerCase());
        }
    }
}
