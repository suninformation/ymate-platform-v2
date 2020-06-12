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
package net.ymate.platform.configuration;

import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.core.configuration.IConfigFileParser;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/7/31 上午12:11
 */
public abstract class AbstractConfigFileParser implements IConfigFileParser {

    private Map<String, Attribute> attributes;

    private Map<String, Category> categories;

    private boolean loaded;

    private boolean sorted;

    @Override
    public IConfigFileParser load(boolean sorted) {
        if (!loaded) {
            // 判断是否保证顺序
            if (sorted) {
                this.sorted = true;
                categories = new LinkedHashMap<>();
                attributes = new LinkedHashMap<>();
            } else {
                categories = new HashMap<>(16);
                attributes = new HashMap<>(16);
            }
            onLoad();
            //
            loaded = true;
        }
        return this;
    }

    /**
     * 配置文件分析过程
     */
    protected abstract void onLoad();

    public boolean isSorted() {
        return sorted;
    }

    @Override
    public Attribute getAttribute(String key) {
        return this.attributes.get(key);
    }

    @Override
    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public Category getDefaultCategory() {
        return this.categories.get(DEFAULT_CATEGORY_NAME);
    }

    @Override
    public Category getCategory(String name) {
        return this.categories.get(name);
    }

    @Override
    public Map<String, Category> getCategories() {
        return categories;
    }

    @Override
    public IJsonObjectWrapper toJson() {
        IJsonObjectWrapper jsonObject = JsonWrapper.createJsonObject(sorted);
        //
        IJsonObjectWrapper jsonAttrs = JsonWrapper.createJsonObject();
        attributes.values().forEach((attr) -> {
            attr.appendTo(jsonAttrs);
        });
        jsonObject.put(TAG_NAME_ATTRIBUTES, jsonAttrs);
        //
        IJsonArrayWrapper jsonCategories = JsonWrapper.createJsonArray();
        categories.values().forEach((category) -> {
            jsonCategories.add(category.toJson());
        });
        jsonObject.put(TAG_NAME_CATEGORIES, jsonCategories);
        return jsonObject;
    }
}
