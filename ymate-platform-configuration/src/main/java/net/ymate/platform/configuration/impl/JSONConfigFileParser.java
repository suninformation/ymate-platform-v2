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
package net.ymate.platform.configuration.impl;

import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.configuration.AbstractConfigFileParser;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/06/15 21:13
 * @since 2.1.0
 */
public class JSONConfigFileParser extends AbstractConfigFileParser {

    private IJsonObjectWrapper objectWrapper;

    public JSONConfigFileParser(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            doInit(IOUtils.toString(reader));
        }
    }

    public JSONConfigFileParser(InputStream inputStream) throws IOException {
        doInit(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }

    public JSONConfigFileParser(URL url) throws IOException {
        doInit(IOUtils.toString(url, StandardCharsets.UTF_8));
    }

    public JSONConfigFileParser(String jsonStr) {
        doInit(jsonStr);
    }

    private void doInit(String jsonStr) {
        JsonWrapper jsonWrapper = JsonWrapper.fromJson(jsonStr);
        if (jsonWrapper != null) {
            if (jsonWrapper.isJsonObject()) {
                objectWrapper = jsonWrapper.getAsJsonObject();
            } else {
                objectWrapper = JsonWrapper.createJsonObject().put(TAG_NAME_CATEGORIES, jsonWrapper.getAsJsonArray());
            }
        } else {
            objectWrapper = JsonWrapper.createJsonObject();
        }
    }

    @Override
    protected void onLoad() {
        Category.fromJson(objectWrapper.getJsonArray(TAG_NAME_CATEGORIES), isSorted())
                .forEach(category -> getCategories().put(category.getName(), category));
        if (!getCategories().containsKey(DEFAULT_CATEGORY_NAME)) {
            getCategories().put(DEFAULT_CATEGORY_NAME, new Category(DEFAULT_CATEGORY_NAME, null, null, isSorted()));
        }
    }

    @Override
    public void writeTo(File targetFile) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(targetFile.toPath())) {
            writeTo(outputStream);
        }
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        IOUtils.write(toString(), outputStream, StandardCharsets.UTF_8);
    }
}
