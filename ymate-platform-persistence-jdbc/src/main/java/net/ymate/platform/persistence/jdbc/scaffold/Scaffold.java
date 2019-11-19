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
package net.ymate.platform.persistence.jdbc.scaffold;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.base.IResultSetHandler;
import net.ymate.platform.persistence.jdbc.query.SQL;
import net.ymate.platform.persistence.jdbc.query.Select;
import net.ymate.platform.persistence.jdbc.support.ResultSetHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/19 下午5:27
 * @since 2.1.0 Renamed by ConfigInfo.
 */
public final class Scaffold {

    private static final Log LOG = LogFactory.getLog(Scaffold.class);

    /**
     * 数据库名称
     */
    private final String dbName;

    /**
     * 数据库用户名称
     */
    private final String dbUserName;

    /**
     * 生成代码基准包名称
     */
    private String packageName;

    /**
     * 生成文件输出路径
     */
    private String outputPath;

    /**
     * 待生成数据库表名称集合
     */
    private final List<String> tableNames;

    /**
     * 排除的数据库表名称集合
     */
    private final List<String> excludedTableNames;

    /**
     * 数据库表名称前缀字符串集合
     */
    private final List<String> tablePrefixes;

    /**
     * 是否移除表名称前缀
     */
    private boolean removePrefix;

    /**
     * 是否使用/继承实体模型接口抽象实现类
     */
    private boolean baseEntity;

    /**
     * 是否使用类名后缀
     */
    private boolean classSuffix;

    /**
     * 是否使用链式调用模式
     */
    private boolean chainMode;

    /**
     * 是否使用类成员属性值状态变化注解
     */
    private boolean stateSupport;

    /**
     * 实体及属性命名过滤器
     */
    private INamedFilter namedFilter;

    /**
     * 只读字段名称集合
     */
    private final List<String> readonlyColumns;

    public Scaffold(String dbName, String dbUserName) {
        this(dbName, dbUserName, null);
    }

    public Scaffold(String dbName, String dbUserName, INamedFilter namedFilter) {
        this(dbName, dbUserName, true, false, true, true, namedFilter);
    }

    public Scaffold(String dbName, String dbUserName, boolean baseEntity, boolean classSuffix, boolean chainMode, boolean stateSupport, INamedFilter namedFilter) {
        this(dbName, dbUserName, null, null, null, null, null, false, baseEntity, classSuffix, chainMode, stateSupport, namedFilter, null);
    }

