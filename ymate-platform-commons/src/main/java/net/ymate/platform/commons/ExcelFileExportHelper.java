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
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;

import java.io.*;
import java.lang.reflect.Field;
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
        return export(dataType, null);
    }

    public File export(Class<?> dataType, String charset) throws Exception {
        File file = null;
        if (processor != null) {
            List<File> files = new ArrayList<>();
            for (int idx = 1; ; idx++) {
                Map<String, Object> processorData = processor.getData(idx);
                if (processorData == null || processorData.isEmpty()) {
                    break;
                }
                files.add(doExport(dataType, idx, processorData, charset));
            }
            file = toZip(files);
        } else if (!data.isEmpty()) {
            file = doExport(dataType, 1, data, charset);
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

    private File doExport(Class<?> dataType, int index, Map<String, Object> data, String charset) throws Exception {
        ClassUtils.BeanWrapper<?> beanWrapper = ClassUtils.wrapper(dataType);
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
            ConsoleTableBuilder tableBuilder = ConsoleTableBuilder.create(columnNames.size()).csv();
            if (!columnNames.isEmpty()) {
                columnNames.forEach(tableBuilder.addRow()::addColumn);
            }
            for (Object item : data.values()) {
                if (item instanceof Collection) {
                    for (Object obj : (Collection) item) {
                        ClassUtils.BeanWrapper<?> objectBeanWrapper = ClassUtils.wrapper(obj);
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
                                        newRow.addColumn(DateTimeUtils.formatTime(BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toLongValue(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS));
                                    } else if (exportColumnAnn != null && exportColumnAnn.dataRange().length > 0) {
                                        newRow.addColumn(exportColumnAnn.dataRange()[BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toIntValue()]);
                                    } else if (exportColumnAnn != null && exportColumnAnn.currency()) {
                                        newRow.addColumn(MathCalcHelper.bind(BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toStringValue()).scale(2).divide("100").toBlurObject().toStringValue());
                                    } else {
                                        newRow.addColumn(StringUtils.trimToEmpty(BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toStringValue()));
                                    }
                                } catch (Exception e) {
                                    newRow.addColumn(StringUtils.trimToEmpty(BlurObject.bind(objectBeanWrapper.getValue(fieldName)).toStringValue()));
                                }
                            }
                        }
                    }
                }
            }
            File tempFile = File.createTempFile("export_", "_" + index + ".csv");
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                IOUtils.write(tableBuilder.toString(), outputStream, StringUtils.defaultIfBlank(charset, "GB2312"));
            }
            return tempFile;
        }
        return null;
    }

    private File doExport(String tmplFile, int index, Map<String, Object> data) throws Exception {
        try (InputStream resourceAsStream = ExcelFileExportHelper.class.getResourceAsStream(tmplFile + ".xls")) {
            File tempFile = File.createTempFile("export_", "_" + index + ".xls");
            try (OutputStream fileOutputStream = new FileOutputStream(tempFile)) {
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
                Map<String, Object> processorData = processor.getData(idx);
                if (processorData == null || processorData.isEmpty()) {
                    break;
                }
                files.add(doExport(tmplFile, idx, processorData));
            }
            file = toZip(files);
        } else if (!data.isEmpty()) {
            file = doExport(tmplFile, 1, data);
        }
        return file;
    }
}
