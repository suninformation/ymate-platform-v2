/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.core.lang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * TreeObject
 * </p>
 * <p>
 * 树对象，使用级联方式存储各种数据类型，不限层级深度；
 * </p>
 *
 * @author 刘镇 (suninformation@163.com)
 * @version 0.0.0
 *          <table style="border:1px solid gray;">
 *          <tr>
 *          <th width="100px">版本号</th><th width="100px">动作</th><th
 *          width="100px">修改人</th><th width="100px">修改时间</th>
 *          </tr>
 *          <!-- 以 Table 方式书写修改历史 -->
 *          <tr>
 *          <td>0.0.0</td>
 *          <td>创建类</td>
 *          <td>刘镇</td>
 *          <td>2010-3-27下午10:32:09</td>
 *          </tr>
 *          </table>
 */
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
    private int _mode = MODE_VALUE;

    /**
     * 当前TreeObject对象数据类型
     */
    private int _type = TYPE_NULL;

    /**
     * 当前TreeObject对象存储的值对象
     */
    private Object _object;

    /**
     * @param o 目标对象
     * @return 检测目标对象的数据类型并返回类型常量值
     */
    private static final int __checkType(Object o) {
        if (o == null) {
            return TYPE_NULL;
        }
        Class<? extends Object> _class = o.getClass();
        int _returnValue = TYPE_OBJECT;
        if (o instanceof Integer || int.class.isAssignableFrom(_class)) {
            _returnValue = TYPE_INTEGER;
        } else if (o instanceof Long || long.class.isAssignableFrom(_class)) {
            _returnValue = TYPE_LONG;
        } else if (o instanceof String) {
            // String和MixString统一采用String存储
            _returnValue = TYPE_MIX_STRING;
        } else if (o instanceof Boolean || boolean.class.isAssignableFrom(_class)) {
            _returnValue = TYPE_BOOLEAN;
        } else if (o instanceof Float || float.class.isAssignableFrom(_class)) {
            _returnValue = TYPE_FLOAT;
        } else if (o instanceof Double || double.class.isAssignableFrom(_class)) {
            _returnValue = TYPE_DOUBLE;
        } else if (o instanceof Byte || byte.class.isAssignableFrom(_class)) {
            _returnValue = TYPE_BYTE;
        } else if (o instanceof Character || char.class.isAssignableFrom(_class)) {
            _returnValue = TYPE_CHAR;
        } else if (o instanceof Short || short.class.isAssignableFrom(_class)) {
            _returnValue = TYPE_SHORT;
        } else if (o instanceof byte[] || o instanceof Byte[]
                || byte[].class.isAssignableFrom(_class)
                || Byte[].class.isAssignableFrom(_class)) {
            _returnValue = TYPE_BYTES;
        } else if (o instanceof Map) {
            _returnValue = TYPE_MAP;
        } else if (o instanceof Collection) {
            _returnValue = TYPE_COLLECTION;
        } else if (o instanceof TreeObject) {
            _returnValue = TYPE_TREE_OBJECT;
        }
        return _returnValue;
    }

    //////////

    public static final TreeObject fromJson(String jsonStr) {
        return fromJson(JSON.parseObject(jsonStr));
    }

    public static final TreeObject fromJson(JSONObject json) {
        if (json == null) {
            throw new NullArgumentException("json");
        }
        if (!json.containsKey(KEY_CLASS)) {
            throw new IllegalArgumentException();
        }
        int _class = json.getIntValue(KEY_CLASS);
        TreeObject _target = new TreeObject();
        if (_class == TYPE_MAP) { // MAP
            JSONObject _value = json.getJSONObject(KEY_VALUE);
            Iterator<String> _valueIt = _value.keySet().iterator();
            while (_valueIt.hasNext()) {
                String _key = _valueIt.next();
                _target.put(_key, fromJson(_value.getJSONObject(_key)));
            }
        } else if (_class == TYPE_COLLECTION) { // COLLECTION
            JSONArray _value = json.getJSONArray(KEY_VALUE);
            for (int _idx = 0; _idx < _value.size(); _idx++) {
                _target.add(fromJson(_value.getJSONObject(_idx)));
            }
        } else { // VALUE
            Object _value = json.get(KEY_VALUE);
            if (_class == TYPE_MIX_STRING) {
                _value = new String(Base64.decodeBase64((String) _value));
            } else if (_class == TYPE_BYTES) {
                _value = Base64.decodeBase64((String) _value);
            }
            _target = new TreeObject(_value, _class);
        }
        return _target;
    }

    public JSONObject toJson() {
        return toJson(this);
    }

    public static final JSONObject toJson(TreeObject tObject) {
        if (tObject == null) {
            return null;
        }
        JSONObject _returnJson;
        if (tObject.isMap()) { // MAP
            TreeObject _itemTObject;
            Map<String, TreeObject> _nodeValue = tObject.getMap();
            if (_nodeValue != null && !_nodeValue.isEmpty()) {
                JSONObject _itemJson = new JSONObject();
                for (String _nodeAttrKey : _nodeValue.keySet()) {
                    if (StringUtils.isNotBlank(_nodeAttrKey)) {
                        _itemTObject = _nodeValue.get(_nodeAttrKey);
                        if (_itemTObject != null) {
                            _returnJson = toJson(_itemTObject);
                            if (_returnJson != null) {
                                if (!_returnJson.containsKey(KEY_VALUE)) {
                                    JSONObject _nodeAttrValue = new JSONObject();
                                    _nodeAttrValue.put(KEY_VALUE, _returnJson);
                                    _nodeAttrValue.put(KEY_CLASS, TYPE_MAP);
                                    _itemJson.put(_nodeAttrKey, _nodeAttrValue);
                                    continue;
                                }
                                _itemJson.put(_nodeAttrKey, _returnJson);
                            }
                        }
                    }
                }
                // 处理值为map类型时没有_v的情况，保证结构为{_v:{},_c:9}
                if (!_itemJson.containsKey(KEY_VALUE)) {
                    JSONObject _nodeAttrValue = new JSONObject();
                    _nodeAttrValue.put(KEY_CLASS, TYPE_MAP);
                    _nodeAttrValue.put(KEY_VALUE, _itemJson);
                    return _nodeAttrValue;
                } else {
                    return _itemJson;
                }
            } else {
                JSONObject _itemJson = new JSONObject();
                _itemJson.put(KEY_CLASS, TYPE_MAP);
                _itemJson.put(KEY_VALUE, new JSONObject());
                return _itemJson;
            }
        } else if (tObject.isList()) { // ARRAY
            List<TreeObject> _nodeValue = tObject.getList();
            if (_nodeValue != null && !_nodeValue.isEmpty()) {
                JSONArray _itemJson = new JSONArray();
                for (TreeObject _itemTObject : _nodeValue) {
                    if (_itemTObject != null) {
                        _returnJson = toJson(_itemTObject);
                        if (_returnJson != null) {
                            _itemJson.add(_returnJson);
                        }
                    }
                }
                // 保证数组的格式:{_value:[{_class:,_value},{}..]}
                JSONObject _nodeJson = new JSONObject();
                _nodeJson.put(KEY_VALUE, _itemJson);
                _nodeJson.put(KEY_CLASS, TYPE_COLLECTION);
                return _nodeJson;
            } else {
                JSONObject _nodeJson = new JSONObject();
                _nodeJson.put(KEY_VALUE, new JSONArray());
                _nodeJson.put(KEY_CLASS, TYPE_COLLECTION);
                return _nodeJson;
            }
        } else { // VALUE
            JSONObject _nodeJson = new JSONObject();
            _nodeJson.put(KEY_CLASS, tObject.getType());
            //
            if (tObject.getType() == TYPE_MIX_STRING) {
                // 混淆(Mix)类型编码为Base64
                if (tObject.getObject() != null) {
                    String _bStr = Base64.encodeBase64String(tObject.toMixStringValue().getBytes());
                    _nodeJson.put(KEY_VALUE, _bStr);
                }
            } else if (tObject.getType() == TYPE_BYTES) {
                if (tObject.getObject() instanceof byte[]) {
                    String _bytes = String.valueOf(Base64.encodeBase64String(tObject.toBytesValue()));
                    _nodeJson.put(KEY_VALUE, _bytes);
                }
            } else if (tObject.getType() == TYPE_TIME) {
                _nodeJson.put(KEY_VALUE, tObject.toTimeValue());
            } else {
                _nodeJson.put(KEY_VALUE, tObject.getObject());
            }
            return _nodeJson;
        }
    }

    //////////

    public static final TreeObject fromXml(String xml) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String toXml() {
        return toXml(this);
    }

    public static final String toXml(TreeObject tObject) {
        // TODO
        throw new UnsupportedOperationException();
    }

    //////////

    /**
     * 构造器
     */
    public TreeObject() {
    }

    /**
     * 构造器
     *
     * @param bool
     */
    public TreeObject(boolean bool) {
        _object = bool;
        _type = TYPE_BOOLEAN;
    }

    /**
     * 构造器
     *
     * @param bool
     */
    public TreeObject(Boolean bool) {
        _object = bool != null ? bool.booleanValue() : false;
        _type = TYPE_BOOLEAN;
    }

    /**
     * 构造器
     *
     * @param b
     */
    public TreeObject(byte b) {
        _object = b;
        _type = TYPE_BYTE;
    }

    /**
     * 构造器
     *
     * @param b
     */
    public TreeObject(Byte b) {
        _object = b != null ? b.byteValue() : Byte.MIN_VALUE;
        _type = TYPE_BYTE;
    }

    /**
     * 构造器
     *
     * @param bytes
     */
    public TreeObject(byte[] bytes) {
        _object = bytes;
        _type = TYPE_BYTES;
    }

    /**
     * 构造器
     *
     * @param bytes
     */
    public TreeObject(Byte[] bytes) {
        _object = bytes;
        _type = TYPE_BYTES;
    }

    /**
     * 构造器
     *
     * @param c
     */
    public TreeObject(char c) {
        _object = c;
        _type = TYPE_CHAR;
    }

    /**
     * 构造器
     *
     * @param c
     */
    public TreeObject(Character c) {
        _object = c != null ? c.charValue() : Character.MIN_VALUE;
        _type = TYPE_CHAR;
    }

    /**
     * 构造器
     *
     * @param c
     */
    public TreeObject(Collection<? extends Object> c) {
        _object = c;
        _type = TYPE_COLLECTION;
    }

    /**
     * 构造器
     *
     * @param d
     */
    public TreeObject(double d) {
        _object = d;
        _type = TYPE_DOUBLE;
    }

    /**
     * 构造器
     *
     * @param d
     */
    public TreeObject(Double d) {
        _object = d != null ? d.doubleValue() : Double.MIN_VALUE;
        _type = TYPE_DOUBLE;
    }

    /**
     * 构造器
     *
     * @param f
     */
    public TreeObject(float f) {
        _object = f;
        _type = TYPE_FLOAT;
    }

    /**
     * 构造器
     *
     * @param f
     */
    public TreeObject(Float f) {
        _object = f != null ? f.floatValue() : Float.MIN_VALUE;
        _type = TYPE_FLOAT;
    }

    /**
     * 构造器
     *
     * @param i
     */
    public TreeObject(int i) {
        _object = i;
        _type = TYPE_INTEGER;
    }

    /**
     * 构造器
     *
     * @param i
     */
    public TreeObject(Integer i) {
        _object = i != null ? i.intValue() : Integer.MIN_VALUE;
        _type = TYPE_INTEGER;
    }

    /**
     * 构造器
     *
     * @param l
     */
    public TreeObject(long l) {
        _object = l;
        _type = TYPE_LONG;
    }

    /**
     * 构造器
     *
     * @param t
     * @param isTime 是否时间类型，如果是时间类型，则存储的是时间的UTC时间毫秒值
     */
    public TreeObject(long t, boolean isTime) {
        _object = t;
        _type = isTime ? TYPE_TIME : TYPE_LONG;
    }

    /**
     * 构造器
     *
     * @param l
     */
    public TreeObject(Long l) {
        _object = l != null ? l.longValue() : Long.MIN_VALUE;
        _type = TYPE_LONG;
    }

    /**
     * 构造器
     *
     * @param t
     * @param isTime
     */
    public TreeObject(Long t, boolean isTime) {
        _object = t != null ? t.longValue() : Long.MIN_VALUE;
        _type = isTime ? TYPE_TIME : TYPE_LONG;
    }

    /**
     * 构造器
     *
     * @param m
     */
    public TreeObject(Map<? extends Object, ? extends Object> m) {
        _object = m;
        _type = TYPE_MAP;
    }

    /**
     * 构造器
     *
     * @param s
     */
    public TreeObject(short s) {
        _object = s;
        _type = TYPE_SHORT;
    }

    /**
     * 构造器
     *
     * @param s
     */
    public TreeObject(Short s) {
        _object = s != null ? s.shortValue() : Short.MIN_VALUE;
        _type = TYPE_SHORT;
    }

    /**
     * 构造器
     *
     * @param s
     */
    public TreeObject(String s) {
        _object = s;
        _type = TYPE_STRING;
    }

    /**
     * 构造器
     *
     * @param s     需要存储的字符串
     * @param isMix 是否混合字符串，如果是混合字符串，那么type类型为MIX_STRING_TYPE，存储的内部对象还是原始的s
     */
    public TreeObject(String s, boolean isMix) {
        _object = s;
        _type = isMix ? TYPE_MIX_STRING : TYPE_STRING;
    }

    /**
     * 构造器
     *
     * @param tObject
     */
    public TreeObject(TreeObject tObject) {
        if (tObject != null) {
            _object = tObject._object;
            _type = tObject._type;
            _mode = tObject._mode;
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
            _type = TYPE_NULL;
            _object = null;
            return;
        }
        _object = o;
        //
        if (o instanceof Integer) {
            _type = TYPE_INTEGER;
            _object = (Integer) o;
        } else if (int.class.isAssignableFrom(o.getClass())) {
            _type = TYPE_INTEGER;
        } else if (o instanceof String) {
            _type = TYPE_MIX_STRING;
        } else if (o instanceof Long) {
            _type = TYPE_LONG;
            _object = (Long) o;
        } else if (long.class.isAssignableFrom(o.getClass())) {
            _type = TYPE_LONG;
        } else if (o instanceof Boolean) {
            _type = TYPE_BOOLEAN;
            _object = (Boolean) o;
        } else if (boolean.class.isAssignableFrom(o.getClass())) {
            _type = TYPE_BOOLEAN;
        } else if (o instanceof Float) {
            _type = TYPE_FLOAT;
            _object = (Float) o;
        } else if (float.class.isAssignableFrom(o.getClass())) {
            _type = TYPE_FLOAT;
        } else if (o instanceof Double) {
            _type = TYPE_DOUBLE;
            _object = (Double) o;
        } else if (double.class.isAssignableFrom(o.getClass())) {
            _type = TYPE_DOUBLE;
        } else if (o instanceof Map) {
            _type = TYPE_MAP;
        } else if (o instanceof Collection) {
            _type = TYPE_COLLECTION;
        } else if (o instanceof Byte) {
            _type = TYPE_BYTE;
            _object = (Byte) o;
        } else if (byte.class.isAssignableFrom(o.getClass())) {
            _type = TYPE_BYTE;
        } else if (o instanceof Character) {
            _type = TYPE_CHAR;
            _object = (Character) o;
        } else if (char.class.isAssignableFrom(o.getClass())) {
            _type = TYPE_CHAR;
        } else if (o instanceof Short) {
            _type = TYPE_SHORT;
            _object = (Short) o;
        } else if (short.class.isAssignableFrom(o.getClass())) {
            _type = TYPE_SHORT;
        } else if (o instanceof byte[]) {
            _type = TYPE_BYTES;
        } else if (o instanceof Byte[]) {
            _type = TYPE_BYTES;
        } else if (o instanceof TreeObject) {
            TreeObject tObject = (TreeObject) o;
            _object = tObject._object;
            this._type = tObject._type;
            _mode = tObject._mode;
        } else {
            this._type = TYPE_OBJECT;
        }
    }

    /**
     * 构造器
     *
     * @param o    简单值类型对象
     * @param type 需要输入的对象类型，是检查类型，输入的o如果不是此类型，则尝试转换，如果无法转换，则对应的o为type类型的对应无效值
     */
    public TreeObject(Object o, int type) {
        _object = o;
        _type = type;
        switch (type) {
            case TYPE_NULL:
                _object = null;
                break;
            case TYPE_INTEGER:
                _object = new BlurObject(o).toIntValue();
                break;
            case TYPE_MIX_STRING:
                _object = new BlurObject(o).toStringValue();
                break;
            case TYPE_LONG:
                _object = new BlurObject(o).toLongValue();
                break;
            case TYPE_STRING:
                _object = new BlurObject(o).toStringValue();
                break;
            case TYPE_TIME:
                _object = new BlurObject(o).toLongValue();
                break;
            case TYPE_BOOLEAN:
                _object = new BlurObject(o).toBooleanValue();
                break;
            case TYPE_FLOAT:
                _object = new BlurObject(o).toFloatValue();
                break;
            case TYPE_DOUBLE:
                _object = new BlurObject(o).toDoubleValue();
                break;
            case TYPE_MAP:
                break;
            case TYPE_COLLECTION:
                break;
            case TYPE_BYTE:
                _object = new BlurObject(o).toByteValue();
                break;
            case TYPE_CHAR:
                _object = new BlurObject(o).toCharValue();
                break;
            case TYPE_SHORT:
                _object = new BlurObject(o).toShortValue();
                break;
            case TYPE_BYTES:
                _object = new BlurObject(o).toBytesValue();
                break;
            case TYPE_OBJECT: {
                break;
            }
            case TYPE_UNKNOWN:
                break;
            case TYPE_TREE_OBJECT:
                if (o instanceof TreeObject) {
                    TreeObject tObject = (TreeObject) o;
                    _object = tObject._object;
                    this._type = tObject._type;
                    _mode = tObject._mode;
                } else {
                    _type = TYPE_OBJECT;
                }
                break;
            default:
        }
    }

    ////////

    /**
     * 添加元素 - 布尔型
     *
     * @param b
     */
    public TreeObject add(boolean b) {
        return add(b, TYPE_BOOLEAN);
    }

    /**
     * 添加元素 - 布尔型
     *
     * @param b
     */
    public TreeObject add(Boolean b) {
        return add(b != null ? b.booleanValue() : false, TYPE_BOOLEAN);
    }

    /**
     * 添加元素 - byte字节
     *
     * @param b
     */
    public TreeObject add(byte b) {
        return add(b, TYPE_BYTE);
    }

    /**
     * 添加元素 - Byte型
     *
     * @param b
     */
    public TreeObject add(Byte b) {
        return add(b != null ? b.byteValue() : 0, TYPE_BYTE);
    }

    /**
     * 添加元素 - byte字节数组
     *
     * @param bytes
     */
    public TreeObject add(byte[] bytes) {
        return add(bytes, TYPE_BYTES);
    }

    /**
     * 添加元素 - Byte型数组
     *
     * @param bytes
     */
    public TreeObject add(Byte[] bytes) {
        return add(bytes, TYPE_BYTES);
    }

    /**
     * 添加元素 - char字符
     *
     * @param c
     */
    public TreeObject add(char c) {
        return add(c, TYPE_CHAR);
    }

    /**
     * 添加元素 - Char型
     *
     * @param c
     */
    public TreeObject add(Character c) {
        return add(c != null ? c.charValue() : Character.MIN_CODE_POINT, TYPE_CHAR);
    }

    /**
     * 添加元素 - double双精度
     *
     * @param d
     */
    public TreeObject add(double d) {
        return add(d, TYPE_DOUBLE);
    }

    /**
     * 添加元素 - Double型
     *
     * @param d
     */
    public TreeObject add(Double d) {
        return add(d != null ? d.doubleValue() : 0d, TYPE_DOUBLE);
    }

    /**
     * 添加元素 - float浮点
     *
     * @param f
     */
    public TreeObject add(float f) {
        return add(f, TYPE_FLOAT);
    }

    /**
     * 添加元素 - Float型
     *
     * @param f
     */
    public TreeObject add(Float f) {
        return add(f != null ? f.floatValue() : 0f, TYPE_FLOAT);
    }

    /**
     * 添加元素 - int整数
     *
     * @param i
     */
    public TreeObject add(int i) {
        return add(i, TYPE_INTEGER);
    }

    /**
     * 添加元素 - Integer型
     *
     * @param i
     */
    public TreeObject add(Integer i) {
        return add(i != null ? i.intValue() : 0, TYPE_INTEGER);
    }

    /**
     * 添加元素 - long长整数
     *
     * @param l
     */
    public TreeObject add(long l) {
        return add(l, TYPE_LONG);
    }

    /**
     * 添加元素 - long或utc时间
     *
     * @param t
     * @param isTime
     */
    public TreeObject add(long t, boolean isTime) {
        return add(t, isTime ? TYPE_TIME : TYPE_LONG);
    }

    /**
     * 添加元素 - Long型
     *
     * @param l
     */
    public TreeObject add(Long l) {
        return add(l != null ? l.longValue() : 0l, TYPE_LONG);
    }

    /**
     * 添加元素 - Long型或utc时间
     *
     * @param t
     * @param isTime
     */
    public TreeObject add(Long t, boolean isTime) {
        return add(t != null ? t.longValue() : 0l, isTime ? TYPE_TIME : TYPE_LONG);
    }

    /**
     * 添加元素 - Object对象
     *
     * @param o
     */
    public TreeObject add(Object o) {
        return add(o, __checkType(o));
    }

    /**
     * 添加元素 - Object对象并指定type类型
     *
     * @param o
     * @param type
     */
    public TreeObject add(Object o, int type) {
        return add(new TreeObject(o, type));
    }

    /**
     * 添加元素 - short短整型
     *
     * @param s
     */
    public TreeObject add(short s) {
        return add(s, TYPE_SHORT);
    }

    /**
     * 添加元素 - Short型
     *
     * @param s
     */
    public TreeObject add(Short s) {
        return add(s != null ? s.shortValue() : 0, TYPE_SHORT);
    }

    /**
     * 添加元素 - String字符串
     *
     * @param s
     */
    public TreeObject add(String s) {
        return add(s, TYPE_STRING);
    }

    /**
     * 添加元素 - String字符串并指定是否混淆
     *
     * @param s
     * @param isMix
     */
    public TreeObject add(String s, boolean isMix) {
        return add(s, isMix ? TYPE_MIX_STRING : TYPE_STRING);
    }

    /**
     * 添加元素 - TreeObject树对象
     *
     * @param tObject
     */
    public TreeObject add(TreeObject tObject) {
        if (tObject != null) {
            if (_mode != MODE_MAP) {
                if (_mode == MODE_VALUE) {
                    _type = TYPE_TREE_OBJECT;
                    _mode = MODE_ARRAY;
                }
                if (_object == null) {
                    // 创建一个线程安全的集合
                    _object = new CopyOnWriteArrayList<TreeObject>();
                }
                ((List<TreeObject>) _object).add(tObject);
            } else {
                throw new IllegalStateException();
            }
        }
        return this;
    }

    //////////

    /**
     * @param k
     * @param b
     */
    public TreeObject put(String k, boolean b) {
        return put(k, b, TYPE_BOOLEAN);
    }

    /**
     * @param k
     * @param b
     */
    public TreeObject put(String k, Boolean b) {
        return put(k, b, TYPE_BOOLEAN);
    }

    /**
     * @param k
     * @param b
     */
    public TreeObject put(String k, byte b) {
        return put(k, b, TYPE_BYTE);
    }

    /**
     * @param k
     * @param b
     */
    public TreeObject put(String k, Byte b) {
        return put(k, b, TYPE_BYTE);
    }

    /**
     * @param k
     * @param bytes
     */
    public TreeObject put(String k, byte[] bytes) {
        return put(k, bytes, TYPE_BYTES);
    }

    /**
     * @param k
     * @param bytes
     */
    public TreeObject put(String k, Byte[] bytes) {
        return put(k, bytes, TYPE_BYTES);
    }

    /**
     * @param k
     * @param c
     */
    public TreeObject put(String k, char c) {
        return put(k, c, TYPE_CHAR);
    }

    /**
     * @param k
     * @param c
     */
    public TreeObject put(String k, Character c) {
        return put(k, c, TYPE_CHAR);
    }

    /**
     * @param k
     * @param d
     */
    public TreeObject put(String k, double d) {
        return put(k, d, TYPE_DOUBLE);
    }

    /**
     * @param k
     * @param d
     */
    public TreeObject put(String k, Double d) {
        return put(k, d, TYPE_DOUBLE);
    }

    /**
     * @param k
     * @param f
     */
    public TreeObject put(String k, float f) {
        return put(k, f, TYPE_FLOAT);
    }

    /**
     * @param k
     * @param f
     */
    public TreeObject put(String k, Float f) {
        return put(k, f, TYPE_FLOAT);
    }

    /**
     * @param k
     * @param i
     */
    public TreeObject put(String k, int i) {
        return put(k, i, TYPE_INTEGER);
    }

    /**
     * @param k
     * @param i
     */
    public TreeObject put(String k, Integer i) {
        return put(k, i, TYPE_INTEGER);
    }

    /**
     * @param k
     * @param l
     */
    public TreeObject put(String k, long l) {
        return put(k, l, TYPE_LONG);
    }

    /**
     * @param k
     * @param t
     * @param isTime
     */
    public TreeObject put(String k, long t, boolean isTime) {
        return put(k, t, isTime ? TYPE_TIME : TYPE_LONG);
    }

    /**
     * @param k
     * @param l
     */
    public TreeObject put(String k, Long l) {
        return put(k, l, TYPE_LONG);
    }

    /**
     * @param k
     * @param t
     * @param isTime
     */
    public TreeObject put(String k, Long t, boolean isTime) {
        return put(k, t, isTime ? TYPE_TIME : TYPE_LONG);
    }

    /**
     * @param k
     * @param o
     */
    public TreeObject put(String k, Object o) {
        return put(k, o, __checkType(o));
    }

    /**
     * @param k
     * @param o
     * @param type
     */
    public TreeObject put(String k, Object o, int type) {
        if (o != null) {
            put(k, new TreeObject(o, type));
        }
        return this;
    }

    /**
     * @param k
     * @param s
     */
    public TreeObject put(String k, short s) {
        return put(k, s, TYPE_SHORT);
    }

    /**
     * @param k
     * @param s
     */
    public TreeObject put(String k, Short s) {
        return put(k, s, TYPE_SHORT);
    }

    /**
     * @param k
     * @param s
     */
    public TreeObject put(String k, String s) {
        return put(k, s, TYPE_STRING);
    }

    /**
     * @param k
     * @param s
     * @param isMix
     */
    public TreeObject put(String k, String s, boolean isMix) {
        return put(k, s, isMix ? TYPE_MIX_STRING : TYPE_STRING);
    }

    /**
     * @param k
     * @param tObject
     */
    public TreeObject put(String k, TreeObject tObject) {
        if (StringUtils.isNotBlank(k) && tObject != null) {
            if (_mode != MODE_ARRAY) {
                if (_mode == MODE_VALUE) {
                    _type = TYPE_TREE_OBJECT;
                    _mode = MODE_MAP;
                }
                if (_object == null) {
                    // 创建一个线程安全的映射
                    _object = new ConcurrentHashMap<String, TreeObject>();
                }
                ((Map<String, TreeObject>) _object).put(k, tObject);
            } else {
                throw new IllegalStateException();
            }
        }
        return this;
    }

    //////////

    /**
     * 若存在指定序列的对象且对象不为空，则返回true
     *
     * @param index
     * @return
     */
    public boolean has(int index) {
        if (isMap() || isValue()) {
            throw new IllegalStateException();
        }
        if (index >= 0 && isList()) {
            List<TreeObject> _list = ((List<TreeObject>) _object);
            if (_list != null && _list.size() > 0 && index < _list.size() && _list.get(index) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 若当前为映射模式，并存在key对应的元素且此对象不为空，是返回true
     *
     * @param key
     * @return
     */
    public boolean has(String key) {
        if (isList() || isValue()) {
            throw new IllegalStateException();
        }
        if (StringUtils.isNotBlank(key) && isMap()) {
            Map<String, TreeObject> _map = ((Map<String, TreeObject>) _object);
            if (_map != null && _map.size() > 0 && _map.get(key) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否数组集合模式
     *
     * @return
     */
    public boolean isList() {
        return _mode == MODE_ARRAY;
    }

    /**
     * 是否映射模式
     *
     * @return
     */
    public boolean isMap() {
        return _mode == MODE_MAP;
    }

    /**
     * 是否值模式
     *
     * @return
     */
    public boolean isValue() {
        return _mode == MODE_VALUE;
    }

    public int getType() {
        return _type;
    }

    //////////

    /**
     * @return
     */
    public List<TreeObject> getList() {
        if (isList()) {
            List<TreeObject> _returnValue = new ArrayList<TreeObject>();
            List<TreeObject> _list = (List<TreeObject>) _object;
            if (_list != null && !_list.isEmpty()) {
                for (TreeObject _tObject : _list) {
                    if (_tObject != null) {
                        _returnValue.add(_tObject);
                    }
                }
            }
            return _returnValue;
        }
        throw new IllegalStateException();
    }

    /**
     * @return
     */
    public Map<String, TreeObject> getMap() {
        if (isMap()) {
            Map<String, TreeObject> _returnValue = new HashMap<String, TreeObject>();
            Map<String, TreeObject> _map = (Map<String, TreeObject>) _object;
            if (_map != null && !_map.isEmpty()) {
                for (String _k : _map.keySet()) {
                    if (StringUtils.isBlank(_k)) {
                        continue;
                    }
                    TreeObject _v = _map.get(_k);
                    if (_v != null) {
                        _returnValue.put(_k, _v);
                    }
                }
            }
            return _returnValue;
        }
        throw new IllegalStateException();
    }

    /**
     * @return
     */
    public Object getObject() {
        if (isValue()) {
            return _object;
        }
        throw new IllegalStateException();
    }

    //////////

    /**
     * 转换为布尔型
     *
     * @return
     */
    public boolean toBooleanValue() {
        if (isValue()) {
            if (_object == null) {
                return false;
            }
            if (_type == TYPE_BOOLEAN && (boolean.class.isAssignableFrom(_object.getClass()) || _object instanceof Boolean)) {
                return (Boolean) _object;
            } else {
                return new BlurObject(_object).toBooleanValue();
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 转换为字节
     *
     * @return
     */
    public byte toByteValue() {
        if (isValue()) {
            if (_object == null) {
                return 0;
            }
            if (_type == TYPE_BYTE && (byte.class.isAssignableFrom(_object.getClass()) || _object instanceof Byte)) {
                return (Byte) _object;
            } else {
                return new BlurObject(_object).toByteValue();
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 转换为字节数组
     *
     * @return
     */
    public byte[] toBytesValue() {
        if (isValue()) {
            if (_object == null) {
                return null;
            }
            if (_type == TYPE_BYTES
                    && (byte[].class.isAssignableFrom(_object.getClass())
                    || _object instanceof byte[]
                    || Byte[].class.isAssignableFrom(_object.getClass()) || _object instanceof Byte[])) {
                return (byte[]) _object;
            } else {
                return new BlurObject(_object).toBytesValue();
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 转换为字符型
     *
     * @return
     */
    public char toCharValue() {
        if (isValue()) {
            if (_object == null) {
                return Character.MIN_CODE_POINT;
            }
            if (_type == TYPE_CHAR
                    && (char.class.isAssignableFrom(_object.getClass()) || _object instanceof Character)) {
                return (Character) _object;
            } else {
                return new BlurObject(_object).toCharValue();
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 转换为双精度浮点型
     *
     * @return
     */
    public double toDoubleValue() {
        if (isValue()) {
            if (_object == null) {
                return 0d;
            }
            if (_type == TYPE_DOUBLE && (double.class.isAssignableFrom(_object.getClass()) || _object instanceof Double)) {
                return (Double) _object;
            } else {
                return new BlurObject(_object).toDoubleValue();
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 转换为浮点型
     *
     * @return
     */
    public float toFloatValue() {
        if (isValue()) {
            if (_object == null) {
                return 0f;
            }
            if (_type == TYPE_FLOAT && (float.class.isAssignableFrom(_object.getClass()) || _object instanceof Float)) {
                return (Float) _object;
            } else {
                return new BlurObject(_object).toFloatValue();
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 转换为整型
     *
     * @return
     */
    public int toIntValue() {
        if (isValue()) {
            if (_object == null) {
                return 0;
            }
            if (_type == TYPE_INTEGER && (int.class.isAssignableFrom(_object.getClass()) || _object instanceof Integer)) {
                return (Integer) _object;
            } else {
                return new BlurObject(_object).toIntValue();
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 转换为长整型
     *
     * @return
     */
    public long toLongValue() {
        if (isValue()) {
            if (_object == null) {
                return 0l;
            }
            if (_type == TYPE_LONG && (long.class.isAssignableFrom(_object.getClass()) || _object instanceof Long)) {
                return (Long) _object;
            } else {
                return new BlurObject(_object).toLongValue();
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 转换为混合字符串
     *
     * @return
     */
    public String toMixStringValue() {
        if (isValue()) {
            if (_object == null) {
                return null;
            }
            if ((_type == TYPE_MIX_STRING || _type == TYPE_STRING) && _object instanceof String) {
                return (String) _object;
            } else {
                return new BlurObject(_object).toStringValue();
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 转换为短整型
     *
     * @return
     */
    public short toShortValue() {
        if (isValue()) {
            if (_object == null) {
                return 0;
            }
            if (_type == TYPE_SHORT && (short.class.isAssignableFrom(_object.getClass()) || _object instanceof Short)) {
                return (Short) _object;
            } else {
                return new BlurObject(_object).toShortValue();
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 转换为字符串
     *
     * @return
     */
    public String toStringValue() {
        if (isValue()) {
            if (_object == null) {
                return null;
            }
            if ((_type == TYPE_MIX_STRING || _type == TYPE_STRING) && _object instanceof String) {
                return (String) _object;
            } else {
                return new BlurObject(_object).toStringValue();
            }
        }
        throw new IllegalStateException();
    }

    /**
     * 转换为UTC时间毫秒数
     *
     * @return
     */
    public long toTimeValue() {
        if (isValue()) {
            if (_object == null) {
                return 0l;
            }
            if ((_type == TYPE_TIME || _type == TYPE_LONG)
                    && (long.class.isAssignableFrom(_object.getClass()) || _object instanceof Long)) {
                return (Long) _object;
            } else {
                return new BlurObject(_object).toLongValue();
            }
        }
        throw new IllegalStateException();
    }

    //////////

    /**
     * @param index
     * @return
     */
    public TreeObject get(int index) {
        if (isList()) {
            List<TreeObject> _list = ((List<TreeObject>) _object);
            if (_list != null && _list.size() > 0 && index >= 0 && index < _list.size()) {
                return _list.get(index);
            }
            return null;
        }
        throw new IllegalStateException();
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public TreeObject get(int index, TreeObject defaultValue) {
        if (isList()) {
            List<TreeObject> _list = ((List<TreeObject>) _object);
            if (_list != null && _list.size() > 0 && index >= 0 && index < _list.size()) {
                return _list.get(index);
            }
            return defaultValue;
        }
        throw new IllegalStateException();
    }

    /**
     * @param key
     * @return
     */
    public TreeObject get(String key) {
        if (isMap()) {
            Map<String, TreeObject> _map = (Map<String, TreeObject>) _object;
            if (StringUtils.isNotBlank(key) && _map != null && _map.size() > 0) {
                return _map.get(key);
            }
            return null;
        }
        throw new IllegalStateException();
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public TreeObject get(String key, TreeObject defaultValue) {
        if (isMap()) {
            Map<String, TreeObject> _map = (Map<String, TreeObject>) _object;
            if (StringUtils.isNotBlank(key) && _map != null && _map.size() > 0) {
                return _map.get(key);
            }
            return defaultValue;
        }
        throw new IllegalStateException();
    }

    /**
     * @param index
     * @return
     */
    public boolean getBoolean(int index) {
        return getBoolean(index, false);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public boolean getBoolean(int index, boolean defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toBooleanValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toBooleanValue();
        }
        return defaultValue;
    }

    /**
     * @param index
     * @return
     */
    public byte getByte(int index) {
        return getByte(index, (byte) 0);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public byte getByte(int index, byte defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toByteValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public byte getByte(String key, byte defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toByteValue();
        }
        return defaultValue;
    }

    /**
     * @param index
     * @return
     */
    public byte[] getBytes(int index) {
        return getBytes(index, null);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public byte[] getBytes(int index, byte[] defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toBytesValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public byte[] getBytes(String key) {
        return getBytes(key, null);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public byte[] getBytes(String key, byte[] defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toBytesValue();
        }
        return defaultValue;
    }

    /**
     * @param index
     * @return
     */
    public char getChar(int index) {
        return getChar(index, (char) 0);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public char getChar(int index, char defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toCharValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public char getChar(String key) {
        return getChar(key, (char) 0);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public char getChar(String key, char defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toCharValue();
        }
        return defaultValue;
    }

    /**
     * @param index
     * @return
     */
    public double getDouble(int index) {
        return getDouble(index, 0d);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public double getDouble(int index, double defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toDoubleValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public double getDouble(String key) {
        return getDouble(key, 0d);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public double getDouble(String key, double defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toDoubleValue();
        }
        return defaultValue;
    }

    /**
     * @param index
     * @return
     */
    public float getFloat(int index) {
        return getFloat(index, 0f);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public float getFloat(int index, float defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toFloatValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public float getFloat(String key) {
        return getFloat(key, 0f);
    }

    public float getFloat(String key, float defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toFloatValue();
        }
        return defaultValue;
    }

    /**
     * @param index
     * @return
     */
    public int getInt(int index) {
        return getInt(index, 0);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public int getInt(int index, int defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toIntValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public int getInt(String key, int defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toIntValue();
        }
        return defaultValue;
    }

    /**
     * @param index
     * @return
     */
    public long getLong(int index) {
        return getLong(index, 0l);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public long getLong(int index, long defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toLongValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public long getLong(String key) {
        return getLong(key, 0l);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public long getLong(String key, long defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toLongValue();
        }
        return defaultValue;
    }

    /**
     * @param index
     * @return
     */
    public String getMixString(int index) {
        return getMixString(index, null);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public String getMixString(int index, String defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toMixStringValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public String getMixString(String key) {
        return getMixString(key, null);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public String getMixString(String key, String defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toMixStringValue();
        }
        return defaultValue;
    }

    /**
     * @param index
     * @return
     */
    public short getShort(int index) {
        return getShort(index, (short) 0);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public short getShort(int index, short defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toShortValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public short getShort(String key) {
        return getShort(key, (short) 0);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public short getShort(String key, short defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toShortValue();
        }
        return defaultValue;
    }

    /**
     * @param index
     * @return
     */
    public String getString(int index) {
        return getString(index, null);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public String getString(int index, String defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toStringValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(String key, String defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toStringValue();
        }
        return defaultValue;
    }

    /**
     * @param index
     * @return
     */
    public long getTime(int index) {
        return getTime(index, 0l);
    }

    /**
     * @param index
     * @param defaultValue
     * @return
     */
    public long getTime(int index, long defaultValue) {
        TreeObject _tObj = get(index);
        if (_tObj != null) {
            return _tObj.toTimeValue();
        }
        return defaultValue;
    }

    /**
     * @param key
     * @return
     */
    public long getTime(String key) {
        return getTime(key, 0l);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public long getTime(String key, long defaultValue) {
        TreeObject _tObj = get(key);
        if (_tObj != null) {
            return _tObj.toTimeValue();
        }
        return defaultValue;
    }

}
