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
package net.ymate.platform.commons;

import freemarker.cache.*;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Freemarker模板引擎配置构建工具类
 *
 * @author 刘镇 (suninformation@163.com) on 2018/3/26 下午2:24
 */
public final class FreemarkerConfigBuilder {

    private Version version;

    private String encoding;

    private String outputEncoding;

    private TemplateExceptionHandler templateExceptionHandler;

    private final List<TemplateLoader> templateLoaders = new ArrayList<>();

    private final Set<File> templateFiles = new HashSet<>();

    private final Map<String, String> templateSources = new HashMap<>();

    public static FreemarkerConfigBuilder create() {
        return new FreemarkerConfigBuilder();
    }

    public Version getVersion() {
        return version != null ? version : Configuration.VERSION_2_3_29;
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

    public String getOutputEncoding() {
        return outputEncoding;
    }

    public FreemarkerConfigBuilder setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
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

    public FreemarkerConfigBuilder addTemplateClass(Class<?> resourceLoaderClass, String basePackagePath) {
        if (resourceLoaderClass != null && StringUtils.isNotBlank(basePackagePath)) {
            this.templateLoaders.add(new ClassTemplateLoader(resourceLoaderClass, basePackagePath));
        }
        return this;
    }

    public Configuration build() throws IOException {
        Configuration config = new Configuration(getVersion());
        config.setDefaultEncoding(getEncoding());
        config.setOutputEncoding(StringUtils.defaultIfBlank(getOutputEncoding(), getEncoding()));
        config.setTemplateExceptionHandler(templateExceptionHandler != null ? templateExceptionHandler : TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        //
        for (File tplFileDir : templateFiles) {
            templateLoaders.add(new FileTemplateLoader(tplFileDir));
        }
        if (!templateSources.isEmpty()) {
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            templateSources.forEach(stringTemplateLoader::putTemplate);
            templateLoaders.add(stringTemplateLoader);
        }
        //
        if (!templateLoaders.isEmpty()) {
            if (templateLoaders.size() > 1) {
                config.setTemplateLoader(new MultiTemplateLoader(templateLoaders.toArray(new TemplateLoader[0])));
            } else {
                config.setTemplateLoader(templateLoaders.get(0));
            }
        }
        //
        return config;
    }
}
