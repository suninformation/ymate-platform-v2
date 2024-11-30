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
package net.ymate.platform.commons;

import net.ymate.platform.commons.annotation.ExportColumn;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SHEET页分析处理器接口
 *
 * @param <T> 元素类型
 * @author 刘镇 (suninformation@163.com) on 2018/5/25 上午6:37
 */
public interface ISheetHandler<T> {

    class Default extends Abstract<Object[]> {
        @Override
        public Object[] parseRow(Row row) throws Exception {
            CellMeta[] cellMetas = getCellMetas();
            Object[] result = new Object[cellMetas.length];
            for (int idx = 0; idx < cellMetas.length; idx++) {
                CellMeta cellMeta = cellMetas[idx];
                Cell cell = row.getCell(cellMeta.getCellIndex());
                result[idx] = new Object[]{cellMeta.getName(), parseCell(cell)};
            }
            return result;
        }
    }

    /**
     * @since 2.1.3
     */
    class Bean<T> extends Abstract<T> {

        private final Class<T> beanClass;

        private final Map<Class<? extends IExportDataRender>, IExportDataRender> rendersCache = new HashMap<>();

        private final Map<String, IExportDataRender> renders = new HashMap<>();

        Map<String, PairObject<String, ExportColumn>> columnsMap = new HashMap<>();

        @SuppressWarnings("unchecked")
        public Bean() {
            beanClass = (Class<T>) ClassUtils.getParameterizedTypes(getClass()).get(0);
        }

        public Bean(Class<T> beanClass) {
            this.beanClass = beanClass;
        }

        @Override
        public List<T> handle(Sheet sheet) throws Exception {
            for (Field field : ClassUtils.getFields(beanClass, true)) {
                ExportColumn columnAnn = field.getAnnotation(ExportColumn.class);
                if (columnAnn != null && !columnAnn.excluded() && columnAnn.importable()) {
                    if (!columnAnn.render().equals(IExportDataRender.class)) {
                        IExportDataRender dataRender = rendersCache.computeIfAbsent(columnAnn.render(), aClass -> ClassUtils.impl(columnAnn.render(), IExportDataRender.class));
                        if (dataRender != null) {
                            renders.put(field.getName(), dataRender);
                        }
                    }
                    columnsMap.put(StringUtils.defaultIfBlank(columnAnn.value(), field.getName()), PairObject.bind(field.getName(), columnAnn));
                }
            }
            return super.handle(sheet);
        }

        @Override
        public T parseRow(Row row) throws Exception {
            ClassUtils.BeanWrapper<T> beanWrapper = ClassUtils.wrapperClass(beanClass);
            if (beanWrapper != null) {
                for (CellMeta cellMeta : getCellMetas()) {
                    PairObject<String, ExportColumn> column = columnsMap.get(cellMeta.getName());
                    if (column != null) {
                        Object value = parseCell(row.getCell(cellMeta.getCellIndex()));
                        if (value != null) {
                            IExportDataRender dataRender = renders.get(column.getKey());
                            if (dataRender != null) {
                                value = dataRender.render(beanWrapper, column.getValue(), column.getKey(), value, true);
                            } else if (column.getValue().dateTime()) {
                                if (value instanceof String) {
                                    value = DateTimeUtils.parseDateTime(BlurObject.bind(value).toStringValue(), StringUtils.defaultIfBlank(column.getValue().pattern(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS)).getTime();
                                } else if (value instanceof Number || float.class.isAssignableFrom(value.getClass())
                                        || int.class.isAssignableFrom(value.getClass())
                                        || long.class.isAssignableFrom(value.getClass())
                                        || double.class.isAssignableFrom(value.getClass())) {
                                    value = DateTimeHelper.bind(BlurObject.bind(value).toLongValue()).time();
                                }
                            } else if (column.getValue().dataRange().length > 0) {
                                String valueStr = BlurObject.bind(value).toStringValue();
                                String[] dataRange = column.getValue().dataRange();
                                for (int idx = 0; idx < dataRange.length; idx++) {
                                    if (StringUtils.equalsIgnoreCase(valueStr, dataRange[idx])) {
                                        value = idx;
                                        break;
                                    }
                                }
                            } else if (column.getValue().currency()) {
                                value = doProcessCurrencyValue(column.getValue(), value).toDoubleValue();
                            }
                            beanWrapper.setValue(column.getKey(), value);
                        }
                    }
                }
                return beanWrapper.getTargetObject();
            }
            return null;
        }

        private BlurObject doProcessCurrencyValue(ExportColumn columnAnn, Object currencyValue) {
            int decimals = columnAnn.decimals();
            if (decimals <= 0) {
                decimals = 2;
            }
            MathCalcHelper mathCalcHelper = MathCalcHelper.bind(BlurObject.bind(currencyValue).toStringValue()).scale(decimals);
            if (columnAnn.accuracy()) {
                mathCalcHelper.multiply(Math.pow(10, decimals));
            } else {
                mathCalcHelper.round();
            }
            return mathCalcHelper.toBlurObject();
        }
    }

