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
import net.ymate.platform.commons.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.poi.ss.usermodel.*;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/12/25 下午2:42
 */
public final class ExcelFileExportHelper {

    private static final String EXCEL_TYPE_XLS = "xls";

    private static final String EXCEL_TYPE_XLSX = "xlsx";

    private static final String DATA_KEY = "data";

    private Map<String, Object> data;

    private final Map<String, String> customFieldNames = new HashMap<>();

    private final List<String> excludedFieldNames = new ArrayList<>();

    private final Map<Class<? extends IExportDataRender>, IExportDataRender> rendersCache = new HashMap<>();

    private final Map<String, IExportDataRender> renders = new HashMap<>();

    private IExportDataProcessor processor;

    private String prefix;

    private boolean firstCellAsIndex;

    @Deprecated
    public static ExcelFileExportHelper bind() {
        return new ExcelFileExportHelper();
    }

    public static ExcelFileExportHelper bind(Map<String, Object> data) {
        return new ExcelFileExportHelper(data);
    }

    /**
     * @since 2.1.4
     */
    public static ExcelFileExportHelper bind(List<?> data) {
        return new ExcelFileExportHelper(data);
    }

    public static ExcelFileExportHelper bind(IExportDataProcessor processor) {
        return new ExcelFileExportHelper(processor);
    }

    @Deprecated
    private ExcelFileExportHelper() {
        data = new HashMap<>();
    }

    private ExcelFileExportHelper(Map<String, Object> data) {
        if (data == null) {
            throw new NullArgumentException("data");
        }
        this.data = data;
    }

    /**
     * @since 2.1.4
     */
    private ExcelFileExportHelper(List<?> data) {
        if (data == null) {
            throw new NullArgumentException("data");
        }
        this.data = new HashMap<>();
        this.data.put(DATA_KEY, data);
    }

    private ExcelFileExportHelper(IExportDataProcessor processor) {
        if (processor == null) {
            throw new NullArgumentException("processor");
        }
        this.processor = processor;
    }

    @Deprecated
    public ExcelFileExportHelper excludedFieldNames(String[] fieldNames) {
        if (fieldNames != null && fieldNames.length > 0) {
            excludedFieldNames.addAll(Arrays.asList(fieldNames));
        }
        return this;
    }

    @Deprecated
    public ExcelFileExportHelper putFieldRender(String fieldName, IExportDataRender render) {
        if (StringUtils.isNotBlank(fieldName) && render != null) {
            renders.put(fieldName, render);
        }
        return this;
    }

