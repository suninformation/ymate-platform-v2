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
package net.ymate.platform.webmvc.view;

import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.view.impl.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/5/28 下午5:44
 */
public class View {

    private static final Map<String, IViewBuilder> VIEW_BUILDERS = new LinkedHashMap<>();

    static {
        VIEW_BUILDERS.put(HtmlView.FILE_SUFFIX, HtmlView::bind);
        VIEW_BUILDERS.put(JspView.FILE_SUFFIX, JspView::bind);
        VIEW_BUILDERS.put(FreemarkerView.FILE_SUFFIX, FreemarkerView::bind);
    }

    /**
     * 注册视图文件处理器
     *
     * @param fileSuffix  文件扩展名
     * @param viewBuilder 文件视图对象构建器
     * @since 2.1.0
     */
    public static void registerViewBuilder(String fileSuffix, IViewBuilder viewBuilder) {
        if (StringUtils.isNotBlank(fileSuffix) && viewBuilder != null) {
            if (!StringUtils.startsWith(fileSuffix, ".")) {
                fileSuffix = "." + fileSuffix;
            }
            if (!VIEW_BUILDERS.containsKey(fileSuffix)) {
                VIEW_BUILDERS.put(fileSuffix, viewBuilder);
            }
        }
    }

    public static BinaryView binaryView(File targetFile) throws Exception {
        return BinaryView.bind(targetFile);
    }

    public static ForwardView forwardView(String path) {
        return ForwardView.bind(path);
    }

    public static FreemarkerView freemarkerView(IWebMvc owner, String path) {
        return FreemarkerView.bind(owner, path);
    }

    public static FreemarkerView freemarkerView(String path) {
        return FreemarkerView.bind(path);
    }

    public static FreemarkerView freemarkerView() {
        return FreemarkerView.bind();
    }

    public static VelocityView velocityView(IWebMvc owner, String path) {
        return VelocityView.bind(owner, path);
    }

    public static VelocityView velocityView(String path) {
        return VelocityView.bind(path);
    }

    public static VelocityView velocityView() {
        return VelocityView.bind();
    }

    public static HtmlView htmlView(IWebMvc owner, String htmlFile) throws Exception {
        return HtmlView.bind(owner, htmlFile);
    }

    public static HtmlView htmlView(String htmlFile) throws Exception {
        return HtmlView.bind(htmlFile);
    }

    public static HtmlView htmlView(File htmlFile) throws Exception {
        return HtmlView.bind(htmlFile);
    }

    public static HttpStatusView httpStatusView(int status) {
        return HttpStatusView.bind(status);
    }

    public static HttpStatusView httpStatusView(int status, String msg) {
        return HttpStatusView.bind(status, msg);
    }

    public static JsonView jsonView(Object obj) {
        return JsonView.bind(obj);
    }

    public static JspView jspView() {
        return JspView.bind();
    }

    public static JspView jspView(IWebMvc owner) {
        return JspView.bind(owner);
    }

    public static JspView jspView(String path) {
        return JspView.bind(path);
    }

    public static JspView jspView(IWebMvc owner, String path) {
        return JspView.bind(owner, path);
    }

    public static NullView nullView() {
        return NullView.bind();
    }

    public static RedirectView redirectView(String path) {
        return RedirectView.bind(path);
    }

    public static TextView textView(String content) {
        return TextView.bind(content);
    }

    public static PairObject<IView, String> mappingToView(IWebMvc owner, String requestMapping) throws Exception {
        IView view = null;
        String fileType = null;
        for (Map.Entry<String, IViewBuilder> entry : VIEW_BUILDERS.entrySet()) {
            File targetFile = new File(owner.getConfig().getAbstractBaseViewPath(), requestMapping + entry.getKey());
            if (targetFile.exists()) {
                fileType = entry.getKey();
                view = entry.getValue().build(owner, requestMapping);
                break;
            }
        }
        return PairObject.bind(view, fileType);
    }

    /**
     * 文件视图构建器接口
     *
     * @author 刘镇 (suninformation@163.com) on 19/11/07 12:28
     * @since 2.1.0
     */
    public interface IViewBuilder {

        /**
         * 构建视图
         *
         * @param owner          所属容器对象
         * @param requestMapping 请求路径
         * @return 返回视图对象
         * @throws Exception 可能产生的任何异常
         */
        IView build(IWebMvc owner, String requestMapping) throws Exception;
    }
}
