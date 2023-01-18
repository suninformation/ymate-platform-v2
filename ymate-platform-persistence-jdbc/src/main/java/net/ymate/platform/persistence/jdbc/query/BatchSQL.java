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
package net.ymate.platform.persistence.jdbc.query;

import net.ymate.platform.commons.util.ResourceUtils;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.base.impl.BatchUpdateOperator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 批量更新SQL语句及参数对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/11 下午2:25
 */
public final class BatchSQL {

    public static List<String> loadSQL(String resourceName) throws IOException {
        List<String> scripts = new ArrayList<>();
        if (StringUtils.isNotBlank(resourceName)) {
            Iterator<URL> resources = ResourceUtils.getResources(resourceName, BatchSQL.class, true);
            while (resources.hasNext()) {
                try (InputStream inputStream = resources.next().openStream()) {
                    scripts.addAll(loadSQL(inputStream));
                }
            }
        }
        return scripts;
    }

    public static List<String> loadSQL(File file) throws IOException {
        if (file != null && file.exists()) {
            try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                return loadSQL(inputStream);
            }
        }
        return Collections.emptyList();
    }

    public static List<String> loadSQL(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            String[] scripts = StringUtils.split(IOUtils.toString(inputStream, StandardCharsets.UTF_8), ";");
            if (!ArrayUtils.isEmpty(scripts)) {
                return Arrays.stream(scripts).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    public static int execSQL(List<String> scripts) throws Exception {
        return execSQL(JDBC.get(), null, scripts);
    }

    public static int execSQL(String dataSourceName, List<String> scripts) throws Exception {
        return execSQL(JDBC.get(), dataSourceName, scripts);
    }

    public static int execSQL(IDatabase database, List<String> scripts) throws Exception {
        return execSQL(database, null, scripts);
    }

    public static int execSQL(IDatabase database, String dataSourceName, List<String> scripts) throws Exception {
        int effectCount = 0;
        if (scripts != null && !scripts.isEmpty()) {
            BatchSQL batchSQL = BatchSQL.create(database);
            scripts.forEach(batchSQL::addSQL);
            if (!batchSQL.getSQLs().isEmpty()) {
                effectCount = BatchUpdateOperator.parseEffectCounts(batchSQL.execute(StringUtils.isNotBlank(dataSourceName) ? dataSourceName : database.getConfig().getDefaultDataSourceName()));
            }
        }
        return effectCount;
    }

    private final IDatabase owner;

    private final String batchSql;

    private final List<Params> params = new ArrayList<>();

    private final List<String> sqls = new ArrayList<>();

    public static BatchSQL create(String batchSql) {
        return new BatchSQL(JDBC.get(), batchSql);
    }

    public static BatchSQL create(IDatabase owner, String batchSql) {
        return new BatchSQL(owner, batchSql);
    }

    public static BatchSQL create() {
        return new BatchSQL(JDBC.get(), null);
    }

    public static BatchSQL create(IDatabase owner) {
        return new BatchSQL(owner, null);
    }

    public static BatchSQL create(Insert insert) {
        return new BatchSQL(insert.owner(), insert.toString());
    }

    public static BatchSQL create(Update update) {
        return new BatchSQL(update.owner(), update.toString());
    }

    public static BatchSQL create(Delete delete) {
        return new BatchSQL(delete.owner(), delete.toString());
    }

    public BatchSQL(IDatabase owner, String batchSql) {
        this.owner = owner;
        this.batchSql = batchSql;
    }

    public IDatabase owner() {
        return owner;
    }

    public List<Params> params() {
        return this.params;
    }

    public String getSQL() {
        return this.batchSql;
    }

    public List<String> getSQLs() {
        return this.sqls;
    }

    public BatchSQL addParameter(Params param) {
        if (StringUtils.isBlank(batchSql)) {
            // 构造未设置SQL时将不支持添加批量参数
            throw new UnsupportedOperationException();
        }
        params.add(param);
        return this;
    }

    public BatchSQL addSQL(String sql) {
        this.sqls.add(sql);
        return this;
    }

    public int[] execute() throws Exception {
        return owner.openSession(session -> session.executeForUpdate(this));
    }

    public int[] execute(String dataSourceName) throws Exception {
        return owner.openSession(dataSourceName, session -> session.executeForUpdate(this));
    }
}
