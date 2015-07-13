/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.webmvc;

import java.util.List;
import java.util.Locale;

/**
 * WebMVC模块配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 下午1:33
 * @version 1.0
 */
public interface IWebMvcModuleCfg {

    /**
     * @return 控制器请求处理器，可选值为已知处理器名称或自定义处理器类名称，默认为default，目前支持已知处理器[default|json|xml|...]
     */
    public IRequestProcessor getRequestProcessor();

    /**
     * @return 异常错误处理器，可选参数
     */
    public IWebErrorProcessor getErrorProcessor();

    /**
     * @return 默认语言设置，可选参数，默认采用系统环境语言
     */
    public Locale getDefaultLocale();

    /**
     * @return 默认字符编码集设置，可选参数，默认值为UTF-8
     */
    public String getDefaultCharsetEncoding();

    /**
     * @return 请求忽略正则表达式，可选参数，默认值为^.+\.(jsp|jspx|png|gif|jpg|jpeg|js|css|swf|ico|htm|html|eot|woff|ttf|svg)$
     */
    public String getRequestIgnoreRegex();

    /**
     * @return 请求方法参数名称，可选参数， 默认值为_method
     */
    public String getRequestMethodParam();

    /**
     * @return 请求路径前缀，可选参数，默认值为空
     */
    public String getRequestPrefix();

    /**
     * @return 控制器视图文件基础路径（必须是以 '/' 开始和结尾，默认值为/WEB-INF/templates/）
     */
    public String getBaseViewPath();

    /**
     * @return 尽量返回控制器视图绝对路径
     */
    public String getAbstractBaseViewPath();

    /**
     * @return Cookie键前缀，可选参数，默认值为空
     */
    public String getCookiePrefix();

    /**
     * @return Cookie作用域，可选参数，默认值为空
     */
    public String getCookieDomain();

    /**
     * @return Cookie作用路径，可选参数，默认值为'/'
     */
    public String getCookiePath();

    /**
     * @return Cookie密钥，可选参数，默认值为空
     */
    public String getCookieAuthKey();

    /**
     * @return 文件上传临时目录，为空则默认使用：System.getProperty("java.io.tmpdir")
     */
    public String getUploadTempDir();

    /**
     * @return 上传文件大小最大值（字节），默认值：-1（注：10485760 = 10M）
     */
    public int getUploadFileSizeMax();

    /**
     * @return 上传文件总量大小最大值（字节）, 默认值：-1（注：10485760 = 10M）
     */
    public int getUploadTotalSizeMax();

    /**
     * @return 内存缓冲区的大小，默认值： 10240字节（=10K），即如果文件大于10K，将使用临时文件缓存上传文件
     */
    public int getUploadSizeThreshold();

    /**
     * @return 零配置模式(无需编写控制器代码, 直接匹配并执行视图)，可选参数，默认值为false
     */
    public boolean isConventionMode();

    /**
     * @return 零配置模式视图文件路径(基于base_view_path的相对路径)，可选参数，默认值为空(即不限制访问路径)，多个路径间用'|'分隔
     */
    public List<String> getConventionViewPaths();
}
