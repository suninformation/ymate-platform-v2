/*
 * Copyright 2007-2017 the original author or authors.
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/7/31 上午12:11
 * @version 1.0
 */
public abstract class AbstractConfigFileParser implements IConfigFileParser {

    protected Map<String, Attribute> __rootAttributes;

    protected Map<String, Category> __categories;

    private boolean __loaded;

    protected boolean __sorted;

    @Override
    public IConfigFileParser load(boolean sorted) {
        if (!__loaded) {
            // 判断是否保证顺序
            if (sorted) {
                __sorted = true;
                __categories = new LinkedHashMap<String, Category>();
                __rootAttributes = new LinkedHashMap<String, Attribute>();
            } else {
                __categories = new HashMap<String, Category>();
                __rootAttributes = new HashMap<String, Attribute>();
            }
            __doLoad();
            //
            __loaded = true;
        }
        return this;
    }

    protected abstract void __doLoad();

    @Override
    public Attribute getAttribute(String key) {
        return this.__rootAttributes.get(key);
    }

    @Override
    public Map<String, Attribute> getAttributes() {
        return __rootAttributes;
    }

    @Override
    public Category getDefaultCategory() {
        return this.__categories.get(DEFAULT_CATEGORY_NAME);
    }

    @Override
    public Category getCategory(String name) {
        return this.__categories.get(name);
    }

    @Override
    public Map<String, Category> getCategories() {
        return __categories;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject _jsonObject = new JSONObject(__sorted);
        //
        JSONObject _jsonAttrs = new JSONObject();
        for (Attribute _attr : __rootAttributes.values()) {
            _attr.appendTo(_jsonAttrs);
        }
        _jsonObject.put(TAG_NAME_ATTRIBUTES, _jsonAttrs);
        //
        JSONArray _jsonCategories = new JSONArray();
        for (Category _category : __categories.values()) {
            _jsonCategories.add(_category.toJSON());
        }
        _jsonObject.put(TAG_NAME_CATEGORIES, _jsonCategories);
        return _jsonObject;
    }
}
