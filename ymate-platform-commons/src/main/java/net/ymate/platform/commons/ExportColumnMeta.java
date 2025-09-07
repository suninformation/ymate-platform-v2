/*
 * Copyright 2007-2025 the original author or authors.
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

import net.ymate.platform.commons.annotation.ExportColumn;
import net.ymate.platform.commons.util.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author 刘镇 (suninformation@163.com) on 2025/9/7 16:06
 * @since 2.1.4
 */
public class ExportColumnMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    public static ExportColumnMeta create(ExportColumn exportColumn) {
        if (exportColumn != null) {
            return new ExportColumnMeta()
                    .setName(exportColumn.value())
                    .setDataRange(exportColumn.dataRange())
                    .setDateTime(exportColumn.dateTime())
                    .setPattern(exportColumn.pattern())
                    .setCurrency(exportColumn.currency())
                    .setAccuracy(exportColumn.accuracy())
                    .setDecimals(exportColumn.decimals())
                    .setExcluded(exportColumn.excluded())
                    .setRender(exportColumn.render())
                    .setImportable(exportColumn.importable())
                    .setOrder(exportColumn.order());
        }
        return new ExportColumnMeta();
    }

    public static ExportColumnMeta create() {
        return new ExportColumnMeta();
    }

    public ExportColumnMeta() {
        this.name = StringUtils.EMPTY;
        this.dataRange = new String[0];
        this.pattern = DateTimeUtils.YYYY_MM_DD_HH_MM_SS;
        this.accuracy = true;
        this.decimals = 2;
        this.render = IExportDataRender.class;
        this.importable = true;
        this.order = 0;
    }

    /**
     * 列名称
     */
    private String name;

    /**
     * 针对数值数据通过下标输出值(若下标越界将输出原始值)
     */
    private String[] dataRange;

    /**
     * 指定将列值转换为日期
     */
    private boolean dateTime;

    /**
     * 日期时间输出模式
     */
    private String pattern;

    /**
     * 指定列为货币类型将值保留小数
     */
    private boolean currency;

    /**
     * 配置货币类型计算时是否将原值除以10的decimals次方后计算（基于数值以整数存储的情况）
     */
    private boolean accuracy;

    /**
     * 配置货币类型计算时保留小数位数
     */
    private int decimals;

    /**
     * 排除导出属性
     */
    private boolean excluded;

    /**
     * 自定义列渲染器接口实现类
     */
    private Class<? extends IExportDataRender> render;

    /**
     * 标记列是否用于数据导入
     */
    private boolean importable;

    /**
     * 排序
     */
    private int order;

    public String getName() {
        return name;
    }

    public ExportColumnMeta setName(String name) {
        this.name = name;
        return this;
    }

    public String[] getDataRange() {
        return dataRange;
    }

    public ExportColumnMeta setDataRange(String[] dataRange) {
        this.dataRange = dataRange;
        return this;
    }

    public boolean isDateTime() {
        return dateTime;
    }

    public ExportColumnMeta setDateTime(boolean dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public ExportColumnMeta setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public boolean isCurrency() {
        return currency;
    }

    public ExportColumnMeta setCurrency(boolean currency) {
        this.currency = currency;
        return this;
    }

    public boolean isAccuracy() {
        return accuracy;
    }

    public ExportColumnMeta setAccuracy(boolean accuracy) {
        this.accuracy = accuracy;
        return this;
    }

    public int getDecimals() {
        return decimals;
    }

    public ExportColumnMeta setDecimals(int decimals) {
        this.decimals = decimals;
        return this;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public ExportColumnMeta setExcluded(boolean excluded) {
        this.excluded = excluded;
        return this;
    }

    public Class<? extends IExportDataRender> getRender() {
        return render;
    }

    public ExportColumnMeta setRender(Class<? extends IExportDataRender> render) {
        this.render = render;
        return this;
    }

    public boolean isImportable() {
        return importable;
    }

    public ExportColumnMeta setImportable(boolean importable) {
        this.importable = importable;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public ExportColumnMeta setOrder(int order) {
        this.order = order;
        return this;
    }

    @Override
    public String toString() {
        return String.format("ExportColumnMeta [name='%s', dataRange=%s, dateTime=%s, pattern='%s', currency=%s, accuracy=%s, decimals=%d, excluded=%s, render=%s, importable=%s, order=%d]", name, Arrays.toString(dataRange), dateTime, pattern, currency, accuracy, decimals, excluded, render, importable, order);
    }
}
