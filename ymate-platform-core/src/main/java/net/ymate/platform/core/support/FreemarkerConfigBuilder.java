/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.core.support;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Freemarker模板引擎配置构建工具类
 *
 * @author 刘镇 (suninformation@163.com) on 2018/3/26 下午2:24
 * @version 1.0
 */
public final class FreemarkerConfigBuilder {

    private Version version;

    private String encoding;

    private TemplateExceptionHandler templateExceptionHandler;

    private List<TemplateLoader> templateLoaders = new ArrayList<TemplateLoader>();

    private Set<File> templateFiles = new HashSet<File>();

    private Map<String, String> templateSources = new HashMap<String, String>();

    public static FreemarkerConfigBuilder create() {
        return new FreemarkerConfigBuilder();
    }

    public Version getVersion() {
        return version != null ? version : Configuration.VERSION_2_3_22;
    }

    public FreemarkerConfigBuilder setVersion(Version version) {
        this.version = version;
        return this;
    }

    public String getEncoding() {
        return StringUtils.defaultIfBlank(encoding, "UTF-8");
    }

    public FreemarkerConfigBuilder setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public TemplateExceptionHandler getTemplateExceptionHandler() {
        return templateExceptionHandler;
    }

    public FreemarkerConfigBuilder setTemplateExceptionHandler(TemplateExceptionHandler templateExceptionHandler) {
        this.templateExceptionHandler = templateExceptionHandler;
        return this;
    }

    public FreemarkerConfigBuilder addTemplateLoader(TemplateLoader... templateLoaders) {
        if (ArrayUtils.isNotEmpty(templateLoaders)) {
            this.templateLoaders.addAll(Arrays.asList(templateLoaders));
        }
        return this;
    }

    public FreemarkerConfigBuilder addTemplateFileDir(File... fileBaseDirs) {
        if (ArrayUtils.isNotEmpty(fileBaseDirs)) {
            this.templateFiles.addAll(Arrays.asList(fileBaseDirs));
        }
        return this;
    }

    public FreemarkerConfigBuilder addTemplateSource(String name, String templateSource) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(templateSource)) {
            this.templateSources.put(name, templateSource);
        }
        return this;
    }

    public Configuration build() throws IOException {
        Configuration _config = new Configuration(getVersion());
        _config.setDefaultEncoding(getEncoding());
        _config.setTemplateExceptionHandler(templateExceptionHandler != null ? templateExceptionHandler : TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        //
        for (File _tplFileDir : templateFiles) {
            templateLoaders.add(new FileTemplateLoader(_tplFileDir));
        }
        if (!templateSources.isEmpty()) {
            StringTemplateLoader _stringTplLoader = new StringTemplateLoader();
            for (Map.Entry<String, String> _entry : templateSources.entrySet()) {
                _stringTplLoader.putTemplate(_entry.getKey(), _entry.getValue());
            }
            templateLoaders.add(_stringTplLoader);
        }
        //
        if (!templateLoaders.isEmpty()) {
            if (templateLoaders.size() > 1) {
                _config.setTemplateLoader(new MultiTemplateLoader(templateLoaders.toArray(new TemplateLoader[templateLoaders.size()])));
            } else {
                _config.setTemplateLoader(templateLoaders.get(0));
            }
        }
        //
        return _config;
    }
}
