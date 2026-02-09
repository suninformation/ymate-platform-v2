/*
 * Copyright 2007-present the original author or authors.
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
package net.ymate.platform.test;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationInitializer;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.IBeanFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2026-02-06 01:39:36
 * @since 2.1.4
 */
public final class YMPTestUtils {

    private YMPTestUtils() {
    }

    /**
     * 应用初始化配置
     */
    public static class InitConfig {
        private String mainClassName;
        private List<Class<?>> testClasses;
        private Map<String, String> systemProperties;

        public String getMainClassName() {
            return mainClassName;
        }

        public InitConfig setMainClassName(String mainClassName) {
            this.mainClassName = mainClassName;
            return this;
        }

        public List<Class<?>> getTestClasses() {
            return testClasses;
        }

        public InitConfig setTestClasses(List<Class<?>> testClasses) {
            this.testClasses = testClasses;
            return this;
        }

        public Map<String, String> getSystemProperties() {
            return systemProperties;
        }

        public InitConfig setSystemProperties(Map<String, String> systemProperties) {
            this.systemProperties = systemProperties;
            return this;
        }
    }

    /**
     * 初始化YMP应用
     *
     * @param config 初始化配置
     * @return 应用实例
     */
    public static IApplication initializeYMP(InitConfig config) {
        try {
            // 设置系统属性
            if (config.getSystemProperties() != null) {
                config.getSystemProperties().forEach(System::setProperty);
            }
            // 设置主类名
            if (config.getMainClassName() != null) {
                System.setProperty(IApplication.SYSTEM_MAIN_CLASS, config.getMainClassName());
            }
            // 运行应用
            return YMP.run(new IApplicationInitializer() {
                @Override
                public void beforeBeanFactoryInit(IApplication application, IBeanFactory beanFactory) {
                    if (config.getTestClasses() != null) {
                        config.getTestClasses().forEach(clazz ->
                                beanFactory.registerBean(BeanMeta.create(clazz, true)));
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize YMP application", RuntimeUtils.unwrapThrow(e));
        }
    }

    /**
     * 为单个测试类初始化YMP应用
     *
     * @param testClass 测试类
     * @return 应用实例
     */
    public static IApplication initializeYMP(Class<?> testClass) {
        InitConfig config = new InitConfig()
                .setMainClassName(testClass.getName())
                .setTestClasses(Collections.singletonList(testClass));
        return initializeYMP(config);
    }

    /**
     * 为测试套件初始化YMP应用
     *
     * @param suiteClass  套件类
     * @param testClasses 测试类列表
     * @return 应用实例
     */
    public static IApplication initializeYMP(Class<?> suiteClass, Class<?>... testClasses) {
        List<Class<?>> allClasses = new ArrayList<>();
        allClasses.add(suiteClass);
        if (testClasses != null) {
            Collections.addAll(allClasses, testClasses);
        }
        InitConfig config = new InitConfig()
                .setMainClassName(suiteClass.getName())
                .setTestClasses(allClasses);
        return initializeYMP(config);
    }
}
