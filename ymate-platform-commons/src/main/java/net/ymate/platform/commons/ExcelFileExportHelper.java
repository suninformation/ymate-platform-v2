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
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/12/25 下午2:42
 */
public final class ExcelFileExportHelper {

    private Map<String, Object> data;

    private final Map<String, String> customFieldNames = new HashMap<>();

    private final List<String> excludedFieldNames = new ArrayList<>();

    private final Map<String, IExportDataRender> renders = new HashMap<>();

    private IExportDataProcessor processor;

    public static ExcelFileExportHelper bind() {
        return new ExcelFileExportHelper();
    }

    public static ExcelFileExportHelper bind(Map<String, Object> data) {
        return new ExcelFileExportHelper(data);
    }

    public static ExcelFileExportHelper bind(IExportDataProcessor processor) {
        return new ExcelFileExportHelper(processor);
    }

    private ExcelFileExportHelper() {
        data = new HashMap<>();
    }

    private ExcelFileExportHelper(Map<String, Object> data) {
        if (data == null) {
            throw new NullArgumentException("data");
        }
        this.data = data;
    }

    private ExcelFileExportHelper(IExportDataProcessor processor) {
        if (processor == null) {
            throw new NullArgumentException("processor");
        }
        this.processor = processor;
    }

    public ExcelFileExportHelper excludedFieldNames(String[] fieldNames) {
        if (fieldNames != null && fieldNames.length > 0) {
            excludedFieldNames.addAll(Arrays.asList(fieldNames));
        }
        return this;
    }

    public ExcelFileExportHelper putFieldRender(String fieldName, IExportDataRender render) {
        if (StringUtils.isNotBlank(fieldName) && render != null) {
            renders.put(fieldName, render);
        }
        return this;
    }

