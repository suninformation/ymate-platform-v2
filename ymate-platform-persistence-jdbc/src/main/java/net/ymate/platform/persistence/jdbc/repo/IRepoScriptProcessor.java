/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.persistence.jdbc.repo.impl.DefaultRepoScriptProcessor;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储器脚本处理器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2017/12/10 下午10:20
 * @version 1.0
 */
public interface IRepoScriptProcessor {

    /**
     * @return 脚本处理器名称
     */
    String getName();

    /**
     * 初始化脚本处理器
     *
     * @param scriptStatement 脚本代码段
     * @throws Exception 可能产生的任何异常
     */
    void init(String scriptStatement) throws Exception;

    /**
     * @param name   方法名称
     * @param params 参数集合Ω
     * @return 执行处理器返回最终预执行SQL语句
     * @throws Exception 可能产生的任何异常
     */
    String process(String name, Object... params) throws Exception;

    /**
     * @return 是需支持结果数据过滤
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

        private static final Map<String, Class<? extends IRepoScriptProcessor>> __scriptProcessors = new ConcurrentHashMap<String, Class<? extends IRepoScriptProcessor>>();

        static {
            registerScriptProcessor(DefaultRepoScriptProcessor.JAVASCRIPT.toLowerCase(), DefaultRepoScriptProcessor.class);
        }

        public static void registerScriptProcessor(String name, Class<? extends IRepoScriptProcessor> targetClass) {
            String _key = StringUtils.defaultIfBlank(name, targetClass.getName()).toLowerCase();
            if (!__scriptProcessors.containsKey(_key)) {
                __scriptProcessors.put(_key, targetClass);
            }
        }

        public static IRepoScriptProcessor getScriptProcessor(String name) throws Exception {
            if (StringUtils.isNotBlank(name)) {
                if (__scriptProcessors.containsKey(name.toLowerCase())) {
                    return __scriptProcessors.get(name.toLowerCase()).newInstance();
                }
            }
            return null;
        }
    }
}