    @Deprecated
    public ExcelFileExportHelper putCustomFieldName(String name, String customFieldName) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(customFieldName)) {
            customFieldNames.put(name, customFieldName);
        }
        return this;
    }

    @Deprecated
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

    /**
     * @since 2.1.3
     */
    public ExcelFileExportHelper prefix(String prefix) {
        this.prefix = prefix;
        if (StringUtils.isNotBlank(prefix) && !Strings.CS.endsWith(prefix, "_")) {
            this.prefix = prefix.concat("_");
        }
        return this;
    }

    /**
     * 设置第一个单元格作为索引
     *
     * @since 2.1.3
     */
    public ExcelFileExportHelper firstCellAsIndex() {
        this.firstCellAsIndex = true;
        return this;
    }

    /**
     * @since 2.1.3
     */
    public String prefix() {
        return StringUtils.defaultIfBlank(prefix, "export_");
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
        } else if (!data.isEmpty() && data.containsKey(DATA_KEY)) {
            file = doExport(dataType, 1, (List<?>) data.get(DATA_KEY), charset, xssf);
        }
        return file;
    }

    private File toZip(List<File> files) throws IOException {
        if (!files.isEmpty()) {
            if (files.size() == 1) {
                return files.get(0);
            }
            return FileUtils.toZip(prefix(), true, files.toArray(new File[0]));
        }
        return null;
    }

    private File doExport(Class<?> dataType, int index, List<?> data, String charset, boolean xssf) throws Exception {
        ClassUtils.BeanWrapper<?> beanWrapper = ClassUtils.wrapperClass(dataType);
        if (beanWrapper != null) {
            Collection<Field> fields = beanWrapper.getFields();
            Map<String, ExportColumnMeta> columnMetaMap = new LinkedHashMap<>(fields.size());
            List<String> headerNames = new ArrayList<>();
            fields.stream().filter(field -> !excludedFieldNames.contains(field.getName()) && field.isAnnotationPresent(ExportColumn.class))
                    .sorted(Comparator.comparingInt(field -> field.getAnnotation(ExportColumn.class).order()))
                    .forEachOrdered(field -> {
                        ExportColumn exportColumnAnn = field.getAnnotation(ExportColumn.class);
                        if (!exportColumnAnn.excluded()) {
                            if (!exportColumnAnn.render().equals(IExportDataRender.class)) {
                                IExportDataRender dataRender = rendersCache.computeIfAbsent(exportColumnAnn.render(), aClass -> ClassUtils.impl(exportColumnAnn.render(), IExportDataRender.class));
                                if (dataRender != null) {
                                    renders.put(field.getName(), dataRender);
                                }
                            }
                            String colName = StringUtils.defaultIfBlank(exportColumnAnn.value(), field.getName());
                            headerNames.add(customFieldNames.getOrDefault(colName, colName));
                            columnMetaMap.put(field.getName(), ExportColumnMeta.create(exportColumnAnn));
                        }
                    });
            if (xssf) {
                return doExportExcel(headerNames, columnMetaMap, index, data);
            } else {
                return doExportCsv(headerNames, columnMetaMap, index, data, charset);
            }
        }
        return null;
    }

    private CellStyle doCreateCellStyle(Workbook workbook, boolean alignCenter, boolean bgColor, boolean bold) {
        CellStyle cellStyle = workbook.createCellStyle();
        if (bgColor) {
            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        cellStyle.setAlignment(alignCenter ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        if (bold) {
            Font font = workbook.createFont();
            font.setBold(true);
            cellStyle.setFont(font);
        }
        return cellStyle;
    }

    private void doSetCellDateFormat(Workbook workbook, CellStyle cellStyle, ExportColumnMeta columnMeta) {
        cellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(StringUtils.defaultIfBlank(columnMeta.getPattern(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS)));
    }

    private CellStyle doGetOrCreateCellStyleIfNeed(Workbook workbook, CellStyle[] cellStyles, ExportColumnMeta columnMeta, int cellCount, boolean alignCenter, boolean isDate, boolean isNumber) {
        CellStyle cellStyle = cellStyles[cellCount - 1];
        if (cellStyle == null) {
            cellStyle = doCreateCellStyle(workbook, alignCenter, false, false);
            if (isDate) {
                doSetCellDateFormat(workbook, cellStyle, columnMeta);
            } else if (isNumber) {
                cellStyle.setDataFormat(workbook.createDataFormat().getFormat(columnMeta.getDecimals() > 0 ? String.format("0.%s_ ", StringUtils.repeat("0", columnMeta.getDecimals())) : "0_ "));
            }
            cellStyles[cellCount - 1] = cellStyle;
        }
        return cellStyle;
    }

    /**
     * 以Excel格式导出数据
     *
     * @param headerNames   列头名称集合
     * @param columnMetaMap 列元数据映射
     * @return 返回导出的Excel文件或压缩文件
     * @throws Exception 可能产生的任务异常
     * @since 2.1.4
     */
    public File exportExcel(List<String> headerNames, Map<String, ExportColumnMeta> columnMetaMap) throws Exception {
        File file = null;
        if (processor != null) {
            List<File> files = new ArrayList<>();
            for (int idx = 1; ; idx++) {
                List<?> processorData = processor.getData(idx);
                if (processorData == null || processorData.isEmpty()) {
                    break;
                }
                files.add(doExportExcel(headerNames, columnMetaMap, idx, processorData));
            }
            file = toZip(files);
        } else if (!data.isEmpty() && data.containsKey(DATA_KEY)) {
            file = doExportExcel(headerNames, columnMetaMap, 1, (List<?>) data.get(DATA_KEY));
        }
        return file;
    }

    private File doExportExcel(List<String> headerNames, Map<String, ExportColumnMeta> columnMetaMap, int index, List<?> data) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(true)) {
            Sheet sheet = workbook.createSheet();
            CellStyle headCellStyle = doCreateCellStyle(workbook, true, true, true);
            //
            int rowCount = 0;
            Row head = sheet.createRow(rowCount++);
            for (int i = 0; i < headerNames.size(); i++) {
                Cell cell = head.createCell(i);
                cell.setCellValue(headerNames.get(i));
                cell.setCellStyle(headCellStyle);
            }
            CellStyle[] cellStyles = new CellStyle[columnMetaMap.size()];
            for (Object item : data) {
                Row newRow = sheet.createRow(rowCount++);
                int cellCount = 0;
                //
                ClassUtils.BeanWrapper<?> objectBeanWrapper = null;
                if (!(item instanceof Map)) {
                    objectBeanWrapper = ClassUtils.wrapper(item);
                }
                for (Map.Entry<String, ExportColumnMeta> columnEntry : columnMetaMap.entrySet()) {
                    String fieldName = columnEntry.getKey();
                    ExportColumnMeta exportColumnMeta = columnEntry.getValue();
                    //
                    Cell cell = newRow.createCell(cellCount++);
                    boolean firstCellFlag = firstCellAsIndex && cellCount == 1;
                    if (firstCellFlag) {
                        cell.setCellStyle(doGetOrCreateCellStyleIfNeed(workbook, cellStyles, exportColumnMeta, cellCount, true, false, false));
                        cell.setCellValue(rowCount - 1);
                        continue;
                    }
                    Object cellValue = null;
                    Object originValue = null;
                    if (objectBeanWrapper != null) {
                        objectBeanWrapper = ClassUtils.wrapper(item);
                        originValue = objectBeanWrapper.getValue(fieldName);
                    } else if (item instanceof Map) {
                        originValue = ((Map<?, ?>) item).get(fieldName);
                    }
                    try {
                        IExportDataRender dataRender = renders.get(fieldName);
                        if (dataRender != null) {
                            cellValue = dataRender.render(objectBeanWrapper == null ? item : objectBeanWrapper, exportColumnMeta, fieldName, originValue, false);
                            if (cellValue == null) {
                                cellValue = originValue;
                            }
                        } else if (exportColumnMeta.isDateTime()) {
                            cellValue = originValue;
                            if (cellValue instanceof Number) {
                                long timeValue = BlurObject.bind(cellValue).toLongValue();
                                cellValue = DateTimeHelper.bind(timeValue).time();
                            } else if (cellValue instanceof String) {
                                cellValue = DateTimeHelper.bind(cellValue.toString(), StringUtils.defaultIfBlank(exportColumnMeta.getPattern(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS)).time();
                            }
                        } else if (ArrayUtils.isNotEmpty(exportColumnMeta.getDataRange())) {
                            if (originValue instanceof Number || originValue instanceof String) {
                                int position = BlurObject.bind(originValue).toIntValue();
                                if (position >= 0 && position < exportColumnMeta.getDataRange().length) {
                                    cellValue = exportColumnMeta.getDataRange()[position];
                                } else {
                                    cellValue = BlurObject.bind(originValue).toStringValue();
                                }
                            }
                        } else if (exportColumnMeta.isCurrency()) {
                            if (originValue != null) {
                                cellValue = doProcessCurrencyValue(exportColumnMeta, originValue).toDoubleValue();
                            } else {
                                cellValue = 0;
                            }
                        } else {
                            cellValue = originValue;
                        }
                    } catch (Exception e) {
                        cellValue = BlurObject.bind(originValue).toStringValue();
                    }
                    if (cellValue instanceof Date) {
                        cell.setCellStyle(doGetOrCreateCellStyleIfNeed(workbook, cellStyles, exportColumnMeta, cellCount, false, true, false));
                        cell.setCellValue((Date) cellValue);
                    } else if (cellValue instanceof LocalDate) {
                        cell.setCellStyle(doGetOrCreateCellStyleIfNeed(workbook, cellStyles, exportColumnMeta, cellCount, false, true, false));
                        cell.setCellValue((LocalDate) cellValue);
                    } else if (cellValue instanceof Calendar) {
                        cell.setCellStyle(doGetOrCreateCellStyleIfNeed(workbook, cellStyles, exportColumnMeta, cellCount, false, true, false));
                        cell.setCellValue((Calendar) cellValue);
                    } else if (cellValue instanceof LocalDateTime) {
                        cell.setCellStyle(doGetOrCreateCellStyleIfNeed(workbook, cellStyles, exportColumnMeta, cellCount, false, true, false));
                        cell.setCellValue((LocalDateTime) cellValue);
                    } else if (cellValue instanceof Number || (cellValue != null && (float.class.isAssignableFrom(cellValue.getClass())
                            || int.class.isAssignableFrom(cellValue.getClass())
                            || long.class.isAssignableFrom(cellValue.getClass())
                            || double.class.isAssignableFrom(cellValue.getClass())))) {
                        cell.setCellStyle(doGetOrCreateCellStyleIfNeed(workbook, cellStyles, exportColumnMeta, cellCount, false, false, true));
                        cell.setCellValue(BlurObject.bind(cellValue).toDoubleValue());
                    } else if (cellValue instanceof Boolean || (cellValue != null && boolean.class.isAssignableFrom(cellValue.getClass()))) {
                        cell.setCellStyle(doGetOrCreateCellStyleIfNeed(workbook, cellStyles, exportColumnMeta, cellCount, false, false, false));
                        cell.setCellValue(BlurObject.bind(cellValue).toBooleanValue());
                    } else {
                        cell.setCellStyle(doGetOrCreateCellStyleIfNeed(workbook, cellStyles, exportColumnMeta, cellCount, false, false, false));
                        cell.setCellValue(StringUtils.trimToEmpty(BlurObject.bind(cellValue).toStringValue()));
                    }
                }
            }
            File tempFile = FileUtils.createTempFile(prefix(), String.format(".%s", EXCEL_TYPE_XLSX), index);
            tempFile.deleteOnExit();
            try (OutputStream outputStream = Files.newOutputStream(tempFile.toPath())) {
                workbook.write(outputStream);
            }
            return tempFile;
        }
    }

    private BlurObject doProcessCurrencyValue(ExportColumnMeta columnMeta, Object currencyValue) {
        int decimals = columnMeta.getDecimals();
        if (decimals <= 0) {
            decimals = 2;
        }
        MathCalcHelper mathCalcHelper = MathCalcHelper.bind(BlurObject.bind(currencyValue).toStringValue()).scale(decimals);
        if (columnMeta.isAccuracy()) {
            mathCalcHelper.divide(Math.pow(10, decimals));
        } else {
            mathCalcHelper.round();
        }
        return mathCalcHelper.toBlurObject();
    }

    /**
     * 以CSV格式导出数据
     *
     * @param headerNames   列头名称集合
     * @param columnMetaMap 列元数据映射
     * @return 返回导出的CSV文件或压缩文件
     * @throws Exception 可能产生的任务异常
     * @since 2.1.4
     */
    public File exportCsv(List<String> headerNames, Map<String, ExportColumnMeta> columnMetaMap) throws Exception {
        return exportCsv(headerNames, columnMetaMap, null);
    }

    /**
     * 以CSV格式导出数据
     *
     * @param headerNames   列头名称集合
     * @param columnMetaMap 列元数据映射
     * @param charset       指定字符集
     * @return 返回导出的CSV文件或压缩文件
     * @throws Exception 可能产生的任务异常
     * @since 2.1.4
     */
    public File exportCsv(List<String> headerNames, Map<String, ExportColumnMeta> columnMetaMap, String charset) throws Exception {
        File file = null;
        if (processor != null) {
            List<File> files = new ArrayList<>();
            for (int idx = 1; ; idx++) {
                List<?> processorData = processor.getData(idx);
                if (processorData == null || processorData.isEmpty()) {
                    break;
                }
                files.add(doExportCsv(headerNames, columnMetaMap, idx, processorData, charset));
            }
            file = toZip(files);
        } else if (!data.isEmpty() && data.containsKey(DATA_KEY)) {
            file = doExportCsv(headerNames, columnMetaMap, 1, (List<?>) data.get(DATA_KEY), charset);
        }
        return file;
    }

    private File doExportCsv(List<String> headerNames, Map<String, ExportColumnMeta> columnMetaMap, int index, List<?> data, String charset) throws Exception {
        ConsoleTableBuilder tableBuilder = ConsoleTableBuilder.create(headerNames.size()).csv();
        if (!headerNames.isEmpty()) {
            headerNames.forEach(tableBuilder.addRow()::addColumn);
        }
        for (Object item : data) {
            ClassUtils.BeanWrapper<?> objectBeanWrapper = null;
            if (!(item instanceof Map)) {
                objectBeanWrapper = ClassUtils.wrapper(item);
            }
            ConsoleTableBuilder.Row newRow = tableBuilder.addRow();
            for (Map.Entry<String, ExportColumnMeta> columnEntry : columnMetaMap.entrySet()) {
                if (firstCellAsIndex && newRow.getColumns().isEmpty()) {
                    newRow.addColumn(String.valueOf(tableBuilder.getRows().size() - 1));
                    continue;
                }
                String fieldName = columnEntry.getKey();
                ExportColumnMeta exportColumnMeta = columnEntry.getValue();
                //
                Object cellValue = null;
                if (objectBeanWrapper != null) {
                    objectBeanWrapper = ClassUtils.wrapper(item);
                    cellValue = objectBeanWrapper.getValue(fieldName);
                } else if (item instanceof Map) {
                    cellValue = ((Map<?, ?>) item).get(fieldName);
                }
                try {
                    IExportDataRender dataRender = renders.get(fieldName);
                    if (dataRender != null) {
                        String valueStr = BlurObject.bind(dataRender.render(objectBeanWrapper == null ? item : objectBeanWrapper, exportColumnMeta, fieldName, cellValue, false)).toStringValue();
                        if (StringUtils.isNotBlank(valueStr)) {
                            newRow.addColumn(valueStr);
                        } else {
                            newRow.addColumn(StringUtils.trimToEmpty(BlurObject.bind(cellValue).toStringValue()));
                        }
                    } else if (exportColumnMeta.isDateTime()) {
                        long timeValue = BlurObject.bind(cellValue).toLongValue();
                        if (String.valueOf(timeValue).length() >= DateTimeUtils.UTC_LENGTH) {
                            newRow.addColumn(DateTimeUtils.formatTime(timeValue, StringUtils.defaultIfBlank(exportColumnMeta.getPattern(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS)));
                        } else {
                            newRow.addColumn(StringUtils.EMPTY);
                        }
                    } else if (ArrayUtils.isNotEmpty(exportColumnMeta.getDataRange())) {
                        if (cellValue != null) {
                            int position = BlurObject.bind(cellValue).toIntValue();
                            if (position >= 0 && position < exportColumnMeta.getDataRange().length) {
                                newRow.addColumn(exportColumnMeta.getDataRange()[position]);
                            } else {
                                newRow.addColumn(String.valueOf(position));
                            }
                        } else {
                            newRow.addColumn(StringUtils.EMPTY);
                        }
                    } else if (exportColumnMeta.isCurrency()) {
                        if (cellValue != null) {
                            newRow.addColumn(doProcessCurrencyValue(exportColumnMeta, cellValue).toStringValue());
                        } else {
                            newRow.addColumn(StringUtils.EMPTY);
                        }
                    } else {
                        newRow.addColumn(StringUtils.trimToEmpty(BlurObject.bind(cellValue).toStringValue()));
                    }
                } catch (Exception e) {
                    newRow.addColumn(StringUtils.trimToEmpty(BlurObject.bind(cellValue).toStringValue()));
                }
            }
        }
        File tempFile = FileUtils.createTempFile(prefix(), ".csv", index);
        tempFile.deleteOnExit();
        try (OutputStream outputStream = Files.newOutputStream(tempFile.toPath())) {
            IOUtils.write(tableBuilder.toString(), outputStream, StringUtils.defaultIfBlank(charset, "GB2312"));
        }
        return tempFile;
    }

    private File doExport(JxlsHelper jxlsHelper, String tmplFile, int index, Map<String, Object> data) throws IOException {
        String fileType = FileUtils.getExtName(tmplFile).toLowerCase();
        if (StringUtils.isBlank(fileType)) {
            List<String> fileTypes = Arrays.asList(EXCEL_TYPE_XLS, EXCEL_TYPE_XLSX);
            for (String type : fileTypes) {
                try (InputStream templateStream = doGetTemplateFileInputStream(String.format("%s.%s", tmplFile, type))) {
                    if (templateStream != null) {
                        return doExport(jxlsHelper, templateStream, type, index, data);
                    }
                }
            }
        } else if (Strings.CS.endsWithAny(fileType, EXCEL_TYPE_XLS, EXCEL_TYPE_XLSX)) {
            try (InputStream templateStream = doGetTemplateFileInputStream(tmplFile)) {
                return doExport(jxlsHelper, templateStream, fileType, index, data);
            }
        }
        return null;
    }

    private File doExport(JxlsHelper jxlsHelper, InputStream templateStream, String fileExtName, int index, Map<String, Object> data) throws IOException {
        File tempFile = FileUtils.createTempFile(prefix(), String.format(".%s", fileExtName), index);
        tempFile.deleteOnExit();
        try (OutputStream fileOutputStream = Files.newOutputStream(tempFile.toPath())) {
            jxlsHelper.processTemplate(templateStream, fileOutputStream, new Context(data));
        }
        return tempFile;
    }

    private InputStream doGetTemplateFileInputStream(String tmplFilePath) throws IOException {
        InputStream templateStream;
        File tmplFile = new File(RuntimeUtils.replaceEnvVariable(tmplFilePath));
        if (tmplFile.isAbsolute() && tmplFile.exists() && tmplFile.isFile()) {
            templateStream = Files.newInputStream(tmplFile.toPath());
        } else {
            templateStream = ResourceUtils.getResourceAsStream(tmplFilePath, ExcelFileExportHelper.class);
        }
        return templateStream;
    }

    /**
     * @param tmplFile 模板文件名称
     * @return 将导出数据映射到tmplFile指定的Excel文件模板
     * @throws Exception 可能产生的任何异常
     */
    public File export(String tmplFile) throws Exception {
        return export(tmplFile, JxlsHelper.getInstance());
    }

    /**
     * @param tmplFile   模板文件名称
     * @param jxlsHelper JXLS辅助类实例对象
     * @return 将导出数据映射到tmplFile指定的Excel文件模板
     * @throws Exception 可能产生的任何异常
     * @since 2.1.2
     */
    public File export(String tmplFile, JxlsHelper jxlsHelper) throws Exception {
        if (StringUtils.isBlank(tmplFile)) {
            throw new NullArgumentException("tmplFile");
        }
        if (jxlsHelper == null) {
            throw new NullArgumentException("jxlsHelper");
        }
        File file = null;
        if (processor != null) {
            List<File> files = new ArrayList<>();
            for (int idx = 1; ; idx++) {
                List<?> processorData = processor.getData(idx);
                if (processorData == null || processorData.isEmpty()) {
                    break;
                }
                files.add(doExport(jxlsHelper, tmplFile, idx, Collections.singletonMap("data", processorData)));
            }
            file = toZip(files);
        } else if (!data.isEmpty()) {
            file = doExport(jxlsHelper, tmplFile, 1, data);
        }
        return file;
    }
}
