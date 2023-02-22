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
package net.ymate.platform.webmvc.annotation;

import net.ymate.platform.webmvc.IRequestMappingParser;
import net.ymate.platform.webmvc.IRequestProcessor;
import net.ymate.platform.webmvc.IWebCacheProcessor;
import net.ymate.platform.webmvc.IWebErrorProcessor;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/09 21:09
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebConf {

    /**
     * @return 控制器请求映射路径分析器
     */
    Class<? extends IRequestMappingParser> mappingParserClass() default IRequestMappingParser.class;

    /**
     * @return 控制器请求处理器
     */
    Class<? extends IRequestProcessor> requestProcessClass() default IRequestProcessor.class;

    /**
     * @return 异常错误处理器
     */
    Class<? extends IWebErrorProcessor> errorProcessorClass() default IWebErrorProcessor.class;

    /**
     * @return 缓存处理器
     */
    Class<? extends IWebCacheProcessor> cacheProcessorClass() default IWebCacheProcessor.class;

    /**
     * @return 国际化资源文件存放路径
     */
    String resourceHome() default StringUtils.EMPTY;

    /**
     * @return 国际化资源文件名称
     */
    String resourceName() default StringUtils.EMPTY;

    /**
     * 国际化语言设置参数名称，可选参数，默认值为_lang
     *
     * @return 返回国际化语言设置参数名称
     */
    String languageParamName() default StringUtils.EMPTY;

    /**
     * @return 默认字符编码集设置
     */
    String defaultCharsetEncoding() default StringUtils.EMPTY;

    /**
     * @return 默认Content-Type设置
     */
    String defaultContentType() default StringUtils.EMPTY;

    /**
     * @return 请求忽略后缀集合
     */
    String[] requestIgnoreSuffixes() default {};

    /**
     * @return 请求方法参数名称
     */
    String requestMethodParam() default StringUtils.EMPTY;

    /**
     * @return 请求路径前缀
     */
    String requestPrefix() default StringUtils.EMPTY;

    /**
     * @return 控制器视图文件基础路径
     */
    String baseViewPath() default StringUtils.EMPTY;

    /**
     * @return Cookie键前缀
     */
    String cookiePrefix() default StringUtils.EMPTY;

    /**
     * @return Cookie作用域
     */
    String cookieDomain() default StringUtils.EMPTY;

    /**
     * @return Cookie作用路径
     */
    String cookiePath() default StringUtils.EMPTY;

    /**
     * @return Cookie密钥
     */
    String cookieAuthKey() default StringUtils.EMPTY;

    /**
     * @return Cookie密钥验证是否默认开启
     */
    boolean cookieAuthEnabled() default false;

    /**
     * @return Cookie是否默认使用HttpOnly
     */
    boolean cookieUseHttpOnly() default false;

    /**
     * @return 文件上传临时目录
     */
    String uploadTempDir() default StringUtils.EMPTY;

    /**
     * @return 上传文件数量最大值
     */
    long uploadFileCountMax() default 0;

    /**
     * @return 上传文件大小最大值（字节）
     */
    long uploadFileSizeMax() default 0;

    /**
     * @return 上传文件总量大小最大值（字节）
     */
    long uploadTotalSizeMax() default 0;

    /**
     * @return 内存缓冲区的大小（字节）
     */
    int uploadSizeThreshold() default 0;

    /**
     * @return 文件上传状态监听器
     */
    Class<? extends ProgressListener> uploadListenerClass() default ProgressListener.class;
}
