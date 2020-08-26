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
package net.ymate.platform.webmvc;

import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.webmvc.view.impl.JsonView;
import net.ymate.platform.webmvc.view.impl.TextView;

import java.io.Serializable;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/08/22 22:01
 * @since 2.1.0
 */
public interface IWebResult<CODE_TYPE extends Serializable> {

    boolean isSuccess();

    CODE_TYPE code();

    IWebResult<CODE_TYPE> code(CODE_TYPE code);

    String msg();

    IWebResult<CODE_TYPE> msg(String msg);

    IWebResult<CODE_TYPE> data(Object data);

    <T> T data();

    IWebResult<CODE_TYPE> attrs(Map<String, Object> attrs);

    Map<String, Object> attrs();

    <T> T dataAttr(String dataKey);

    IWebResult<CODE_TYPE> dataAttr(String dataKey, Object dataValue);

    <T> T attr(String attrKey);

    IWebResult<CODE_TYPE> attr(String attrKey, Object attrValue);

    IWebResult<CODE_TYPE> withContentType();

    IWebResult<CODE_TYPE> keepNullValue();

    IWebResult<CODE_TYPE> dataFilter(IDateFilter dateFilter);

    IJsonObjectWrapper toJsonObject();

    JsonView toJsonView();

    JsonView toJsonView(String callback);

    String toXml(boolean cdata);

    TextView toXmlView();

    TextView toXmlView(boolean cdata);

    /**
     * 数据过滤器接口
     */
    interface IDateFilter {

        /**
         * 执行数据过滤
         *
         * @param dataAttr  当前数据是否为data属性
         * @param itemName  属性名称
         * @param itemValue 属性值对象
         * @return 若返回null则该属性将被忽略
         */
        Object filter(boolean dataAttr, String itemName, Object itemValue);
    }
}
