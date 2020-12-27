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

import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.configuration.impl.MapSafeConfigReader;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConfig;
import net.ymate.platform.persistence.jdbc.base.impl.ArrayResultSetHandler;
import net.ymate.platform.persistence.jdbc.query.SQL;
import net.ymate.platform.persistence.jdbc.query.Select;
import net.ymate.platform.persistence.jdbc.support.ResultSetHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/19 下午5:27
 * @since 2.1.0 Renamed by ConfigInfo.
 */
public final class Scaffold {

    private static final Log LOG = LogFactory.getLog(Scaffold.class);

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(IApplication owner) {
        return builder(owner, true);
    }

    public static Builder builder(IApplication owner, boolean loadNamedFilter) {
        return builder(MapSafeConfigReader.bind(owner.getParams()), loadNamedFilter);
    }

    public static Builder builder(IConfigReader configReader) {
        return builder(configReader, true);
    }

    public static Builder builder(IConfigReader configReader, boolean loadNamedFilter) {
        return builder().useBaseEntity(configReader.getBoolean(IDatabaseConfig.PARAMS_JDBC_USE_BASE_ENTITY))
                .useClassSuffix(configReader.getBoolean(IDatabaseConfig.PARAMS_JDBC_USE_CLASS_SUFFIX))
                .classSuffix(configReader.getString(IDatabaseConfig.PARAMS_JDBC_CLASS_SUFFIX))
                .useChainMode(configReader.getBoolean(IDatabaseConfig.PARAMS_JDBC_USE_CHAIN_MODE))
                .useStateSupport(configReader.getBoolean(IDatabaseConfig.PARAMS_JDBC_USE_STATE_SUPPORT))
                .packageName(configReader.getString(IDatabaseConfig.PARAMS_JDBC_PACKAGE_NAME, "packages"))
                .outputPath(configReader.getString(IDatabaseConfig.PARAMS_JDBC_OUTPUT_PATH, "${root}/src/main/java"))
                .dbName(configReader.getString(IDatabaseConfig.PARAMS_JDBC_DB_NAME))
                .dbUserName(configReader.getString(IDatabaseConfig.PARAMS_JDBC_DB_USERNAME))
                .useRemovePrefix(configReader.getBoolean(IDatabaseConfig.PARAMS_JDBC_REMOVE_TABLE_PREFIX))
                .addTablePrefixes(configReader.getList(IDatabaseConfig.PARAMS_JDBC_TABLE_PREFIX))
                .addExcludedTableNames(configReader.getList(IDatabaseConfig.PARAMS_JDBC_TABLE_EXCLUDE_LIST))
                .addTableNames(configReader.getList(IDatabaseConfig.PARAMS_JDBC_TABLE_LIST))
                .addReadonlyColumns(configReader.getList(IDatabaseConfig.PARAMS_JDBC_READONLY_FIELD_LIST))
                .namedFilter(loadNamedFilter ? configReader.getClassImpl(IDatabaseConfig.PARAMS_JDBC_NAMED_FILTER_CLASS, INamedFilter.class) : null);
    }

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
    private final String packageName;

    /**
     * 生成文件输出路径
     */
    private final String outputPath;

    /**
     * 待生成数据库表名称集合
     */
    private final Set<String> tableNames;

    /**
     * 排除的数据库表名称集合
     */
    private final Set<String> excludedTableNames;

    /**
     * 数据库表名称前缀字符串集合
     */
    private final Set<String> tablePrefixes;

    /**
     * 是否移除表名称前缀
     */
    private final boolean useRemovePrefix;

    /**
     * 是否使用/继承实体模型接口抽象实现类
     */
    private final boolean useBaseEntity;

    /**
     * 是否使用类名后缀
     */
    private final boolean useClassSuffix;

    private final String classSuffix;

    /**
     * 是否使用链式调用模式
     */
    private final boolean useChainMode;

    /**
     * 是否使用类成员属性值状态变化注解
     */
    private final boolean useStateSupport;

    /**
     * 实体及属性命名过滤器
     */
    private final INamedFilter namedFilter;

