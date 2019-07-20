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
package net.ymate.platform.commons.lang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 树对象，使用级联方式存储各种数据类型，不限层级深度
 *
 * @author 刘镇 (suninformation@163.com) on 2010-3-27 下午10:32:09
 */
@SuppressWarnings("unchecked")
public class TreeObject implements Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = -2971996971836985367L;

    //////////

    private static final String KEY_CLASS = "_c";

    private static final String KEY_VALUE = "_v";

    //////////

    /**
     * 值模式
     */
    public static final int MODE_VALUE = 1;

    /**
     * 数组集合模式
     */
    public static final int MODE_ARRAY = 3;

    /**
     * 映射模式
     */
    public static final int MODE_MAP = 2;

    //////////

    /**
     * NULL类型
     */
    public static final int TYPE_NULL = 0;

    /**
     * Integer类型
     */
    public static final int TYPE_INTEGER = 1;

    /**
     * 混合String类型（通过base64编码的字符串）
     */
    public static final int TYPE_MIX_STRING = 2;

    /**
     * String类型
     */
    public static final int TYPE_STRING = 3;

    /**
     * Long类型
     */
    public static final int TYPE_LONG = 4;

    /**
     * Time类型（UTC时间）
     */
    public static final int TYPE_TIME = 5;

    /**
     * Boolean类型
     */
    public static final int TYPE_BOOLEAN = 6;

    /**
     * Float类型
     */
    public static final int TYPE_FLOAT = 7;

    /**
     * Double类型
     */
    public static final int TYPE_DOUBLE = 8;

    /**
     * Map&lt;String, ? extends Object&gt;类型
     */
    public static final int TYPE_MAP = 9;

    /**
     * Collection&lt;? extends Object&gt;类型
     */
    public static final int TYPE_COLLECTION = 10;

    /**
     * Byte类型
     */
    public static final int TYPE_BYTE = 11;

    /**
     * Character类型
     */
    public static final int TYPE_CHAR = 12;

    /**
     * Short类型
     */
    public static final int TYPE_SHORT = 13;

    /**
     * byte[]类型
     */
    public static final int TYPE_BYTES = 14;

    /**
     * Object类型
     */
    public static final int TYPE_OBJECT = 15;

    /**
     * 未知类型
     */
    public static final int TYPE_UNKNOWN = 99;

    /**
     * 树对象类型
     */
    public static final int TYPE_TREE_OBJECT = 100;

    /**
     * 当前TreeObject对象储值模式
     */
    private int mode = MODE_VALUE;

    /**
     * 当前TreeObject对象数据类型
     */
    private int type = TYPE_NULL;

    /**
     * 当前TreeObject对象存储的值对象
     */
    private Object object;

    /**
     * @param o 目标对象
     * @return 检测目标对象的数据类型并返回类型常量值
     */
    private static int checkType(Object o) {
        if (o == null) {
            return TYPE_NULL;
        }
        Class<?> clazz = o.getClass();
        int returnValue = TYPE_OBJECT;
        if (o instanceof Integer || int.class.isAssignableFrom(clazz)) {
            returnValue = TYPE_INTEGER;
        } else if (o instanceof Long || long.class.isAssignableFrom(clazz)) {
            returnValue = TYPE_LONG;
        } else if (o instanceof String) {
            // String和MixString统一采用String存储
            returnValue = TYPE_MIX_STRING;
        } else if (o instanceof Boolean || boolean.class.isAssignableFrom(clazz)) {
            returnValue = TYPE_BOOLEAN;
        } else if (o instanceof Float || float.class.isAssignableFrom(clazz)) {
            returnValue = TYPE_FLOAT;
        } else if (o instanceof Double || double.class.isAssignableFrom(clazz)) {
            returnValue = TYPE_DOUBLE;
        } else if (o instanceof Byte || byte.class.isAssignableFrom(clazz)) {
            returnValue = TYPE_BYTE;
        } else if (o instanceof Character || char.class.isAssignableFrom(clazz)) {
            returnValue = TYPE_CHAR;
        } else if (o instanceof Short || short.class.isAssignableFrom(clazz)) {
            returnValue = TYPE_SHORT;
        } else if (o instanceof byte[] || o instanceof Byte[]
                || byte[].class.isAssignableFrom(clazz)
                || Byte[].class.isAssignableFrom(clazz)) {
            returnValue = TYPE_BYTES;
        } else if (o instanceof Map) {
            returnValue = TYPE_MAP;
        } else if (o instanceof Collection) {
            returnValue = TYPE_COLLECTION;
        } else if (o instanceof TreeObject) {
            returnValue = TYPE_TREE_OBJECT;
        }
        return returnValue;
    }

    //////////

    public static TreeObject fromJson(String jsonStr) {
        return fromJson(JSON.parseObject(jsonStr));
    }

    public static TreeObject fromJson(JSONObject json) {
        if (json == null) {
            throw new NullArgumentException("json");
        }
        if (!json.containsKey(KEY_CLASS)) {
            throw new IllegalArgumentException();
        }
        int classType = json.getIntValue(KEY_CLASS);
        TreeObject target = new TreeObject();
        switch (classType) {
            case TYPE_MAP: {
                // MAP
                JSONObject value = json.getJSONObject(KEY_VALUE);
                for (String key : value.keySet()) {
                    target.put(key, fromJson(value.getJSONObject(key)));
                }
                break;
            }
            case TYPE_COLLECTION: {
                // COLLECTION
                JSONArray value = json.getJSONArray(KEY_VALUE);
                for (int idx = 0; idx < value.size(); idx++) {
                    target.add(fromJson(value.getJSONObject(idx)));
                }
                break;
            }
            default: {
                // VALUE
                Object value = json.get(KEY_VALUE);
                if (classType == TYPE_MIX_STRING) {
                    value = new String(Base64.decodeBase64((String) value));
                } else if (classType == TYPE_BYTES) {
                    value = Base64.decodeBase64((String) value);
                }
                target = new TreeObject(value, classType);
                break;
            }
        }
        return target;
    }

    public JSONObject toJson() {
        return toJson(this);
    }

    public static JSONObject toJson(TreeObject tObject) {
        if (tObject == null) {
            return null;
        }
        JSONObject returnJson;
        // MAP
        if (tObject.isMap()) {
            TreeObject itemObject;
            Map<String, TreeObject> nodeValue = tObject.getMap();
            if (nodeValue != null && !nodeValue.isEmpty()) {
                JSONObject itemJson = new JSONObject();
                for (Map.Entry<String, TreeObject> entry : nodeValue.entrySet()) {
                    if (StringUtils.isNotBlank(entry.getKey())) {
                        itemObject = entry.getValue();
                        if (itemObject != null) {
                            returnJson = toJson(itemObject);
                            if (returnJson != null) {
                                if (!returnJson.containsKey(KEY_VALUE)) {
                                    JSONObject nodeAttrValue = new JSONObject();
                                    nodeAttrValue.put(KEY_VALUE, returnJson);
                                    nodeAttrValue.put(KEY_CLASS, TYPE_MAP);
                                    itemJson.put(entry.getKey(), nodeAttrValue);
                                    continue;
                                }
                                itemJson.put(entry.getKey(), returnJson);
                            }
                        }
                    }
                }
                // 处理值为map类型时没有_v的情况，保证结构为{_v:{},_c:9}
                if (!itemJson.containsKey(KEY_VALUE)) {
                    JSONObject nodeAttrValue = new JSONObject();
                    nodeAttrValue.put(KEY_CLASS, TYPE_MAP);
                    nodeAttrValue.put(KEY_VALUE, itemJson);
                    return nodeAttrValue;
                } else {
                    return itemJson;
                }
            } else {
                JSONObject itemJson = new JSONObject();
                itemJson.put(KEY_CLASS, TYPE_MAP);
                itemJson.put(KEY_VALUE, new JSONObject());
                return itemJson;
            }
        } else if (tObject.isList()) {
            // ARRAY
            List<TreeObject> nodeValue = tObject.getList();
            if (nodeValue != null && !nodeValue.isEmpty()) {
                JSONArray itemJson = new JSONArray();
                for (TreeObject itemObject : nodeValue) {
                    if (itemObject != null) {
                        returnJson = toJson(itemObject);
                        if (returnJson != null) {
                            itemJson.add(returnJson);
                        }
                    }
                }
                // 保证数组的格式:{_value:[{_class:,_value},{}..]}
                JSONObject nodeJson = new JSONObject();
                nodeJson.put(KEY_VALUE, itemJson);
                nodeJson.put(KEY_CLASS, TYPE_COLLECTION);
                return nodeJson;
            } else {
                JSONObject nodeJson = new JSONObject();
                nodeJson.put(KEY_VALUE, new JSONArray());
                nodeJson.put(KEY_CLASS, TYPE_COLLECTION);
                return nodeJson;
            }
        } else {
            // VALUE
            JSONObject nodeJson = new JSONObject();
            nodeJson.put(KEY_CLASS, tObject.getType());
            //
            switch (tObject.getType()) {
                case TYPE_MIX_STRING:
                    // 混淆(Mix)类型编码为Base64
                    if (tObject.getObject() != null) {
                        String bStr = Base64.encodeBase64String(tObject.toMixStringValue().getBytes());
                        nodeJson.put(KEY_VALUE, bStr);
                    }
                    break;
                case TYPE_BYTES:
                    if (tObject.getObject() instanceof byte[]) {
                        String bytes = String.valueOf(Base64.encodeBase64String(tObject.toBytesValue()));
                        nodeJson.put(KEY_VALUE, bytes);
                    }
                    break;
                case TYPE_TIME:
                    nodeJson.put(KEY_VALUE, tObject.toTimeValue());
                    break;
                default:
                    nodeJson.put(KEY_VALUE, tObject.getObject());
                    break;
            }
            return nodeJson;
        }
    }

    //////////

    public static TreeObject fromXml(String xml) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String toXml() {
        return toXml(this);
    }

    public static String toXml(TreeObject tObject) {
        // TODO
        throw new UnsupportedOperationException();
    }

    //////////

    public TreeObject() {
    }

    public TreeObject(boolean bool) {
        object = bool;
        type = TYPE_BOOLEAN;
    }

    public TreeObject(Boolean bool) {
        object = bool != null && bool;
        type = TYPE_BOOLEAN;
    }

    public TreeObject(byte b) {
        object = b;
        type = TYPE_BYTE;
    }

    public TreeObject(Byte b) {
        object = b != null ? b : Byte.MIN_VALUE;
        type = TYPE_BYTE;
    }

    public TreeObject(byte[] bytes) {
        object = bytes;
        type = TYPE_BYTES;
    }

    public TreeObject(Byte[] bytes) {
        object = bytes;
        type = TYPE_BYTES;
    }

    public TreeObject(char c) {
        object = c;
        type = TYPE_CHAR;
    }

    public TreeObject(Character c) {
        object = c != null ? c : Character.MIN_VALUE;
        type = TYPE_CHAR;
    }

    public TreeObject(Collection<?> c) {
        object = c;
        type = TYPE_COLLECTION;
    }

    public TreeObject(double d) {
        object = d;
        type = TYPE_DOUBLE;
    }

    public TreeObject(Double d) {
        object = d != null ? d : Double.MIN_VALUE;
        type = TYPE_DOUBLE;
    }

    public TreeObject(float f) {
        object = f;
        type = TYPE_FLOAT;
    }

    public TreeObject(Float f) {
        object = f != null ? f : Float.MIN_VALUE;
        type = TYPE_FLOAT;
    }

    public TreeObject(int i) {
        object = i;
        type = TYPE_INTEGER;
    }

    public TreeObject(Integer i) {
        object = i != null ? i : Integer.MIN_VALUE;
        type = TYPE_INTEGER;
    }

    public TreeObject(long l) {
        object = l;
        type = TYPE_LONG;
    }

    /**
     * 构造器
     *
     * @param t      时间毫秒值
     * @param isTime 是否时间类型，如果是时间类型，则存储的是时间的UTC时间毫秒值
     */
    public TreeObject(long t, boolean isTime) {
        object = t;
        type = isTime ? TYPE_TIME : TYPE_LONG;
    }

    public TreeObject(Long l) {
        object = l != null ? l : Long.MIN_VALUE;
        type = TYPE_LONG;
    }

    /**
     * 构造器
     *
     * @param t      时间毫秒值
     * @param isTime 是否时间类型，如果是时间类型，则存储的是时间的UTC时间毫秒值
     */
    public TreeObject(Long t, boolean isTime) {
        object = t != null ? t : Long.MIN_VALUE;
        type = isTime ? TYPE_TIME : TYPE_LONG;
    }

    public TreeObject(Map<?, ?> m) {
        object = m;
        type = TYPE_MAP;
    }

    public TreeObject(short s) {
        object = s;
        type = TYPE_SHORT;
    }

    public TreeObject(Short s) {
        object = s != null ? s : Short.MIN_VALUE;
        type = TYPE_SHORT;
    }

    public TreeObject(String s) {
        object = s;
        type = TYPE_STRING;
    }

    /**
     * 构造器
     *
     * @param s     需要存储的字符串
     * @param isMix 是否混合字符串，如果是混合字符串，那么type类型为MIX_STRING_TYPE，存储的内部对象还是原始的s
     */
    public TreeObject(String s, boolean isMix) {
        object = s;
        type = isMix ? TYPE_MIX_STRING : TYPE_STRING;
    }

    public TreeObject(TreeObject tObject) {
        if (tObject != null) {
            object = tObject.object;
            type = tObject.type;
            mode = tObject.mode;
        }
    }

    /**
     * 构造器，使用此构造器可能产生两个易发生混淆的情况：<br>
     * 1、会忽略MIX_STRING和STRING的差异，默认为MIX_STRING；<br>
     * 2、会忽略LONG和TIME的差异，默认为LONG
     *
     * @param o 任意类型对象
     */
    public TreeObject(Object o) {
        if (o == null) {
            type = TYPE_NULL;
            object = null;
            return;
        }
        object = o;
        //
        if (o instanceof Integer) {
            type = TYPE_INTEGER;
        } else if (int.class.isAssignableFrom(o.getClass())) {
            type = TYPE_INTEGER;
        } else if (o instanceof String) {
            type = TYPE_MIX_STRING;
        } else if (o instanceof Long) {
            type = TYPE_LONG;
        } else if (long.class.isAssignableFrom(o.getClass())) {
            type = TYPE_LONG;
        } else if (o instanceof Boolean) {
            type = TYPE_BOOLEAN;
        } else if (boolean.class.isAssignableFrom(o.getClass())) {
            type = TYPE_BOOLEAN;
        } else if (o instanceof Float) {
            type = TYPE_FLOAT;
        } else if (float.class.isAssignableFrom(o.getClass())) {
            type = TYPE_FLOAT;
        } else if (o instanceof Double) {
            type = TYPE_DOUBLE;
        } else if (double.class.isAssignableFrom(o.getClass())) {
            type = TYPE_DOUBLE;
        } else if (o instanceof Map) {
            type = TYPE_MAP;
        } else if (o instanceof Collection) {
            type = TYPE_COLLECTION;
        } else if (o instanceof Byte) {
            type = TYPE_BYTE;
        } else if (byte.class.isAssignableFrom(o.getClass())) {
            type = TYPE_BYTE;
        } else if (o instanceof Character) {
            type = TYPE_CHAR;
        } else if (char.class.isAssignableFrom(o.getClass())) {
            type = TYPE_CHAR;
        } else if (o instanceof Short) {
            type = TYPE_SHORT;
        } else if (short.class.isAssignableFrom(o.getClass())) {
            type = TYPE_SHORT;
        } else if (o instanceof byte[]) {
            type = TYPE_BYTES;
        } else if (o instanceof Byte[]) {
            type = TYPE_BYTES;
        } else if (o instanceof TreeObject) {
            TreeObject tObject = (TreeObject) o;
            object = tObject.object;
            this.type = tObject.type;
            mode = tObject.mode;
        } else {
            this.type = TYPE_OBJECT;
        }
    }

    /**
     * 构造器
     *
     * @param o    简单值类型对象
     * @param type 需要输入的对象类型，是检查类型，输入的o如果不是此类型，则尝试转换，如果无法转换，则对应的o为type类型的对应无效值
     */
    public TreeObject(Object o, int type) {
        object = o;
        this.type = type;
        switch (type) {
            case TYPE_NULL:
                object = null;
                break;
            case TYPE_INTEGER:
                object = new BlurObject(o).toIntValue();
                break;
            case TYPE_MIX_STRING:
            case TYPE_STRING:
                object = new BlurObject(o).toStringValue();
                break;
            case TYPE_LONG:
            case TYPE_TIME:
                object = new BlurObject(o).toLongValue();
                break;
            case TYPE_BOOLEAN:
                object = new BlurObject(o).toBooleanValue();
                break;
            case TYPE_FLOAT:
                object = new BlurObject(o).toFloatValue();
                break;
            case TYPE_DOUBLE:
                object = new BlurObject(o).toDoubleValue();
                break;
            case TYPE_MAP:
            case TYPE_COLLECTION:
            case TYPE_UNKNOWN:
            case TYPE_OBJECT:
                break;
            case TYPE_BYTE:
                object = new BlurObject(o).toByteValue();
                break;
            case TYPE_CHAR:
                object = new BlurObject(o).toCharValue();
                break;
            case TYPE_SHORT:
                object = new BlurObject(o).toShortValue();
                break;
            case TYPE_BYTES:
                object = new BlurObject(o).toBytesValue();
                break;
            case TYPE_TREE_OBJECT:
                if (o instanceof TreeObject) {
                    TreeObject tObject = (TreeObject) o;
                    object = tObject.object;
                    this.type = tObject.type;
                    mode = tObject.mode;
                } else {
                    this.type = TYPE_OBJECT;
                }
                break;
            default:
        }
    }

    ////////

    public TreeObject add(boolean b) {
        return add(b, TYPE_BOOLEAN);
    }

    public TreeObject add(Boolean b) {
        return add(b != null && b, TYPE_BOOLEAN);
    }

    public TreeObject add(byte b) {
        return add(b, TYPE_BYTE);
    }

    public TreeObject add(Byte b) {
        return add(b != null ? b : 0, TYPE_BYTE);
    }

    public TreeObject add(byte[] bytes) {
        return add(bytes, TYPE_BYTES);
    }

    public TreeObject add(Byte[] bytes) {
        return add(bytes, TYPE_BYTES);
    }

    public TreeObject add(char c) {
        return add(c, TYPE_CHAR);
    }

    public TreeObject add(Character c) {
        return add(c != null ? c : Character.MIN_CODE_POINT, TYPE_CHAR);
    }

    public TreeObject add(double d) {
        return add(d, TYPE_DOUBLE);
    }

    public TreeObject add(Double d) {
        return add(d != null ? d : 0d, TYPE_DOUBLE);
    }

    public TreeObject add(float f) {
        return add(f, TYPE_FLOAT);
    }

    public TreeObject add(Float f) {
        return add(f != null ? f : 0f, TYPE_FLOAT);
    }

    public TreeObject add(int i) {
        return add(i, TYPE_INTEGER);
    }

    public TreeObject add(Integer i) {
        return add(i != null ? i : 0, TYPE_INTEGER);
    }

    public TreeObject add(long l) {
        return add(l, TYPE_LONG);
    }

    public TreeObject add(long t, boolean isTime) {
        return add(t, isTime ? TYPE_TIME : TYPE_LONG);
    }

    public TreeObject add(Long l) {
        return add(l != null ? l : 0, TYPE_LONG);
    }

    public TreeObject add(Long t, boolean isTime) {
        return add(t != null ? t : 0, isTime ? TYPE_TIME : TYPE_LONG);
    }

    public TreeObject add(Object o) {
        return add(o, checkType(o));
    }

    /**
     * 添加元素
     *
     * @param o    Object对象
     * @param type 指定type类型
     * @return 返回当前TreeObject实例
     */
    public TreeObject add(Object o, int type) {
        return add(new TreeObject(o, type));
    }

    public TreeObject add(short s) {
        return add(s, TYPE_SHORT);
    }

    public TreeObject add(Short s) {
        return add(s != null ? s : 0, TYPE_SHORT);
    }

    public TreeObject add(String s) {
        return add(s, TYPE_STRING);
    }

    /**
     * 添加元素
     *
     * @param s     String字符串
     * @param isMix 指定是否混淆
     * @return 返回当前TreeObject实例
     */
    public TreeObject add(String s, boolean isMix) {
        return add(s, isMix ? TYPE_MIX_STRING : TYPE_STRING);
    }

    public TreeObject add(TreeObject tObject) {
        if (tObject != null) {
            if (mode != MODE_MAP) {
                if (mode == MODE_VALUE) {
                    type = TYPE_TREE_OBJECT;
                    mode = MODE_ARRAY;
                }
                if (object == null) {
                    // 创建一个线程安全的集合
                    object = new CopyOnWriteArrayList<TreeObject>();
                }
                ((List<TreeObject>) object).add(tObject);
            } else {
                throw new IllegalStateException();
            }
        }
        return this;
    }

    //////////

    public TreeObject put(String k, boolean b) {
        return put(k, b, TYPE_BOOLEAN);
    }

    public TreeObject put(String k, Boolean b) {
        return put(k, b, TYPE_BOOLEAN);
    }

    public TreeObject put(String k, byte b) {
        return put(k, b, TYPE_BYTE);
    }

    public TreeObject put(String k, Byte b) {
        return put(k, b, TYPE_BYTE);
    }

    public TreeObject put(String k, byte[] bytes) {
        return put(k, bytes, TYPE_BYTES);
    }

    public TreeObject put(String k, Byte[] bytes) {
        return put(k, bytes, TYPE_BYTES);
    }

    public TreeObject put(String k, char c) {
        return put(k, c, TYPE_CHAR);
    }

    public TreeObject put(String k, Character c) {
        return put(k, c, TYPE_CHAR);
    }

    public TreeObject put(String k, double d) {
        return put(k, d, TYPE_DOUBLE);
    }

    public TreeObject put(String k, Double d) {
        return put(k, d, TYPE_DOUBLE);
    }

    public TreeObject put(String k, float f) {
        return put(k, f, TYPE_FLOAT);
    }

    public TreeObject put(String k, Float f) {
        return put(k, f, TYPE_FLOAT);
    }

    public TreeObject put(String k, int i) {
        return put(k, i, TYPE_INTEGER);
    }

    public TreeObject put(String k, Integer i) {
        return put(k, i, TYPE_INTEGER);
    }

    public TreeObject put(String k, long l) {
        return put(k, l, TYPE_LONG);
    }

    public TreeObject put(String k, long t, boolean isTime) {
        return put(k, t, isTime ? TYPE_TIME : TYPE_LONG);
    }

    public TreeObject put(String k, Long l) {
        return put(k, l, TYPE_LONG);
    }

    public TreeObject put(String k, Long t, boolean isTime) {
        return put(k, t, isTime ? TYPE_TIME : TYPE_LONG);
    }

    public TreeObject put(String k, Object o) {
        return put(k, o, checkType(o));
    }

    public TreeObject put(String k, Object o, int type) {
        if (o != null) {
            put(k, new TreeObject(o, type));
        }
        return this;
    }

    public TreeObject put(String k, short s) {
        return put(k, s, TYPE_SHORT);
    }

    public TreeObject put(String k, Short s) {
        return put(k, s, TYPE_SHORT);
    }

    public TreeObject put(String k, String s) {
        return put(k, s, TYPE_STRING);
    }

    public TreeObject put(String k, String s, boolean isMix) {
        return put(k, s, isMix ? TYPE_MIX_STRING : TYPE_STRING);
    }

    public TreeObject put(String k, TreeObject tObject) {
        if (StringUtils.isNotBlank(k) && tObject != null) {
            if (mode != MODE_ARRAY) {
                if (mode == MODE_VALUE) {
                    type = TYPE_TREE_OBJECT;
                    mode = MODE_MAP;
                }
                if (object == null) {
                    // 创建一个线程安全的映射
                    object = new ConcurrentHashMap<String, TreeObject>(16);
                }
                ((Map<String, TreeObject>) object).put(k, tObject);
            } else {
                throw new IllegalStateException();
            }
        }
        return this;
    }

    //////////

    /**
     * @param index 序列索引
     * @return 若存在指定序列的对象且对象不为空，则返回true
     */
    public boolean has(int index) {
        if (isMap() || isValue()) {
            throw new IllegalStateException();
        }
        if (index >= 0 && isList()) {
            List<TreeObject> list = ((List<TreeObject>) object);
            return list != null && list.size() > 0 && index < list.size() && list.get(index) != null;
        }
        return false;
    }

    /**
     * @param key 元素KEY
     * @return 若当前为映射模式，并存在key对应的元素且此对象不为空，是返回true
     */
    public boolean has(String key) {
        if (isList() || isValue()) {
            throw new IllegalStateException();
        }
        if (StringUtils.isNotBlank(key) && isMap()) {
            Map<String, TreeObject> map = ((Map<String, TreeObject>) object);
            return map != null && map.size() > 0 && map.get(key) != null;
        }
        return false;
    }

    /**
     * @return 是否数组集合模式
     */
    public boolean isList() {
        return mode == MODE_ARRAY;
    }

    /**
     * @return 是否映射模式
     */
    public boolean isMap() {
        return mode == MODE_MAP;
    }

    /**
     * @return 是否值模式
     */
    public boolean isValue() {
        return mode == MODE_VALUE;
    }

    public int getType() {
        return type;
    }

    //////////

    public List<TreeObject> getList() {
        if (isList()) {
            List<TreeObject> returnValue = new ArrayList<>();
            List<TreeObject> list = (List<TreeObject>) object;
            if (list != null && !list.isEmpty()) {
                list.stream().filter(Objects::nonNull).forEachOrdered(returnValue::add);
            }
            return returnValue;
        }
        throw new IllegalStateException();
    }

    public Map<String, TreeObject> getMap() {
        if (isMap()) {
            Map<String, TreeObject> returnValue = new HashMap<>(16);
            Map<String, TreeObject> map = (Map<String, TreeObject>) object;
            if (map != null && !map.isEmpty()) {
                returnValue = map.entrySet().stream()
                        .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
                        .filter(entry -> entry.getValue() != null)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, () -> new HashMap<>(16)));
            }
            return returnValue;
        }
        throw new IllegalStateException();
    }

    public Object getObject() {
        if (isValue()) {
            return object;
        }
        throw new IllegalStateException();
    }

    //////////

    /**
     * @return 转换为布尔型
     */
    public boolean toBooleanValue() {
        if (isValue()) {
            return new BlurObject(object).toBooleanValue();
        }
        throw new IllegalStateException();
    }

    /**
     * @return 转换为字节
     */
    public byte toByteValue() {
        if (isValue()) {
            return new BlurObject(object).toByteValue();
        }
        throw new IllegalStateException();
    }

    /**
     * @return 转换为字节数组
     */
    public byte[] toBytesValue() {
        if (isValue()) {
            return new BlurObject(object).toBytesValue();
        }
        throw new IllegalStateException();
    }

    /**
     * @return 转换为字符型
     */
    public char toCharValue() {
        if (isValue()) {
            return new BlurObject(object).toCharValue();
        }
        throw new IllegalStateException();
    }

    /**
     * @return 转换为双精度浮点型
     */
    public double toDoubleValue() {
        if (isValue()) {
            return new BlurObject(object).toDoubleValue();
        }
        throw new IllegalStateException();
    }

    /**
     * @return 转换为浮点型
     */
    public float toFloatValue() {
        if (isValue()) {
            return new BlurObject(object).toFloatValue();
        }
        throw new IllegalStateException();
    }

    /**
     * @return 转换为整型
     */
    public int toIntValue() {
        if (isValue()) {
            return new BlurObject(object).toIntValue();
        }
        throw new IllegalStateException();
    }

    /**
     * @return 转换为长整型
     */
    public long toLongValue() {
        if (isValue()) {
            return new BlurObject(object).toLongValue();
        }
        throw new IllegalStateException();
    }

    /**
     * @return 转换为混合字符串
     */
    public String toMixStringValue() {
        if (isValue()) {
            return new BlurObject(object).toStringValue();
        }
        throw new IllegalStateException();
    }

    /**
     * @return 转换为短整型
     */
    public short toShortValue() {
        if (isValue()) {
            return new BlurObject(object).toShortValue();
        }
        throw new IllegalStateException();
    }

    /**
     * @return 转换为字符串
     */
    public String toStringValue() {
        return toMixStringValue();
    }

    /**
     * @return 转换为UTC时间毫秒数
     */
    public long toTimeValue() {
        if (isValue()) {
            return new BlurObject(object).toLongValue();
        }
        throw new IllegalStateException();
    }

    //////////

    public TreeObject get(int index) {
        if (isList()) {
            List<TreeObject> list = ((List<TreeObject>) object);
            if (list != null && list.size() > 0 && index >= 0 && index < list.size()) {
                return list.get(index);
            }
            return null;
        }
        throw new IllegalStateException();
    }

    public TreeObject get(int index, TreeObject defaultValue) {
        if (isList()) {
            List<TreeObject> list = ((List<TreeObject>) object);
            if (list != null && list.size() > 0 && index >= 0 && index < list.size()) {
                return list.get(index);
            }
            return defaultValue;
        }
        throw new IllegalStateException();
    }

    public TreeObject get(String key) {
        if (isMap()) {
            Map<String, TreeObject> map = (Map<String, TreeObject>) object;
            if (StringUtils.isNotBlank(key) && map != null && map.size() > 0) {
                return map.get(key);
            }
            return null;
        }
        throw new IllegalStateException();
    }

    public TreeObject get(String key, TreeObject defaultValue) {
        if (isMap()) {
            Map<String, TreeObject> map = (Map<String, TreeObject>) object;
            if (StringUtils.isNotBlank(key) && map != null && map.size() > 0) {
                return map.get(key);
            }
            return defaultValue;
        }
        throw new IllegalStateException();
    }

    public boolean getBoolean(int index) {
        return getBoolean(index, false);
    }

    public boolean getBoolean(int index, boolean defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toBooleanValue();
        }
        return defaultValue;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toBooleanValue();
        }
        return defaultValue;
    }

    public byte getByte(int index) {
        return getByte(index, (byte) 0);
    }

    public byte getByte(int index, byte defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toByteValue();
        }
        return defaultValue;
    }

    public byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    public byte getByte(String key, byte defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toByteValue();
        }
        return defaultValue;
    }

    public byte[] getBytes(int index) {
        return getBytes(index, null);
    }

    public byte[] getBytes(int index, byte[] defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toBytesValue();
        }
        return defaultValue;
    }

    public byte[] getBytes(String key) {
        return getBytes(key, null);
    }

    public byte[] getBytes(String key, byte[] defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toBytesValue();
        }
        return defaultValue;
    }

    public char getChar(int index) {
        return getChar(index, (char) 0);
    }

    public char getChar(int index, char defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toCharValue();
        }
        return defaultValue;
    }

    public char getChar(String key) {
        return getChar(key, (char) 0);
    }

    public char getChar(String key, char defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toCharValue();
        }
        return defaultValue;
    }

    public double getDouble(int index) {
        return getDouble(index, 0d);
    }

    public double getDouble(int index, double defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toDoubleValue();
        }
        return defaultValue;
    }

    public double getDouble(String key) {
        return getDouble(key, 0d);
    }

    public double getDouble(String key, double defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toDoubleValue();
        }
        return defaultValue;
    }

    public float getFloat(int index) {
        return getFloat(index, 0f);
    }

    public float getFloat(int index, float defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toFloatValue();
        }
        return defaultValue;
    }

    public float getFloat(String key) {
        return getFloat(key, 0f);
    }

    public float getFloat(String key, float defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toFloatValue();
        }
        return defaultValue;
    }

    public int getInt(int index) {
        return getInt(index, 0);
    }

    public int getInt(int index, int defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toIntValue();
        }
        return defaultValue;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toIntValue();
        }
        return defaultValue;
    }

    public long getLong(int index) {
        return getLong(index, 0L);
    }

    public long getLong(int index, long defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toLongValue();
        }
        return defaultValue;
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }

    public long getLong(String key, long defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toLongValue();
        }
        return defaultValue;
    }

    public String getMixString(int index) {
        return getMixString(index, null);
    }

    public String getMixString(int index, String defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toMixStringValue();
        }
        return defaultValue;
    }

    public String getMixString(String key) {
        return getMixString(key, null);
    }

    public String getMixString(String key, String defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toMixStringValue();
        }
        return defaultValue;
    }

    public short getShort(int index) {
        return getShort(index, (short) 0);
    }

    public short getShort(int index, short defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toShortValue();
        }
        return defaultValue;
    }

    public short getShort(String key) {
        return getShort(key, (short) 0);
    }

    public short getShort(String key, short defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toShortValue();
        }
        return defaultValue;
    }

    public String getString(int index) {
        return getString(index, null);
    }

    public String getString(int index, String defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toStringValue();
        }
        return defaultValue;
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toStringValue();
        }
        return defaultValue;
    }

    public long getTime(int index) {
        return getTime(index, 0);
    }

    public long getTime(int index, long defaultValue) {
        TreeObject tObj = get(index);
        if (tObj != null) {
            return tObj.toTimeValue();
        }
        return defaultValue;
    }

    public long getTime(String key) {
        return getTime(key, 0);
    }

    public long getTime(String key, long defaultValue) {
        TreeObject tObj = get(key);
        if (tObj != null) {
            return tObj.toTimeValue();
        }
        return defaultValue;
    }
}
