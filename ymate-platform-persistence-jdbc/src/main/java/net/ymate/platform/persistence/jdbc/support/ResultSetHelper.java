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
package net.ymate.platform.persistence.jdbc.support;

import net.ymate.platform.commons.ConsoleTableBuilder;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.PropertyMeta;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * 数据结果集处理类，用于帮助开发人员便捷的读取结果集中数据内容<br>
 * 注：此类仅支持结果集由 ArrayResultSetHandler 和 MapResultSetHandler 产生的数据<br>
 *
 * @author 刘镇 (suninformation@163.com) on 2010-10-10 上午10:59:40
 */
public final class ResultSetHelper {

    private static final Log LOG = LogFactory.getLog(ResultSetHelper.class);

    /**
     * 数据结果集
     */
    private List<?> dataSet;

    private final boolean isArray;

    private int rowCount;

    private int columnCount;

    private boolean clearFlag;

    private String[] columnNames;

    public static ResultSetHelper bind(Object[] data) {
        List<Object[]> arrayList = new ArrayList<>();
        arrayList.add(data);
        return bind(arrayList);
    }

    public static ResultSetHelper bind(Map<String, Object> data) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        mapList.add(data);
        return bind(mapList);
    }

    public static ResultSetHelper bind(IResultSet<?> resultSet) {
        return bind(resultSet.getResultData());
    }

    /**
     * @param dataSet 结果数据集合
     * @return 绑定结果集数据，若参数为空则返回默认数据为空的实例对象
     */
    public static ResultSetHelper bind(List<?> dataSet) {
        if (dataSet != null && !dataSet.isEmpty()) {
            Object value = dataSet.get(0);
            if (value instanceof Map) {
                return new ResultSetHelper(dataSet, false);
            } else if (value instanceof Object[]) {
                return new ResultSetHelper(dataSet, true);
            }
        }
        return new ResultSetHelper();
    }

    /**
     * 空构造
     *
     * @since 2.1.0
     */
    private ResultSetHelper() {
        this.dataSet = new ArrayList<>();
        this.isArray = true;
        this.rowCount = 0;
        this.columnCount = 0;
        this.columnNames = new String[0];
    }


    /**
     * 构造器
     *
     * @param resultSet 结果数据集合
     * @param isArray   集合中数据是否为数组类型
     */
    @SuppressWarnings("unchecked")
    private ResultSetHelper(List<?> resultSet, boolean isArray) {
        this.dataSet = resultSet;
        this.isArray = isArray;
        if (this.dataSet != null) {
            this.rowCount = this.dataSet.size();
            if (this.rowCount > 0) {
                // 计算字段数量
                if (this.isArray) {
                    this.columnCount = ((Object[]) this.dataSet.get(0)).length;
                } else {
                    this.columnCount = ((Map<?, ?>) this.dataSet.get(0)).size();
                }
                // 处理字段名称集合
                if (this.isArray) {
                    Object[] obj = (Object[]) this.dataSet.get(0);
                    this.columnNames = new String[obj.length];
                    for (int i = 0; i < obj.length; i++) {
                        Object[] columnObj = (Object[]) obj[i];
                        this.columnNames[i] = (String) columnObj[0];
                    }
                } else {
                    Map<String, Object> map = (Map<String, Object>) this.dataSet.get(0);
                    Iterator<String> itemIt = map.keySet().iterator();
                    this.columnNames = new String[map.keySet().size()];
                    int idx = 0;
                    while (itemIt.hasNext()) {
                        this.columnNames[idx] = itemIt.next();
                        idx++;
                    }
                }
            }
        }
    }

    /**
     * 获取当前结果集是否可用，即是否为空或元素数量为0
     *
     * @return 若当前结果集可用将返回true，否则返回false
     * @since 2.1.0
     */
    public boolean isResultsAvailable() {
        return !dataSet.isEmpty();
    }

    /**
     * 获取结果集的列名
     *
     * @return String[] 字段名称集合
     */
    public String[] getColumnNames() {
        return this.columnNames;
    }

    /**
     * 清除结果集 本方法为可选方法
     */
    public void clearAll() {
        if (this.dataSet != null) {
            this.dataSet.clear();
            this.dataSet = null;
        }
        this.columnNames = null;
        this.clearFlag = true;
    }

    @Override
    protected void finalize() throws Throwable {
        if (!this.clearFlag) {
            clearAll();
        }
        super.finalize();
    }

    /**
     * 遍历结果集合
     *
     * @param handler 结果集元素处理器
     * @throws Exception 可能产生的异常
     */
    public void forEach(ItemHandler handler) throws Exception {
        this.forEach(1, handler);
    }

    /**
     * 遍历结果集合
     *
     * @param step    步长
     * @param handler 结果集元素处理器
     * @throws Exception 可能产生的异常
     */
    public void forEach(int step, ItemHandler handler) throws Exception {
        step = step > 0 ? step : 1;
        for (int idx = step - 1; idx < rowCount; idx += step) {
            if (!handler.handle(new ItemWrapper(dataSet.get(idx), isArray), idx)) {
                break;
            }
        }
    }

    /**
     * @return 返回结果集中第一个元素的包装对象
     */
    public ItemWrapper firstItemWrapper() {
        return new ItemWrapper(dataSet.get(0), isArray);
    }

    /**
     * 结果集元素处理器
     */
    public interface ItemHandler {

        /**
         * 处理结果集行数据
         *
         * @param wrapper 元素包装对象，提供多种数据提取方法
         * @param row     结果集当前所在行数
         * @return 返回值将决定此次遍历是否继续执行，true或false
         * @throws Exception 可能产生的异常
         */
        boolean handle(ItemWrapper wrapper, int row) throws Exception;
    }

    /**
     * 结果集元素包装对象
     */
    public class ItemWrapper {

        private final Object item;

        private final boolean isArray;

        public ItemWrapper(Object item, boolean isArray) {
            this.item = item;
            this.isArray = isArray;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public String[] getColumnNames() {
            return columnNames;
        }

        /**
         * @param columnName 字段名称
         * @return 按照字段名获取字段值
         */
        public Object getObject(String columnName) {
            return this.doGetObject(columnName);
        }

        @SuppressWarnings("unchecked")
        private Object doGetObject(String columnName) {
            Object returnValue = null;
            if (this.isArray) {
                Object[] obj = (Object[]) item;
                for (int i = 0; i < columnNames.length; i++) {
                    if (columnNames[i].equalsIgnoreCase(columnName)) {
                        Object[] object = (Object[]) obj[i];
                        returnValue = object[1];
                        break;
                    }
                }
            } else {
                Map<String, Object> map = (Map<String, Object>) item;
                returnValue = map.get(columnName);
                if (returnValue == null) {
                    for (String column : columnNames) {
                        if (column.equalsIgnoreCase(columnName)) {
                            returnValue = map.get(column);
                            break;
                        }
                    }
                }
            }
            return returnValue;
        }

        /**
         * @param index 索引
         * @return 按列名顺序获取字段值
         */
        public Object getObject(int index) {
            return this.doGetObject(index);
        }

        @SuppressWarnings("unchecked")
        private Object doGetObject(int index) {
            Object returnValue = null;
            if (index >= 0 && index < columnCount) {
                if (this.isArray) {
                    Object[] obj = (Object[]) item;
                    Object[] object = (Object[]) obj[index];
                    returnValue = object[1];
                } else {
                    Map<String, Object> map = (Map<String, Object>) item;
                    Iterator<Object> itemIt = map.values().iterator();
                    int i = 0;
                    while (itemIt.hasNext()) {
                        returnValue = itemIt.next();
                        if (index == i) {
                            break;
                        } else {
                            returnValue = null;
                        }
                        i++;
                    }
                }
            }
            return returnValue;
        }

        public Time getAsTime(int i) {
            Object o = getObject(i);
            if (o == null) {
                return null;
            }
            if (o instanceof Time) {
                return (Time) o;
            } else {
                return new Time(((Date) o).getTime());
            }
        }

        public Time getAsTime(String columnName) {
            Object o = getObject(columnName);
            if (o == null) {
                return null;
            }
            if (o instanceof Time) {
                return (Time) o;
            }
            return new Time(((Date) o).getTime());
        }

        public Timestamp getAsTimestamp(int i) {
            Object o = getObject(i);
            if (o == null) {
                return null;
            }
            if (o instanceof Timestamp) {
                return (Timestamp) o;
            }
            return new Timestamp(((Date) o).getTime());
        }

        public Timestamp getAsTimestamp(String columnName) {
            Object o = getObject(columnName);
            if (o == null) {
                return null;
            }
            if (o instanceof Timestamp) {
                return (Timestamp) o;
            }
            return new Timestamp(((Date) o).getTime());
        }

        public Date getAsDate(int i) {
            Object o = getObject(i);
            if (o == null) {
                return null;
            }
            if (o instanceof Date) {
                return (Date) o;
            }
            return new Date(((Timestamp) o).getTime());
        }

        public Date getAsDate(String columnName) {
            Object o = getObject(columnName);
            if (o == null) {
                return null;
            }
            if (o instanceof Date) {
                return (Date) o;
            }
            return new Date(((Timestamp) o).getTime());
        }

        public Float getAsFloat(int i) {
            Object o = getObject(i);
            if (o == null) {
                return null;
            }
            return BlurObject.bind(o).toFloatValue();
        }

        public Float getAsFloat(String columnName) {
            Object o = getObject(columnName);
            if (o == null) {
                return null;
            }
            return BlurObject.bind(o).toFloatValue();
        }

        public Double getAsDouble(int i) {
            Object o = getObject(i);
            if (o == null) {
                return null;
            }
            return BlurObject.bind(o).toDoubleValue();
        }

        public Double getAsDouble(String columnName) {
            Object o = getObject(columnName);
            if (o == null) {
                return null;
            }
            return BlurObject.bind(o).toDoubleValue();
        }

        private Byte objectToByte(Object o) {
            if (o == null) {
                return null;
            }
            if (o instanceof Byte) {
                return (Byte) o;
            } else if (o instanceof Integer) {
                return ((Integer) o).byteValue();
            } else {
                return ((BigDecimal) o).byteValue();
            }
        }

        public Byte getAsByte(int i) {
            return objectToByte(getObject(i));
        }

        public Byte getAsByte(String columnName) {
            return objectToByte(getObject(columnName));
        }

        private Short objectToShort(Object o) {
            if (o == null) {
                return null;
            }
            if (o instanceof Short) {
                return (Short) o;
            } else if (o instanceof Integer) {
                return ((Integer) o).shortValue();
            } else {
                return ((BigDecimal) o).shortValue();
            }
        }

        public Short getAsShort(int i) {
            return objectToShort(getObject(i));
        }

        public Short getAsShort(String columnName) {
            return objectToShort(getObject(columnName));
        }

        public Long getAsLong(int i) {
            Object o = getObject(i);
            if (o == null) {
                return null;
            }
            return BlurObject.bind(o).toLongValue();
        }

        public Long getAsLong(String columnName) {
            Object o = getObject(columnName);
            if (o == null) {
                return null;
            }
            return BlurObject.bind(o).toLongValue();
        }

        public BigDecimal getAsBigDecimal(int i) {
            Object o = getObject(i);
            if (o == null) {
                return null;
            }
            return (BigDecimal) o;
        }

        public BigDecimal getAsBigDecimal(String columnName) {
            Object o = getObject(columnName);
            if (o == null) {
                return null;
            }
            return (BigDecimal) o;
        }

        public Integer getAsInteger(int i) {
            Object o = getObject(i);
            if (o == null) {
                return null;
            }
            return BlurObject.bind(o).toIntValue();
        }

        public Integer getAsInteger(String columnName) {
            Object o = getObject(columnName);
            if (o == null) {
                return null;
            }
            return new BlurObject(o).toIntValue();
        }

        public Character getAsChar(String columnName) {
            Object o = getObject(columnName);
            if (o == null) {
                return null;
            }
            if (o instanceof Character) {
                return (Character) o;
            } else {
                return o.toString().charAt(0);
            }
        }

        public Character getAsChar(int index) {
            Object o = getObject(index);
            if (o == null) {
                return null;
            }
            if (o instanceof Character) {
                return (Character) o;
            } else {
                return o.toString().charAt(0);
            }
        }

        public String getAsString(String columnName) {
            Object v = getObject(columnName);
            if (v != null) {
                return v.toString();
            } else {
                return null;
            }
        }

        public String getAsString(int index) {
            Object v = getObject(index);
            if (v != null) {
                return v.toString();
            } else {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        public <T extends IEntity> T toEntity(T entityObject) throws Exception {
            EntityMeta entityMeta = EntityMeta.createAndGet(entityObject.getClass());
            if (entityMeta != null) {
                Object primaryKeyObject = null;
                if (entityMeta.isMultiplePrimaryKey()) {
                    primaryKeyObject = entityMeta.getPrimaryKeyClass().newInstance();
                    //
                    entityObject.setId((Serializable) primaryKeyObject);
                }
                for (PropertyMeta propertyMeta : entityMeta.getProperties()) {
                    Object fieldValue = getObject(propertyMeta.getName());
                    if (fieldValue != null) {
                        if (entityMeta.isPrimaryKey(propertyMeta.getName()) && entityMeta.isMultiplePrimaryKey()) {
                            propertyMeta.getField().set(primaryKeyObject, fieldValue);
                        } else {
                            propertyMeta.getField().set(entityObject, fieldValue);
                        }
                    }
                }
            }
            return entityObject;
        }

        public <T> T toObject(T valueObject) throws Exception {
            ClassUtils.BeanWrapper<?> wrapper = ClassUtils.wrapper(valueObject);
            for (String fieldName : wrapper.getFieldNames()) {
                String columnName = EntityMeta.fieldNameToPropertyName(fieldName, 0);
                Object value = this.getObject(columnName);
                if (value == null) {
                    continue;
                }
                wrapper.setValue(fieldName, value);
            }
            return valueObject;
        }
    }

    private ConsoleTableBuilder buildTableBuilder(String type) {
        ConsoleTableBuilder tableBuilder = ConsoleTableBuilder.create(columnCount).escape();
        if (StringUtils.isNotBlank(type)) {
            switch (type) {
                case ConsoleTableBuilder.TYPE_CSV:
                    tableBuilder.csv();
                    break;
                case ConsoleTableBuilder.TYPE_MARKDOWN:
                    tableBuilder.markdown();
                    break;
                default:
            }
        }
        // Append Headers
        ConsoleTableBuilder.Row header = tableBuilder.addRow();
        Arrays.stream(getColumnNames()).map(StringUtils::upperCase).forEachOrdered(header::addColumn);
        // Append Rows
        try {
            forEach((wrapper, row) -> {
                ConsoleTableBuilder.Row newRow = tableBuilder.addRow();
                Arrays.stream(wrapper.getColumnNames()).map(colName -> BlurObject.bind(wrapper.getObject(colName)).toStringValue()).forEachOrdered(newRow::addColumn);
                return true;
            });
        } catch (Exception ignored) {
        }
        return tableBuilder;
    }

    /**
     * @return 输出控制台表格
     * @since 2.1.0
     */
    @Override
    public String toString() {
        if (!dataSet.isEmpty()) {
            return buildTableBuilder(null).toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * @return 输出CSV表格
     * @since 2.1.0
     */
    public String toCsv() {
        if (!dataSet.isEmpty()) {
            return buildTableBuilder(ConsoleTableBuilder.TYPE_CSV).toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * @return 输出Markdown表格
     * @since 2.1.0
     */
    public String toMarkdown() {
        if (!dataSet.isEmpty()) {
            return buildTableBuilder(ConsoleTableBuilder.TYPE_MARKDOWN).toString();
        }
        return StringUtils.EMPTY;
    }

    public void writeTo(File outputFile) throws IOException {
        writeTo(null, outputFile);
    }

    public void writeTo(String type, File outputFile) throws IOException {
        if (!dataSet.isEmpty()) {
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                buildTableBuilder(type).writeTo(outputStream);
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("Successfully written to file: %s", outputFile.getPath()));
                }
            }
        }
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        writeTo(null, outputStream);
    }

    public void writeTo(String type, OutputStream outputStream) throws IOException {
        if (!dataSet.isEmpty()) {
            buildTableBuilder(type).writeTo(outputStream);
        }
    }
}