    /**
     * 只读字段名称集合
     */
    private final Set<String> readonlyColumns;

    public Scaffold(String dbName, String dbUserName, String packageName, String outputPath, Set<String> tableNames, Set<String> excludedTableNames, Set<String> tablePrefixes, boolean useRemovePrefix, boolean useBaseEntity, boolean useClassSuffix, String classSuffix, boolean useChainMode, boolean useStateSupport, INamedFilter namedFilter, Set<String> readonlyColumns) {
        this.dbName = dbName;
        this.dbUserName = dbUserName;
        this.packageName = StringUtils.defaultIfBlank(packageName, "packages");
        this.outputPath = RuntimeUtils.replaceEnvVariable(StringUtils.defaultIfBlank(outputPath, "${root}"));
        this.tableNames = tableNames != null ? tableNames : new HashSet<>();
        this.excludedTableNames = excludedTableNames != null ? excludedTableNames : new HashSet<>();
        this.tablePrefixes = tablePrefixes != null ? tablePrefixes : new HashSet<>();
        this.useRemovePrefix = useRemovePrefix;
        this.useBaseEntity = useBaseEntity;
        this.useClassSuffix = useClassSuffix;
        this.classSuffix = StringUtils.defaultIfBlank(classSuffix, "Model");
        this.useChainMode = useChainMode;
        this.useStateSupport = useStateSupport;
        this.namedFilter = namedFilter;
        this.readonlyColumns = readonlyColumns != null ? readonlyColumns : new HashSet<>();
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

    public String getOutputPath() {
        return outputPath;
    }

    public Set<String> getTableNames() {
        return Collections.unmodifiableSet(tableNames);
    }

    public Set<String> getExcludedTableNames() {
        return Collections.unmodifiableSet(excludedTableNames);
    }

    public Set<String> getTablePrefixes() {
        return Collections.unmodifiableSet(tablePrefixes);
    }

    public boolean isUseRemovePrefix() {
        return useRemovePrefix;
    }

    public boolean isUseBaseEntity() {
        return useBaseEntity;
    }

    public boolean isUseClassSuffix() {
        return useClassSuffix;
    }

    public String getClassSuffix() {
        return classSuffix;
    }

    public boolean isUseChainMode() {
        return useChainMode;
    }

    public boolean isUseStateSupport() {
        return useStateSupport;
    }

    public INamedFilter getNamedFilter() {
        return namedFilter;
    }

    public Set<String> getReadonlyColumns() {
        return Collections.unmodifiableSet(readonlyColumns);
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
                    sql = SQL.create(owner, "SHOW FULL TABLES WHERE table_type = ?").param(view ? "VIEW" : "BASE TABLE");
                    break;
                case Type.DATABASE.ORACLE:
                    sql = Select.create(owner).from(view ? "USER_VIEWS" : "USER_TABLES").field(view ? "VIEW_NAME" : "TABLE_NAME").toSQL();
                    break;
                case Type.DATABASE.SQLSERVER:
                    sql = SQL.create(owner, "SELECT name FROM SYSOBJECTS WHERE xtype = ?").param(view ? "V" : "U");
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("The current database type '%s' not supported.", dbType));
            }
            List<TableInfo> tableInfos = new ArrayList<>();
            ResultSetHelper.bind(session.find(sql, new ArrayResultSetHandler())).forEach((wrapper, row) -> {
                String tableName = wrapper.getAsString(0);
                if (tableNames.isEmpty() || tableNames.contains(tableName) || tableNames.stream().anyMatch(tName -> StringUtils.contains(tName, "*") && StringUtils.startsWithIgnoreCase(tableName, StringUtils.substringBefore(tName, "*")))) {
                    if (doCheckTableNameNotInBlacklist(tableName)) {
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

    private void doProcessColumns(EntityInfo.Builder entityInfoBuilder, TableInfo tableInfo) {
        tableInfo.getColumns().keySet().stream()
                .filter(key -> !tableInfo.getPrimaryKeys().contains(key))
                .map(key -> tableInfo.getColumns().get(key)).forEachOrdered(columnInfo -> {
            Attr attr = Attr.build(columnInfo).setReadonly(readonlyColumns.contains(columnInfo.getColumnName()));
            entityInfoBuilder.addField(attr)
                    .addConstField(new Attr(String.class.getSimpleName(), doNamedFilter(INamedFilter.Type.COLUMN, columnInfo.getColumnName()).toUpperCase(), columnInfo.getColumnName()));
            if (!attr.isNullable()) {
                entityInfoBuilder.addNonNullableField(attr);
            }
        });
    }

    public EntityInfo buildEntityInfo(TableInfo tableInfo) {
        PairObject<String, String> names = doOptimizationNames(tableInfo.getName());
        //
        EntityInfo.Builder entityInfoBuilder = EntityInfo.builder().name(names.getKey()).tableName(names.getValue());
        //
        if (tableInfo.getPrimaryKeys().size() > 1) {
            String primaryKeyType = String.format("%sPK", names.getKey());
            entityInfoBuilder.primaryKeyType(primaryKeyType)
                    .primaryKeyName("id");
            //
            Attr primaryKeyAttr = new Attr(entityInfoBuilder.build().getPrimaryKeyType(), entityInfoBuilder.build().getPrimaryKeyName());
            entityInfoBuilder.addField(primaryKeyAttr)
                    .addNonNullableField(primaryKeyAttr);
            //
            tableInfo.getPrimaryKeys().stream()
                    .map(pkey -> tableInfo.getColumns().get(pkey))
                    .forEachOrdered(columnInfo -> entityInfoBuilder.addPrimaryKey(Attr.build(columnInfo).setReadonly(readonlyColumns.contains(columnInfo.getColumnName())))
                            .addConstField(new Attr(String.class.getSimpleName(), doNamedFilter(INamedFilter.Type.COLUMN, columnInfo.getColumnName()).toUpperCase(), columnInfo.getColumnName())));
        } else if (!tableInfo.getPrimaryKeys().isEmpty()) {
            String primaryKeyName = tableInfo.getPrimaryKeys().get(0);
            ColumnInfo primaryKeyColumn = tableInfo.getColumns().get(primaryKeyName);
            Attr primaryKeyAttr = Attr.build(primaryKeyColumn);
            entityInfoBuilder.primaryKeyType(primaryKeyColumn.getColumnType())
                    .primaryKeyName(StringUtils.uncapitalize(EntityMeta.propertyNameToFieldName(primaryKeyName)))
                    .addField(primaryKeyAttr)
                    .addNonNullableField(primaryKeyAttr)
                    .addConstField(new Attr(String.class.getSimpleName(), doNamedFilter(INamedFilter.Type.COLUMN, primaryKeyColumn.getColumnName()).toUpperCase(), primaryKeyColumn.getColumnName()));
        } else {
            ColumnInfo primaryKeyColumn = tableInfo.getColumns().get("id");
            Attr primaryKeyAttr = primaryKeyColumn == null ? new Attr(Serializable.class.getSimpleName(), "id", "id") : Attr.build(primaryKeyColumn);
            entityInfoBuilder.primaryKeyName("id")
                    .primaryKeyType(primaryKeyColumn == null ? Serializable.class.getSimpleName() : primaryKeyColumn.getColumnType())
                    .addField(primaryKeyAttr)
                    .addNonNullableField(primaryKeyAttr)
                    .addConstField(new Attr(String.class.getSimpleName(), "ID", "id"));
        }
        doProcessColumns(entityInfoBuilder, tableInfo);
        return entityInfoBuilder.build();
    }

    private PairObject<String, String> doOptimizationNames(String tableName) {
        String modelName = null;
        for (String prefix : tablePrefixes) {
            if (tableName.startsWith(prefix)) {
                if (useRemovePrefix) {
                    tableName = tableName.substring(prefix.length());
                }
                modelName = StringUtils.capitalize(EntityMeta.propertyNameToFieldName(doNamedFilter(INamedFilter.Type.TABLE, tableName)));
                break;
            }
        }
        if (StringUtils.isBlank(modelName)) {
            modelName = StringUtils.capitalize(EntityMeta.propertyNameToFieldName(doNamedFilter(INamedFilter.Type.TABLE, tableName)));
        }
        return PairObject.bind(modelName, tableName);
    }

    private String doNamedFilter(INamedFilter.Type type, String original) {
        if (namedFilter != null) {
            return StringUtils.defaultIfBlank(namedFilter.filter(type, original), original);
        }
        return original;
    }

    /**
     * 判断黑名单中不包含由tableName指定的表或视图名称
     *
     * @param tableName 表或视图名称
     * @return 返回true表示不在黑名单中
     */
    private boolean doCheckTableNameNotInBlacklist(String tableName) {
        if (!excludedTableNames.isEmpty()) {
            return !excludedTableNames.contains(tableName.toLowerCase()) && excludedTableNames.stream().noneMatch(excludedName -> StringUtils.contains(excludedName, "*") && StringUtils.startsWithIgnoreCase(tableName, StringUtils.substringBefore(excludedName, "*")));
        }
        return true;
    }

    public static class Builder {

        private String dbName;

        private String dbUserName;

        private String packageName;

        private String outputPath;

        private final Set<String> tableNames = new LinkedHashSet<>();

        private final Set<String> excludedTableNames = new LinkedHashSet<>();

        private final Set<String> tablePrefixes = new LinkedHashSet<>();

        private boolean useRemovePrefix;

        private boolean useBaseEntity;

        private boolean useClassSuffix;

        private String classSuffix;

        private boolean useChainMode;

        private boolean useStateSupport;

        private INamedFilter namedFilter;

        private final Set<String> readonlyColumns = new LinkedHashSet<>();

        Builder() {
        }

        public Builder dbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        public Builder dbUserName(String dbUserName) {
            this.dbUserName = dbUserName;
            return this;
        }

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder outputPath(String outputPath) {
            this.outputPath = outputPath;
            return this;
        }

        public Builder addTableName(String tableName) {
            this.tableNames.add(tableName);
            return this;
        }

        public Builder addTableNames(Collection<String> tableNames) {
            this.tableNames.addAll(tableNames);
            return this;
        }

        public Builder addExcludedTableName(String excludedTableName) {
            this.excludedTableNames.add(excludedTableName);
            return this;
        }

        public Builder addExcludedTableNames(Collection<String> excludedTableNames) {
            this.excludedTableNames.addAll(excludedTableNames);
            return this;
        }

        public Builder addTablePrefix(String tablePrefix) {
            this.tablePrefixes.add(tablePrefix);
            return this;
        }

        public Builder addTablePrefixes(Collection<String> tablePrefixes) {
            this.tablePrefixes.addAll(tablePrefixes);
            return this;
        }

        public Builder useRemovePrefix(boolean useRemovePrefix) {
            this.useRemovePrefix = useRemovePrefix;
            return this;
        }

        public Builder useBaseEntity(boolean useBaseEntity) {
            this.useBaseEntity = useBaseEntity;
            return this;
        }

        public Builder useClassSuffix(boolean useClassSuffix) {
            this.useClassSuffix = useClassSuffix;
            return this;
        }

        public Builder classSuffix(String classSuffix) {
            this.classSuffix = classSuffix;
            return this;
        }

        public Builder useChainMode(boolean useChainMode) {
            this.useChainMode = useChainMode;
            return this;
        }

        public Builder useStateSupport(boolean useStateSupport) {
            this.useStateSupport = useStateSupport;
            return this;
        }

        public Builder namedFilter(INamedFilter namedFilter) {
            this.namedFilter = namedFilter;
            return this;
        }

        public Builder addReadonlyColumns(Collection<String> readonlyColumns) {
            this.readonlyColumns.addAll(readonlyColumns);
            return this;
        }

        public Scaffold build() {
            return new Scaffold(dbName, dbUserName, packageName, outputPath, tableNames, excludedTableNames, tablePrefixes, useRemovePrefix, useBaseEntity, useClassSuffix, classSuffix, useChainMode, useStateSupport, namedFilter, readonlyColumns);
        }
    }
}