    /**
     * @since 2.1.3
     */
    abstract class Abstract<T> implements ISheetHandler<T> {

        private int firstRowNum;

        private int lastRowNum;

        private int firstCellNum;

        private int lastCellNum;

        private String decimalPattern;

        private CellMeta[] cellMetas;

        public Abstract<T> firstRowNum(int firstRowNum) {
            this.firstRowNum = firstRowNum;
            return this;
        }

        public Abstract<T> lastRowNum(int lastRowNum) {
            this.lastRowNum = lastRowNum;
            return this;
        }

        public Abstract<T> firstCellNum(int firstCellNum) {
            this.firstCellNum = firstCellNum;
            return this;
        }

        public Abstract<T> lastCellNum(int lastCellNum) {
            this.lastCellNum = lastCellNum;
            return this;
        }

        public Abstract<T> decimalPattern(String decimalPattern) {
            this.decimalPattern = decimalPattern;
            return this;
        }

        @Override
        public List<T> handle(Sheet sheet) throws Exception {
            List<T> results = new ArrayList<>();
            int startRowIdx = firstRowNum > 0 ? firstRowNum : sheet.getFirstRowNum();
            int maxRowIdx = lastRowNum > 0 ? lastRowNum : sheet.getLastRowNum();
            for (int rowIdx = startRowIdx; rowIdx <= maxRowIdx; rowIdx++) {
                Row sheetRow = sheet.getRow(rowIdx);
                if (cellMetas == null && rowIdx == startRowIdx) {
                    List<CellMeta> metaList = new ArrayList<>();
                    short cellIdx = firstCellNum > 0 ? (short) firstCellNum : sheetRow.getFirstCellNum();
                    short maxCellIdx = lastCellNum > 0 ? (short) lastCellNum : sheetRow.getLastCellNum();
                    for (; cellIdx <= maxCellIdx; cellIdx++) {
                        Object cellValue = parseCell(sheetRow.getCell(cellIdx));
                        if (cellValue != null) {
                            metaList.add(new CellMeta(BlurObject.bind(cellValue).toStringValue(), cellIdx));
                        }
                    }
                    cellMetas = metaList.toArray(new CellMeta[0]);
                } else if (rowIdx > startRowIdx) {
                    T row = parseRow(sheetRow);
                    if (row != null) {
                        results.add(row);
                    }
                }
            }
            return results;
        }

        @Override
        public CellMeta[] getCellMetas() {
            return cellMetas;
        }

        protected Object parseCell(Cell cell) throws Exception {
            Object value = null;
            if (cell != null) {
                switch (cell.getCellType()) {
                    case STRING:
                        value = cell.getStringCellValue();
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell) && cell.getDateCellValue() != null) {
                            value = cell.getDateCellValue().getTime();
                        } else {
                            value = new DecimalFormat(StringUtils.defaultIfBlank(decimalPattern, "##.###")).format(cell.getNumericCellValue());
                        }
                        break;
                    case FORMULA:
                        value = cell.getCellFormula();
                        break;
                    case BOOLEAN:
                        value = cell.getBooleanCellValue();
                        break;
                    case BLANK:
                    case ERROR:
                    default:
                        value = StringUtils.EMPTY;
                }
            }
            return value;
        }
    }

    /**
     * CELL单元格描述对象
     */
    class CellMeta {

        private final String name;

        private final int cellIndex;

        public CellMeta(String name, int cellIndex) {
            this.name = name;
            this.cellIndex = cellIndex;
        }

        public String getName() {
            return name;
        }

        public int getCellIndex() {
            return cellIndex;
        }
    }

    /**
     * 处理Sheet页
     *
     * @param sheet Sheet页接口对象
     * @return 返回数据对象集合
     * @throws Exception 可能产生的任何异常
     */
    List<T> handle(Sheet sheet) throws Exception;

    /**
     * 获取单元格描述对象集合
     *
     * @return 返回单元格描述对象集合
     */
    CellMeta[] getCellMetas();

    /**
     * 分析行数据
     *
     * @param row 记录行接口对象
     * @return 返回行数据对象
     * @throws Exception 可能产生的任何异常
     */
    T parseRow(Row row) throws Exception;

}
