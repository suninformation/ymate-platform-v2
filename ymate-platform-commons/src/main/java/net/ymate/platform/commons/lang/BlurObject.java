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

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.annotation.Converter;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模糊对象，任意数据类型间转换
 *
 * @author 刘镇 (suninformation@163.com) on 2010-4-16 下午11:51:39
 */
public class BlurObject implements Serializable, Cloneable {

    private static final Log LOG = LogFactory.getLog(BlurObject.class);

    private static final long serialVersionUID = 4141840934670622411L;

    private static final Map<Class<?>, Map<Class<?>, IConverter<?>>> CONVERTERS = new ConcurrentHashMap<>();

    static {
        try {
            ClassUtils.ExtensionLoader<IConverter> extensionLoader = ClassUtils.getExtensionLoader(IConverter.class);
            for (Class<IConverter> converter : extensionLoader.getExtensionClasses()) {
                Converter converterAnn = converter.getAnnotation(Converter.class);
                if (converterAnn != null) {
                    IConverter converterInst = converter.newInstance();
                    for (Class<?> from : converterAnn.from()) {
                        if (!from.equals(converterAnn.to())) {
                            registerConverter(from, converterAnn.to(), converterInst);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    /**
     * 注册类型转换器
     *
     * @param fromClass 原类型
     * @param toClass   目标类型
     * @param converter 类型转换器实例
     * @throws Exception 可能产生的任何异常
     */
    public static void registerConverter(Class<?> fromClass, Class<?> toClass, IConverter<?> converter) throws Exception {
        Map<Class<?>, IConverter<?>> map = ReentrantLockHelper.putIfAbsentAsync(CONVERTERS, toClass, () -> new HashMap<>(16));
        map.put(fromClass, converter);
    }

    /**
     * 当前存储对象值
     */
    private final Object attr;

    public static BlurObject bind(Object o) {
        return new BlurObject(o);
    }

    public BlurObject(Object o) {
        attr = o;
    }

    /**
     * @return 输出对象
     */
    public Object toObjectValue() {
        return attr;
    }

    /**
     * @return 输出模糊对象
     */
    public BlurObject toBlurObjectValue() {
        if (attr instanceof BlurObject) {
            return (BlurObject) attr;
        }
        return this;
    }

    /**
     * @return 输出为映射
     */
    public Map<?, ?> toMapValue() {
        if (attr == null) {
            return null;
        }
        if (attr instanceof Map) {
            return (Map<?, ?>) attr;
        }
        return Collections.emptyMap();
    }

    /**
     * @return 输出为列表
     */
    public List<?> toListValue() {
        if (attr == null) {
            return null;
        }
        if (attr instanceof List) {
            return (List<?>) attr;
        }
        List<Object> returnValue = new ArrayList<>();
        returnValue.add(attr);
        return returnValue;
    }

    /**
     * @return 输出为集合
     */
    public Set<?> toSetValue() {
        if (attr == null) {
            return null;
        }
        if (attr instanceof List) {
            return (Set<?>) attr;
        }
        Set<Object> returnValue = new HashSet<>();
        returnValue.add(attr);
        return returnValue;
    }

    /**
     * @return 输出布尔值，如果当前类型非布尔值，那么尝试转换
     */
    public boolean toBooleanValue() {
        if (attr == null) {
            return false;
        }
        if (attr instanceof String) {
            return "true".equalsIgnoreCase(this.attr.toString()) || "on".equalsIgnoreCase(this.attr.toString()) || "1".equalsIgnoreCase(this.attr.toString());
        }
        if (boolean.class.isAssignableFrom(attr.getClass())) {
            return (Boolean) attr;
        }
        if (attr instanceof Boolean) {
            return (Boolean) attr;
        }
        if (float.class.isAssignableFrom(attr.getClass())) {
            return ((Float) attr) > 0;
        }
        if (int.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).floatValue() > 0;
        }
        if (long.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).floatValue() > 0;
        }
        if (double.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).floatValue() > 0;
        }
        if (attr instanceof Number) {
            return ((Number) attr).floatValue() > 0;
        }
        if (attr instanceof List) {
            return ((Collection) attr).size() > 0;
        }
        if (attr instanceof Map) {
            return ((Map) attr).size() > 0;
        }
        return attr instanceof BlurObject && ((BlurObject) this.attr).toBooleanValue();
    }

    /**
     * @return 输出整数
     */
    public int toIntValue() {
        if (attr == null) {
            return 0;
        }
        if (int.class.isAssignableFrom(attr.getClass())) {
            return (Integer) attr;
        }
        if (long.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).intValue();
        }
        if (float.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).intValue();
        }
        if (double.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).intValue();
        }
        if (short.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).intValue();
        }
        if (attr instanceof Number) {
            return ((Number) attr).intValue();
        }
        if (attr instanceof String) {
            if (StringUtils.isNotBlank((CharSequence) attr)) {
                try {
                    return NumberFormat.getInstance().parse((String) attr).intValue();
                } catch (ParseException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            return 0;
        }
        int value = toInt();
        if (value != -1) {
            return value;
        }
        if (attr instanceof BlurObject) {
            return ((BlurObject) this.attr).toIntValue();
        }
        return 0;
    }

    private int toInt() {
        if (boolean.class.isAssignableFrom(attr.getClass())) {
            return (Boolean) attr ? 1 : 0;
        }
        if (attr instanceof Boolean) {
            return (Boolean) attr ? 1 : 0;
        }
        if (attr instanceof Map) {
            return ((Map) attr).size();
        }
        if (attr instanceof List) {
            return ((Collection) attr).size();
        }
        return -1;
    }

    /**
     * @return 输出串
     */
    public String toStringValue() {
        if (attr == null) {
            return null;
        }
        if (attr instanceof String) {
            return (String) attr;
        }
        if (attr instanceof Clob) {
            Clob clob = (Clob) attr;
            try (Reader reader = clob.getCharacterStream()) {
                if (clob.length() > 0 && reader != null) {
                    return IOUtils.toString(reader);
                }
            } catch (IOException | SQLException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        if (attr instanceof BlurObject) {
            return ((BlurObject) this.attr).toStringValue();
        }
        return attr.toString();
    }

    /**
     * @return 输出浮点数
     */
    public float toFloatValue() {
        if (attr == null) {
            return 0f;
        }
        if (float.class.isAssignableFrom(attr.getClass())) {
            return (Float) attr;
        }
        if (int.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).floatValue();
        }
        if (long.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).floatValue();
        }
        if (double.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).floatValue();
        }
        if (short.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).floatValue();
        }
        if (attr instanceof Number) {
            return ((Number) attr).floatValue();
        }
        if (attr instanceof String) {
            if (StringUtils.isNotBlank((CharSequence) attr)) {
                try {
                    return NumberFormat.getInstance().parse((String) attr).floatValue();
                } catch (ParseException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            return 0f;
        }
        if (boolean.class.isAssignableFrom(attr.getClass())) {
            return (Boolean) attr ? 1f : 0f;
        }
        if (attr instanceof Boolean) {
            return (Boolean) attr ? 1f : 0f;
        }
        if (attr instanceof Map) {
            return ((Map) attr).size();
        }
        if (attr instanceof List) {
            return ((Collection) attr).size();
        }
        if (attr instanceof BlurObject) {
            return ((BlurObject) this.attr).toFloatValue();
        }
        return 0f;
    }

    /**
     * @return 输出双精度
     */
    public double toDoubleValue() {
        if (attr == null) {
            return 0d;
        }
        if (double.class.isAssignableFrom(attr.getClass())) {
            return (Double) attr;
        }
        if (int.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).doubleValue();
        }
        if (long.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).doubleValue();
        }
        if (float.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).doubleValue();
        }
        if (short.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).doubleValue();
        }
        if (attr instanceof Number) {
            return ((Number) attr).doubleValue();
        }
        if (attr instanceof String) {
            if (StringUtils.isNotBlank((CharSequence) attr)) {
                try {
                    return NumberFormat.getInstance().parse((String) attr).doubleValue();
                } catch (ParseException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            return 0d;
        }
        if (boolean.class.isAssignableFrom(attr.getClass())) {
            return (Boolean) attr ? 1d : 0d;
        }
        if (attr instanceof Boolean) {
            return (Boolean) attr ? 1d : 0d;
        }
        if (attr instanceof Map) {
            return ((Map) attr).size();
        }
        if (attr instanceof List) {
            return ((Collection) attr).size();
        }
        if (attr instanceof BlurObject) {
            return ((BlurObject) this.attr).toDoubleValue();
        }
        return 0d;
    }

    /**
     * @return 输出长整形
     */
    public long toLongValue() {
        if (attr == null) {
            return 0;
        }
        if (long.class.isAssignableFrom(attr.getClass())) {
            return (Long) attr;
        }
        if (int.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).longValue();
        }
        if (float.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).longValue();
        }
        if (double.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).longValue();
        }
        if (short.class.isAssignableFrom(attr.getClass())) {
            return ((Number) attr).longValue();
        }
        if (attr instanceof Number) {
            return ((Number) attr).longValue();
        }
        if (attr instanceof String) {
            if (StringUtils.isNotBlank((CharSequence) attr)) {
                try {
                    return NumberFormat.getInstance().parse((String) attr).longValue();
                } catch (ParseException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
            return 0;
        }
        int value = toInt();
        if (value != -1) {
            return value;
        }
        if (attr instanceof BlurObject) {
            return ((BlurObject) this.attr).toLongValue();
        }
        return 0;
    }

    public byte toByteValue() {
        if (attr == null) {
            return 0;
        }
        if (attr instanceof Byte) {
            return (Byte) attr;
        }
        if (attr instanceof BlurObject) {
            return ((BlurObject) this.attr).toByteValue();
        }
        return Byte.parseByte(toStringValue());
    }

    public byte[] toBytesValue() {
        if (attr instanceof byte[]) {
            return (byte[]) attr;
        }
        if (attr instanceof Byte[]) {
            Byte[] bArr = (Byte[]) attr;
            byte[] returnArr = new byte[bArr.length];
            for (int idx = 0; idx < bArr.length; idx++) {
                returnArr[idx] = bArr[idx];
            }
            return returnArr;
        }
        if (attr instanceof Blob) {
            Blob blob = ((Blob) attr);
            try (InputStream input = blob.getBinaryStream()) {
                if (blob.length() > 0 && input != null) {
                    byte[] bArr = new byte[(int) blob.length()];
                    if (input.read(bArr) > 0) {
                        return bArr;
                    }
                }
            } catch (IOException | SQLException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return null;
    }

    public short toShortValue() {
        return (short) toIntValue();
    }

    public char toCharValue() {
        if (attr == null) {
            return Character.MIN_CODE_POINT;
        }
        if (attr instanceof Character) {
            return (Character) attr;
        }
        if (attr instanceof BlurObject) {
            return ((BlurObject) this.attr).toCharValue();
        }
        return Character.MIN_CODE_POINT;
    }

    /**
     * 输出指定类的对象
     *
     * @param clazz 指定类
     * @return 如果对象不能转换成指定类返回null，指定类是null，返回null。
     */
    public Object toObjectValue(Class<?> clazz) {
        Object object = null;
        if (clazz.equals(String.class)) {
            object = attr == null ? null : this.toStringValue();
        } else if (clazz.equals(Double.class)) {
            object = attr == null ? null : this.toDoubleValue();
        } else if (clazz.equals(double.class)) {
            object = this.toDoubleValue();
        } else if (clazz.equals(Float.class)) {
            object = attr == null ? null : this.toFloatValue();
        } else if (clazz.equals(float.class)) {
            object = this.toFloatValue();
        } else if (clazz.equals(Integer.class)) {
            object = attr == null ? null : this.toIntValue();
        } else if (clazz.equals(int.class)) {
            object = this.toIntValue();
        } else if (clazz.equals(Long.class)) {
            object = attr == null ? null : this.toLongValue();
        } else if (clazz.equals(long.class)) {
            object = this.toLongValue();
        } else if (clazz.equals(BigInteger.class)) {
            String value = StringUtils.trimToNull(toStringValue());
            if (value != null) {
                object = new BigInteger(value);
            }
        } else if (clazz.equals(BigDecimal.class)) {
            String value = StringUtils.trimToNull(toStringValue());
            if (value != null) {
                object = new BigDecimal(value);
            }
        } else if (clazz.equals(Boolean.class)) {
            object = attr == null ? null : this.toBooleanValue();
        } else if (clazz.equals(boolean.class)) {
            object = this.toBooleanValue();
        } else if (clazz.equals(Byte.class)) {
            object = attr == null ? null : this.toByteValue();
        } else if (clazz.equals(byte.class)) {
            object = this.toByteValue();
        } else if (clazz.equals(Byte[].class)) {
            object = attr == null ? null : this.toBytesValue();
        } else if (clazz.equals(byte[].class)) {
            object = this.toBytesValue();
        } else if (clazz.equals(Character.class)) {
            object = attr == null ? null : this.toCharValue();
        } else if (clazz.equals(char.class)) {
            object = this.toCharValue();
        } else if (clazz.equals(List.class)) {
            object = this.toListValue();
        } else if (clazz.equals(Map.class)) {
            object = this.toMapValue();
        } else if (clazz.equals(Set.class)) {
            object = this.toSetValue();
        }
        if (object == null && attr != null && !CONVERTERS.isEmpty()) {
            Map<Class<?>, IConverter<?>> map = CONVERTERS.get(clazz);
            if (map != null && !map.isEmpty()) {
                IConverter<?> converter = map.get(attr.getClass());
                if (converter != null) {
                    object = converter.convert(attr);
                }
            }
        }
        if (object == null) {
            try {
                object = clazz.cast(attr);
            } catch (ClassCastException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return object;
    }

    /**
     * @return 获得对象类
     */
    public Class<?> getObjectClass() {
        if (attr != null) {
            return attr.getClass();
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attr == null) ? 0 : attr.hashCode());
        Class<?> attrClass = attr == null ? null : attr.getClass();
        result = prime * result + ((attrClass == null) ? 0 : attrClass.hashCode());
        result = prime * result;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BlurObject other = (BlurObject) obj;
        if (this.attr == null) {
            if (other.toObjectValue() != null) {
                return false;
            }
        } else if (!this.attr.equals(other.toObjectValue())) {
            return false;
        }
        Class<?> attrClass = attr == null ? null : attr.getClass();
        if (attrClass == null) {
            if ((other.toObjectValue() != null ? other.toObjectValue().getClass() : null) != null) {
                return false;
            }
        } else if (!attrClass.equals(other.toObjectValue().getClass())) {
            return false;
        }
        return attr == other.toObjectValue();
    }

    @Override
    public String toString() {
        if (attr != null) {
            return attr.toString();
        }
        return StringUtils.EMPTY;
    }
}