    public ExcelFileExportHelper putCustomFieldName(String name, String customFieldName) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(customFieldName)) {
            customFieldNames.put(name, customFieldName);
        }
        return this;
    }

    public ExcelFileExportHelper putData(String varName, Object data) {
        if (StringUtils.isBlank(varName)) {
            throw new NullArgumentException("varName");
        }
        if (data == null) {
            throw new NullArgumentException("data");
        }
        this.data.put(varName, data);
        return this;
    }

    public File export(Class<?> dataType) throws Exception {
        return export(dataType, false);
    }

    public File export(Class<?> dataType, boolean xssf) throws Exception {
        return export(dataType, null, xssf);
    }

    public File export(Class<?> dataType, String charset) throws Exception {
        return export(dataType, charset, false);
    }

    public File export(Class<?> dataType, String charset, boolean xssf) throws Exception {
        File file = null;
        if (processor != null) {
            List<File> files = new ArrayList<>();
            for (int idx = 1; ; idx++) {
                List<?> processorData = processor.getData(idx);
                if (processorData == null || processorData.isEmpty()) {
                    break;
                }
                files.add(doExport(dataType, idx, processorData, charset, xssf));
            }
            file = toZip(files);
        } else if (!data.isEmpty()) {
            file = doExport(dataType, 1, Collections.singletonList(data), charset, xssf);
        }
        return file;
    }

    private File toZip(List<File> files) throws IOException {
        if (!files.isEmpty()) {
            if (files.size() == 1) {
                return files.get(0);
            }
            return FileUtils.toZip("export_", files.toArray(new File[0]));
        }
        return null;
    }

    private File doExport(Class<?> dataType, int index, List<?> data, String charset, boolean xssf) throws Exception {
        ClassUtils.BeanWrapper<?> beanWrapper = ClassUtils.wrapperClass(dataType);
        if (beanWrapper != null) {
            Collection<Field> fields = beanWrapper.getFields();
            Map<String, ExportColumn> columnsMap = new HashMap<>(fields.size());
            List<String> columnNames = new ArrayList<>();
            fields.stream().filter(field -> !excludedFieldNames.contains(field.getName())).forEachOrdered(field -> {
                ExportColumn exportColumnAnn = field.getAnnotation(ExportColumn.class);
                if (exportColumnAnn != null) {
                    columnsMap.put(field.getName(), exportColumnAnn);
                    if (!exportColumnAnn.excluded()) {
                        if (!exportColumnAnn.render().equals(IExportDataRender.class)) {
                            IExportDataRender dataRender = ClassUtils.impl(exportColumnAnn.render(), IExportDataRender.class);
                            if (dataRender != null) {
                                renders.put(field.getName(), dataRender);
                            }
                        }
                        String colName = StringUtils.defaultIfBlank(exportColumnAnn.value(), field.getName());
                        columnNames.add(customFieldNames.getOrDefault(colName, colName));
                    }
                } else {
                    columnNames.add(customFieldNames.getOrDefault(field.getName(), field.getName()));
                }
            });
            if (xssf) {
                return doExportExcel(columnNames, columnsMap, index, data);
            } else {
                return doExportCsv(columnNames, columnsMap, index, data, charset);
            }
        }
        return null;
    }

    private File doExportExcel(List<String> columnNames, Map<String, ExportColumn> columnsMap, int index, List<?> data) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(true)) {
            Sheet sheet = workbook.createSheet();
            CellStyle cellStyle = workbook.createCellStyle();
            int rowCount = 0;
            Row head = sheet.createRow(rowCount++);
            for (int i = 0; i < columnNames.size(); i++) {
                Cell cell = head.createCell(i);
                cell.setCellValue(columnNames.get(i));
                cell.setCellStyle(cellStyle);
            }
            for (Object item : data) {
                ClassUtils.BeanWrapper<?> objectBeanWrapper = ClassUtils.wrapper(item);
                Row newRow = sheet.createRow(rowCount++);
                int cellCount = 0;
                for (String fieldName : objectBeanWrapper.getFieldNames()) {
                    if (!excludedFieldNames.contains(fieldName)) {
                        String cellValue;
                        try {
                            ExportColumn exportColumnAnn = columnsMap.get(fieldName);
                            if (exportColumnAnn != null && exportColumnAnn.excluded()) {
                                continue;
                            }
                            IExportDataRender dataRender = renders.get(fieldName);
                            if (exportColumnAnn != null && dataRender != null) {
                                String valueStr = dataRender.render(exportColumnAnn, fieldName, objectBeanWrapper.getValue(fieldName));
                                if (StringUtils.isNotBlank(valueStr)) {
                                    cellValue = valueStr;
                                } else {
                                    cellValue = StringUtils.trimToEmpty(BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toStringValue());
                                }
                            } else if (exportColumnAnn != null && exportColumnAnn.dateTime()) {
                                long timeValue = BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toLongValue();
                                if (String.valueOf(timeValue).length() >= DateTimeUtils.UTC_LENGTH) {
                                    cellValue = DateTimeUtils.formatTime(timeValue, DateTimeUtils.YYYY_MM_DD_HH_MM_SS);
                                } else {
                                    cellValue = StringUtils.EMPTY;
                                }
                            } else if (exportColumnAnn != null && exportColumnAnn.dataRange().length > 0) {
                                Object dataRangeValue = objectBeanWrapper.getValue(fieldName);
                                if (dataRangeValue != null) {
                                    int position = BlurObject.bind(dataRangeValue).toIntValue();
                                    if (position >= 0 && position < exportColumnAnn.dataRange().length) {
                                        cellValue = exportColumnAnn.dataRange()[position];
                                    } else {
                                        cellValue = String.valueOf(position);
                                    }
                                } else {
                                    cellValue = StringUtils.EMPTY;
                                }
                            } else if (exportColumnAnn != null && exportColumnAnn.currency()) {
                                Object currencyValue = objectBeanWrapper.getValue(fieldName);
                                if (currencyValue != null) {
                                    cellValue = MathCalcHelper.bind(BlurObject.bind(currencyValue).toStringValue()).scale(2).divide("100").toBlurObject().toStringValue();
                                } else {
                                    cellValue = StringUtils.EMPTY;
                                }
                            } else {
                                cellValue = StringUtils.trimToEmpty(BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toStringValue());
                            }
                        } catch (Exception e) {
                            cellValue = StringUtils.trimToEmpty(BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toStringValue());
                        }
                        Cell cell = newRow.createCell(cellCount++);
                        cell.setCellValue(cellValue);
                        cell.setCellStyle(cellStyle);
                    }
                }
            }
            File tempFile = File.createTempFile("export_", "_" + index + ".xlsx");
            tempFile.deleteOnExit();
            try (OutputStream outputStream = Files.newOutputStream(tempFile.toPath())) {
                workbook.write(outputStream);
            }
            return tempFile;
        }
    }

    private File doExportCsv(List<String> columnNames, Map<String, ExportColumn> columnsMap, int index, List<?> data, String charset) throws Exception {
        ConsoleTableBuilder tableBuilder = ConsoleTableBuilder.create(columnNames.size()).csv();
        if (!columnNames.isEmpty()) {
            columnNames.forEach(tableBuilder.addRow()::addColumn);
        }
        for (Object item : data) {
            ClassUtils.BeanWrapper<?> objectBeanWrapper = ClassUtils.wrapper(item);
            ConsoleTableBuilder.Row newRow = tableBuilder.addRow();
            for (String fieldName : objectBeanWrapper.getFieldNames()) {
                if (!excludedFieldNames.contains(fieldName)) {
                    try {
                        ExportColumn exportColumnAnn = columnsMap.get(fieldName);
                        if (exportColumnAnn != null && exportColumnAnn.excluded()) {
                            continue;
                        }
                        IExportDataRender dataRender = renders.get(fieldName);
                        if (exportColumnAnn != null && dataRender != null) {
                            String valueStr = dataRender.render(exportColumnAnn, fieldName, objectBeanWrapper.getValue(fieldName));
                            if (StringUtils.isNotBlank(valueStr)) {
                                newRow.addColumn(valueStr);
                            } else {
                                newRow.addColumn(StringUtils.trimToEmpty(BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toStringValue()));
                            }
                        } else if (exportColumnAnn != null && exportColumnAnn.dateTime()) {
                            long timeValue = BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toLongValue();
                            if (String.valueOf(timeValue).length() >= DateTimeUtils.UTC_LENGTH) {
                                newRow.addColumn(DateTimeUtils.formatTime(timeValue, DateTimeUtils.YYYY_MM_DD_HH_MM_SS));
                            } else {
                                newRow.addColumn(StringUtils.EMPTY);
                            }
                        } else if (exportColumnAnn != null && exportColumnAnn.dataRange().length > 0) {
                            Object dataRangeValue = objectBeanWrapper.getValue(fieldName);
                            if (dataRangeValue != null) {
                                int position = BlurObject.bind(dataRangeValue).toIntValue();
                                if (position >= 0 && position < exportColumnAnn.dataRange().length) {
                                    newRow.addColumn(exportColumnAnn.dataRange()[position]);
                                } else {
                                    newRow.addColumn(String.valueOf(position));
                                }
                            } else {
                                newRow.addColumn(StringUtils.EMPTY);
                            }
                        } else if (exportColumnAnn != null && exportColumnAnn.currency()) {
                            Object currencyValue = objectBeanWrapper.getValue(fieldName);
                            if (currencyValue != null) {
                                newRow.addColumn(MathCalcHelper.bind(BlurObject.bind(currencyValue).toStringValue()).scale(2).divide("100").toBlurObject().toStringValue());
                            } else {
                                newRow.addColumn(StringUtils.EMPTY);
                            }
                        } else {
                            newRow.addColumn(StringUtils.trimToEmpty(BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toStringValue()));
                        }
                    } catch (Exception e) {
                        newRow.addColumn(StringUtils.trimToEmpty(BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toStringValue()));
                    }
                }
            }
        }
        File tempFile = File.createTempFile("export_", "_" + index + ".csv");
        tempFile.deleteOnExit();
        try (OutputStream outputStream = Files.newOutputStream(tempFile.toPath())) {
            IOUtils.write(tableBuilder.toString(), outputStream, StringUtils.defaultIfBlank(charset, "GB2312"));
        }
        return tempFile;
    }

    private File doExport(String tmplFile, int index, Map<String, Object> data) throws Exception {
        try (InputStream resourceAsStream = ExcelFileExportHelper.class.getResourceAsStream(tmplFile + ".xls")) {
            File tempFile = File.createTempFile("export_", "_" + index + ".xls");
            tempFile.deleteOnExit();
            try (OutputStream fileOutputStream = Files.newOutputStream(tempFile.toPath())) {
                JxlsHelper.getInstance().processTemplate(resourceAsStream, fileOutputStream, new Context(data));
            }
            return tempFile;
        }
    }

    /**
     * @param tmplFile 模板文件名称
     * @return 将导出数据映射到tmplFile指定的Excel文件模板
     * @throws Exception 可能产生的任何异常
     */
    public File export(String tmplFile) throws Exception {
        if (StringUtils.isBlank(tmplFile)) {
            throw new NullArgumentException("tmplFile");
        }
        File file = null;
        if (processor != null) {
            List<File> files = new ArrayList<>();
            for (int idx = 1; ; idx++) {
                List<?> processorData = processor.getData(idx);
                if (processorData == null || processorData.isEmpty()) {
                    break;
                }
                files.add(doExport(tmplFile, idx, Collections.singletonMap("data", processorData)));
            }
            file = toZip(files);
        } else if (!data.isEmpty()) {
            file = doExport(tmplFile, 1, data);
        }
        return file;
    }
}