    public Scaffold(String dbName, String dbUserName, String packageName, String outputPath, List<String> tableNames, List<String> excludedTableNames, List<String> tablePrefixes, boolean removePrefix, boolean baseEntity, boolean classSuffix, boolean chainMode, boolean stateSupport, INamedFilter namedFilter, List<String> readonlyColumns) {
        this.dbName = dbName;
        this.dbUserName = dbUserName;
        this.packageName = StringUtils.defaultIfBlank(packageName, "packages");
        this.outputPath = RuntimeUtils.replaceEnvVariable(StringUtils.defaultIfBlank(outputPath, "${root}"));
        this.tableNames = tableNames != null ? tableNames : new ArrayList<>();
        this.excludedTableNames = excludedTableNames != null ? excludedTableNames : new ArrayList<>();
        this.tablePrefixes = tablePrefixes != null ? tablePrefixes : new ArrayList<>();
        this.removePrefix = removePrefix;
        this.baseEntity = baseEntity;
        this.classSuffix = classSuffix;
        this.chainMode = chainMode;
        this.stateSupport = stateSupport;
        this.namedFilter = namedFilter;
        this.readonlyColumns = readonlyColumns != null ? readonlyColumns : new ArrayList<>();
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbUserName() {
        return dbUserName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Scaffold packageName(String packageName) {
        if (StringUtils.isNotBlank(packageName)) {
            this.packageName = packageName;
        }
        return this;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public Scaffold outputPath(String outputPath) {
        if (StringUtils.isNotBlank(outputPath)) {
            this.outputPath = RuntimeUtils.replaceEnvVariable(outputPath);
        }
        return this;
    }

    public List<String> getTableNames() {
        return Collections.unmodifiableList(tableNames);
    }

    public Scaffold addTableName(String tableName) {
        if (StringUtils.isNotBlank(tableName) && !tableNames.contains(tableName)) {
            tableNames.add(tableName);
        }
        return this;
    }

    public List<String> getExcludedTableNames() {
        return Collections.unmodifiableList(excludedTableNames);
    }

    public Scaffold addExcludedTableName(String tableName) {
        if (StringUtils.isNotBlank(tableName) && !excludedTableNames.contains(tableName)) {
            excludedTableNames.add(tableName);
        }
        return this;
    }

    public List<String> getTablePrefixes() {
        return Collections.unmodifiableList(tablePrefixes);
    }

    public Scaffold addTablePrefix(String prefix) {
        if (StringUtils.isNotBlank(prefix) && !tablePrefixes.contains(prefix)) {
            tablePrefixes.add(prefix);
        }
        return this;
    }

    public boolean isRemovePrefix() {
        return removePrefix;
    }

    public Scaffold removePrefix(boolean removePrefix) {
        this.removePrefix = removePrefix;
        return this;
    }

    public boolean isBaseEntity() {
        return baseEntity;
    }

    public Scaffold baseEntity(boolean baseEntity) {
        this.baseEntity = baseEntity;
        return this;
    }

    public boolean isClassSuffix() {
        return classSuffix;
    }

    public Scaffold classSuffix(boolean classSuffix) {
        this.classSuffix = classSuffix;
        return this;
    }

    public boolean isChainMode() {
        return chainMode;
    }

    public Scaffold chainMode(boolean chainMode) {
        this.chainMode = chainMode;
        return this;
    }

    public boolean isStateSupport() {
        return stateSupport;
    }

    public Scaffold stateSupport(boolean stateSupport) {
        this.stateSupport = stateSupport;
        return this;
    }

    public INamedFilter getNamedFilter() {
        return namedFilter;
    }

    public Scaffold namedFilter(INamedFilter namedFilter) {
        this.namedFilter = namedFilter;
        return this;
    }

    public List<String> getReadonlyColumns() {
        return Collections.unmodifiableList(readonlyColumns);
    }

    public Scaffold addReadonlyColumn(String columnName) {
        if (StringUtils.isNotBlank(columnName) && !readonlyColumns.contains(columnName)) {
            readonlyColumns.add(columnName);
        }
        return this;
    }

    public List<TableInfo> getTables(IDatabase owner, boolean view) throws Exception {
        return getTables(owner, owner.getConfig().getDefaultDataSourceName(), view);
    }

    public List<TableInfo> getTables(IDatabase owner, String dataSourceName, boolean view) throws Exception {
        Scaffold scaffold = this;
        List<TableInfo> tables = owner.openSession(dataSourceName, session -> {
            String dbType = session.getConnectionHolder().getDialect().getName();
            SQL sql;
            switch (dbType) {
                case Type.DATABASE.MYSQL:
                    sql = SQL.create("SHOW FULL TABLES WHERE table_type =? ").param(view ? "VIEW" : "BASE TABLE");
                    break;
                case Type.DATABASE.ORACLE:
                    sql = Select.create(view ? "USER_VIEWS" : "USER_TABLES").field(view ? "VIEW_NAME" : "TABLE_NAME").toSQL();
                    break;
                case Type.DATABASE.SQLSERVER:
                    sql = SQL.create("SELECT name FROM SYSOBJECTS WHERE xtype = ?").param(view ? "V" : "U");
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("The current database type '%s' not supported.", dbType));
            }
            List<TableInfo> tableInfos = new ArrayList<>();
            ResultSetHelper.bind(session.find(sql, IResultSetHandler.ARRAY)).forEach((wrapper, row) -> {
                String tableName = wrapper.getAsString(0);
                if (tableNames.isEmpty() || tableNames.contains(tableName)) {
                    if (excludedTableNames.isEmpty() || !excludedTableNames.contains(tableName)) {
                        tableInfos.add(TableInfo.create(session.getConnectionHolder(), scaffold, tableName, view));
                    } else if (LOG.isInfoEnabled()) {
                        LOG.info(String.format("Datatable '%s' in the excluded_table_names list, ignored.", tableName));
                    }
                }
                return true;
            });
            return tableInfos;
        });
        //
        return Collections.unmodifiableList(tables);
    }
}
