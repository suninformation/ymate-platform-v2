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

import net.ymate.platform.commons.lang.BlurObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * SHEET页分析处理器接口
 *
 * @param <T> 元素类型
 * @author 刘镇 (suninformation@163.com) on 2018/5/25 上午6:37
 */
public interface ISheetHandler<T> {

    class Default implements ISheetHandler<Object[]> {

        private int firstRowNum;

        private int lastRowNum;

        private int firstCellNum;

        private int lastCellNum;

        private String decimalPattern;

        private CellMeta[] cellMetas;

        public Default firstRowNum(int firstRowNum) {
            this.firstRowNum = firstRowNum;
            return this;
        }

        public Default lastRowNum(int lastRowNum) {
            this.lastRowNum = lastRowNum;
            return this;
        }

        public Default firstCellNum(int firstCellNum) {
            this.firstCellNum = firstCellNum;
            return this;
        }

        public Default lastCellNum(int lastCellNum) {
            this.lastCellNum = lastCellNum;
            return this;
        }

        public Default decimalPattern(String decimalPattern) {
            this.decimalPattern = decimalPattern;
            return this;
        }

        @Override
        public List<Object[]> handle(Sheet sheet) throws Exception {
            List<Object[]> results = new ArrayList<>();
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
                    results.add(parseRow(sheetRow));
                }
            }
            return results;
        }

        @Override
        public CellMeta[] getCellMetas() {
            return cellMetas;
        }

        @Override
        public Object[] parseRow(Row row) throws Exception {
            Object[] result = new Object[cellMetas.length];
            for (int idx = 0; idx < cellMetas.length; idx++) {
                CellMeta cellMeta = cellMetas[idx];
                result[idx] = new Object[]{cellMeta.getName(), parseCell(row.getCell(cellMeta.getCellIndex()))};
            }
            return result;
        }

        private Object parseCell(Cell cell) throws Exception {
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
