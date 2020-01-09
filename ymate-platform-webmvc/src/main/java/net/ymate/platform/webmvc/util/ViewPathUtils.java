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
package net.ymate.platform.webmvc.util;

import net.ymate.platform.plugin.Plugins;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * @author 刘镇 (suninformation@163.com) on 15/8/15 下午3:26
 */
public class ViewPathUtils {

    private static String BASE_VIEW_PATH;

    private static String PLUGIN_VIEW_PATH;

    /**
     * @return 模板基准路径并以'/WEB-INF'开始，以'/'结束
     */
    public static String getViewPath() {
        if (BASE_VIEW_PATH == null) {
            doInitViewPath();
        }
        return BASE_VIEW_PATH;
    }

    /**
     * @return 插件模板基准路径，以'/WEB-INF'开始，以'/'结束
     */
    public static String getPluginViewPath() {
        if (PLUGIN_VIEW_PATH == null) {
            doInitViewPath();
        }
        return PLUGIN_VIEW_PATH;
    }

    private static String pathChecker(String path, String defaultPath, boolean substring) {
        if (StringUtils.isNotBlank(path)) {
            path = path.replaceAll("\\\\", Type.Const.PATH_SEPARATOR);
            if (substring && path.contains(Type.Const.WEB_INF)) {
                path = StringUtils.substring(path, path.indexOf(Type.Const.WEB_INF));
            }
            if (!path.startsWith(Type.Const.WEB_INF)) {
                path = defaultPath;
            }
            if (!path.endsWith(Type.Const.PATH_SEPARATOR)) {
                path += Type.Const.PATH_SEPARATOR;
            }
            return path;
        }
        return defaultPath;
    }

    private synchronized static void doInitViewPath() {
        if (BASE_VIEW_PATH == null && PLUGIN_VIEW_PATH == null) {
            BASE_VIEW_PATH = pathChecker(WebUtils.getOwner().getConfig().getBaseViewPath(), "/WEB-INF/templates/", false);
            // 为了适应Web环境JSP文件的特殊性(即不能引用工程路径外的JSP文件), 建议采用默认"/WEB-INF/plugins/
            String pluginViewPath = "/WEB-INF/plugins/";
            try {
                Plugins plugins = WebUtils.getOwner().getOwner().getModuleManager().getModule(Plugins.class);
                if (plugins != null) {
                    File pluginHomeFile = plugins.getConfig().getPluginHome();
                    pluginViewPath = pathChecker(pluginHomeFile == null ? null : pluginHomeFile.getPath(), pluginViewPath, true);
                }
            } catch (Throwable ignored) {
                // 一般出现异常的可能性只有插件包未引用导致的NoClassDefFoundError, 可忽略
            }
            PLUGIN_VIEW_PATH = pluginViewPath;
        }
    }
}
