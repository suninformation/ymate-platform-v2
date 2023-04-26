/*
 * Copyright 2007-2022 the original author or authors.
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
package net.ymate.platform.commons.json;

import net.ymate.platform.commons.json.impl.GsonAdapter;
import net.ymate.platform.commons.json.impl.JacksonAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2022/9/2 16:03
 * @since 2.1.2
 */
public class JsonWrapperTest {

    private static final Log LOG = LogFactory.getLog(JsonWrapperTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testJsonWrappers() throws Exception {
        IJsonAdapter[] jsonAdapters = {JsonWrapper.getJsonAdapter(), new JacksonAdapter(), new GsonAdapter()};
        for (IJsonAdapter jsonAdapter : jsonAdapters) {
            LOG.info("JsonAdapter class: " + jsonAdapter.getClass().getName());
            testJsonObjectWrapper(jsonAdapter);
            testJsonArrayWrapper(jsonAdapter);
            testJsonNodeWrapper(jsonAdapter);
            testJsonWrapper(jsonAdapter);
            testJsonSerialize(jsonAdapter);
        }
    }

    private void testJsonObjectWrapper(IJsonAdapter jsonAdapter) {
        // 创建 JsonObject 对象实例并设置 ordered 为有序的
        IJsonObjectWrapper jsonObj = jsonAdapter.createJsonObject(true);
        jsonObj.put("name", "suninformation");
        jsonObj.put("realName", "有理想的鱼");
        jsonObj.put("age", 20);
        jsonObj.put("gender", (String) null);
        jsonObj.put("attrs", jsonAdapter.createJsonObject()
                .put("key1", "value1")
                .put("key2", "value2"));
        // 采用格式化输出并保留值为空的属性
        LOG.info(jsonObj.toString(true, true, true));
        // 取值：
        LOG.info("Name: " + jsonObj.getString("name"));
        LOG.info("Age: " + jsonObj.getInt("age"));
        //
        IJsonObjectWrapper attrs = jsonObj.getJsonObject("attrs");
        LOG.info("Key1: " + attrs.getString("key1"));
        LOG.info("Key2: " + attrs.getString("key2"));
    }

    private void testJsonArrayWrapper(IJsonAdapter jsonAdapter) {
        // 创建 JsonArray 对象实例
        IJsonArrayWrapper jsonArray = jsonAdapter.createJsonArray(new Object[]{1, null, 2, 3, false, true})
                .add(jsonAdapter.createJsonArray().add(new String[]{"a", "b"}))
                .add(jsonAdapter.createJsonObject(true)
                        .put("name", "suninformation")
                        .put("realName", "有理想的鱼")
                        .put("age", 20)
                        .put("gender", (String) null))
                .add(11);
        // 采用格式化输出并保留值为空的属性
        LOG.info(jsonArray.toString(true, false));
        // 取值：
        LOG.info("Index3: " + jsonArray.getInt(3));
        LOG.info("Index4: " + jsonArray.getString(4));
        //
        IJsonObjectWrapper jsonObj = jsonArray.getJsonObject(7);
        LOG.info("Name: " + jsonObj.getString("name"));
        LOG.info("Age: " + jsonObj.getInt("age"));
    }

    private void testJsonNodeWrapper(IJsonAdapter jsonAdapter) {
        // 创建复杂的 JsonObject 对象
        IJsonObjectWrapper jsonObj = jsonAdapter.createJsonObject(true)
                .put("name", "suninformation")
                .put("realName", "有理想的鱼")
                .put("age", 20)
                .put("array", jsonAdapter.createJsonArray(new String[]{"a", "b"}))
                .put("attrs", jsonAdapter.createJsonObject()
                        .put("key1", "value1")
                        .put("key2", "value2"));
        // 采用格式化输出并保留值为空的属性
        LOG.info(jsonObj.toString(true, true));
        // 遍历：
        for (String key : jsonObj.keySet()) {
            IJsonNodeWrapper nodeWrapper = jsonObj.get(key);
            if (nodeWrapper.isJsonArray()) {
                // 判断当前元素是否为 JsonArray 对象
                LOG.info(nodeWrapper.getJsonArray().getString(0));
            } else if (nodeWrapper.isJsonObject()) {
                // 判断当前元素是否为 JsonObject 对象
                LOG.info(nodeWrapper.getJsonObject().getString("key1"));
            } else {
                // 否则为值对象，直接取值
                LOG.info(nodeWrapper.getString());
            }
        }
    }

    private void testJsonWrapper(IJsonAdapter jsonAdapter) {
        String jsonStr = "{\"age\":20,\"name\":\"suninformation\",\"real_name\":\"有理想的鱼\"}";
        // 将字符串转换为 JSON 对象
        JsonWrapper jsonWrapper = jsonAdapter.fromJson(jsonStr);
        if (jsonWrapper.isJsonObject()) {
            IJsonObjectWrapper jsonObj = jsonWrapper.getAsJsonObject();
            if (jsonObj != null) {
                // 取值：
                LOG.info("Name: " + jsonObj.getString("name"));
                LOG.info("Age: " + jsonObj.getInt("age"));
                LOG.info("RealName: " + jsonObj.getString("real_name"));
            }
        }
        // 将 JSON 对象格式化输出为字符串
        LOG.info(jsonWrapper.toString(true, true));
    }

    private void testJsonSerialize(IJsonAdapter jsonAdapter) throws Exception {
        User user = new User();
        user.setName("suninformation");
        user.setAge(20);
        user.setRealName("有理想的鱼");
        //
        User otherUser = new User();
        otherUser.setName("YMP");
        otherUser.setAge(16);
        otherUser.setRealName("YMP");
        //
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(otherUser);
        //
        byte[] serializeArr = jsonAdapter.serialize(users, true);
        List<User> newUsers = jsonAdapter.deserialize(serializeArr, new TypeReferenceWrapper<List<User>>() {
        });
        LOG.info("newUsers: " + newUsers);
        // 采用 snakeCase 模式输出和反序列化操作
        String jsonStr = jsonAdapter.toJsonString(user, false, false, true);
        User newUser = jsonAdapter.deserialize(jsonStr, true, User.class);
        LOG.info("newUser: " + newUser);
    }


    static class User {

        private String name;

        private Integer age;

        private String realName;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        @Override
        public String toString() {
            return String.format("User{name='%s', age=%d, realName='%s'}", name, age, realName);
        }
    }
}