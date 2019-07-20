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
package net.ymate.platform.webmvc.support;

import net.ymate.platform.core.i18n.II18nEventHandler;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.util.CookieHelper;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * @author 刘镇 (suninformation@163.com) on 15/7/20 上午10:02
 */
public class I18nWebEventHandler implements II18nEventHandler {

    @Override
    public Locale onLocale() {
        String langStr = null;
        // 先尝试取URL参数变量
        if (WebContext.getContext() != null) {
            IWebMvc owner = WebContext.getContext().getOwner();
            String i18nLangKey = owner.getConfig().getLanguageParamName();
            langStr = WebContext.getRequestContext().getAttribute(i18nLangKey);
            if (StringUtils.isBlank(langStr)) {
                // 再尝试从请求参数中获取
                langStr = WebContext.getRequest().getParameter(i18nLangKey);
                if (StringUtils.isBlank(langStr)) {
                    // 最后一次机会，尝试读取Cookies
                    langStr = CookieHelper.bind(owner).getCookie(i18nLangKey).toStringValue();
                }
            }
        }
        Locale locale = null;
        try {
            locale = LocaleUtils.toLocale(StringUtils.trimToNull(langStr));
        } catch (IllegalArgumentException e) {
            if (WebContext.getContext() != null) {
                locale = WebContext.getContext().getLocale();
            }
        }
        return locale;
    }

    @Override
    public void onChanged(Locale locale) {
        if (WebContext.getContext() != null && locale != null) {
            IWebMvc owner = WebContext.getContext().getOwner();
            String i18nLangKey = owner.getConfig().getLanguageParamName();
            CookieHelper.bind(owner).setCookie(i18nLangKey, locale.toString());
        }
    }

    @Override
    public InputStream onLoad(String resourceName) throws IOException {
        if (StringUtils.isNotBlank(resourceName) && WebContext.getContext() != null) {
            File resourceFile = new File(WebContext.getContext().getOwner().getConfig().getResourcesHome(), resourceName);
            if (resourceFile.exists() && resourceFile.isFile() && resourceFile.canRead()) {
                return new FileInputStream(resourceFile);
            }
        }
        return null;
    }
}
