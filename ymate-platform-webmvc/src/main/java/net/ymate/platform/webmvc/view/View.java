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

import java.io.File;

/**
 * @author 刘镇 (suninformation@163.com) on 15/5/28 下午5:44
 */
public class View {

    public static BeetlView beetlView(IWebMvc owner, String path) {
        return BeetlView.bind(owner, path);
    }

    public static BeetlView beetlView(String path) {
        return BeetlView.bind(path);
    }

    public static BeetlView beetlView() {
        return BeetlView.bind();
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
        // 采用系统默认方式处理约定优于配置的URL请求映射
        String[] fileTypes = {HtmlView.FILE_SUFFIX, JspView.FILE_SUFFIX, FreemarkerView.FILE_SUFFIX, VelocityView.FILE_SUFFIX, BeetlView.FILE_SUFFIX};
        IView view = null;
        String fileType = null;
        for (String type : fileTypes) {
            File targetFile = new File(owner.getConfig().getAbstractBaseViewPath(), requestMapping + fileType);
            if (targetFile.exists()) {
                switch (type) {
                    case HtmlView.FILE_SUFFIX:
                        view = HtmlView.bind(owner, requestMapping);
                        fileType = type;
                        break;
                    case JspView.FILE_SUFFIX:
                        view = JspView.bind(owner, requestMapping);
                        fileType = type;
                        break;
                    case FreemarkerView.FILE_SUFFIX:
                        view = FreemarkerView.bind(owner, requestMapping);
                        fileType = type;
                        break;
                    case VelocityView.FILE_SUFFIX:
                        view = VelocityView.bind(owner, requestMapping);
                        fileType = type;
                        break;
                    case BeetlView.FILE_SUFFIX:
                        view = BeetlView.bind(owner, requestMapping);
                        fileType = type;
                        break;
                    default:
                }
            }
        }
        return PairObject.bind(view, fileType);
    }
}
