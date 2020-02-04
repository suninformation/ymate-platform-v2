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
package net.ymate.platform.core.beans;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * 对象加载器接口
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-5 下午12:01
 */
public interface IBeanLoader {

    /**
     * 获取当前使用的类加载器
     *
     * @return 返回当前使用的类加载器
     */
    ClassLoader getClassLoader();

    /**
     * 指定类加载器
     *
     * @param classLoader 类加载器
     */
    void setClassLoader(ClassLoader classLoader);

    /**
     * 加载类对象
     *
     * @return 返回加载的类对象集合
     * @throws Exception 类加载过程可能产生异常
     * @since 2.1.0
     */
    List<Class<?>> load() throws Exception;

    /**
     * 加载类对象
     *
     * @param filter 类对象过滤器
     * @return 返回加载的类对象集合
     * @throws Exception 类加载过程可能产生异常
     * @since 2.1.0
     */
    List<Class<?>> load(IBeanFilter filter) throws Exception;

    /**
     * 加载的类对象集合
     *
     * @param beanFactory 对象工厂
     * @throws Exception 类加载过程可能产生异常
     */
    void load(IBeanFactory beanFactory) throws Exception;

    /**
     * 加载的类对象集合
     *
     * @param beanFactory 对象工厂
     * @param filter      类对象过滤器
     * @throws Exception 类加载过程可能产生异常
     */
    void load(IBeanFactory beanFactory, IBeanFilter filter) throws Exception;

    /**
     * 注册扫描包路径(仅在工厂对象执行初始化前有效)
     *
     * @param packageName 包名称
     */
    void registerPackageName(String packageName);

    /**
     * 批量注册扫描包路径(仅在工厂对象执行初始化前有效)
     *
     * @param packageNames 包名称集合
     */
    void registerPackageNames(Collection<String> packageNames);

    /**
     * 获取扫描包路径名称集合
     *
     * @return 返回扫描包路径名称集合
     */
    List<String> getPackageNames();

    /**
     * 注册排除的包名称
     *
     * @param packageName 包名称
     */
    void registerExcludedPackageName(String packageName);

    /**
     * 批量注册排除的包名称
     *
     * @param packageNames 包名称
     */
    void registerExcludedPackageNames(Collection<String> packageNames);

    /**
     * 获取被排除的包名称集合
     *
     * @return 返回被排除的包名称集合
     */
    List<String> getExcludedPackageNames();

    /**
     * 注册自定义注解类对象处理器
     *
     * @param annClass 注解类型
     * @param handler  对象处理器
     */
    void registerHandler(Class<? extends Annotation> annClass, IBeanHandler handler);

    /**
     * 注册自定义注解类并使用默认对象处理器
     *
     * @param annClass 注解类型
     */
    void registerHandler(Class<? extends Annotation> annClass);

    /**
     * 获取指定注解类使用的对象处理器
     *
     * @param annClass 注解类型
     * @return 返回对象处理器
     */
    IBeanHandler getBeanHandler(Class<? extends Annotation> annClass);

    /**
     * 获取被排除的jar或zip等包文件名称集合
     *
     * @return 返回当前被排除的jar或zip等包文件名称集合
     */
    List<String> getExcludedFiles();

    /**
     * 注册被排除的jar或zip等包文件名称集合
     *
     * @param excludedFiles 文件名称集合
     */
    void registerExcludedFiles(Collection<String> excludedFiles);
}
