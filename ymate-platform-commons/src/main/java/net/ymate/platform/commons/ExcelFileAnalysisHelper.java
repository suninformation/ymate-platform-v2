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

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.*;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Excel文件数据导入助手类
 *
 * @author 刘镇 (suninformation@163.com) on 2018/5/25 上午6:37
 */
public class ExcelFileAnalysisHelper implements Closeable {

    private final Workbook workbook;

    private final String[] sheetNames;

    public static ExcelFileAnalysisHelper bind(File file) throws IOException {
        return new ExcelFileAnalysisHelper(new FileInputStream(file));
    }

    public static ExcelFileAnalysisHelper bind(InputStream inputStream) throws IOException {
        return new ExcelFileAnalysisHelper(inputStream);
    }

    private ExcelFileAnalysisHelper(InputStream inputStream) throws IOException {
        workbook = WorkbookFactory.create(inputStream);
        sheetNames = new String[workbook.getNumberOfSheets()];
        //
        IntStream.range(0, sheetNames.length).forEachOrdered(idx -> sheetNames[idx] = workbook.getSheetName(idx));
    }

    /**
     * @return 返回SHEET名称集合
     */
    public String[] getSheetNames() {
        return sheetNames;
    }

    public <T> List<T> openSheet(int sheetIdx, ISheetHandler<T> handler) throws Exception {
        return handler.handle(workbook.getSheetAt(sheetIdx));
    }

    public List<Object[]> openSheet(int sheetIdx) throws Exception {
        return openSheet(sheetIdx, new ISheetHandler.Default());
    }

    public <T> List<T> openSheet(String sheetName, ISheetHandler<T> handler) throws Exception {
        return handler.handle(workbook.getSheet(sheetName));
    }

    public List<Object[]> openSheet(String sheetName) throws Exception {
        return openSheet(sheetName, new ISheetHandler.Default());
    }

    @Override
    public void close() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
    }
}
