/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.core.IConfig;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.lang.PairObject;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.persistence.base.EntityMeta;
import net.ymate.platform.persistence.jdbc.IDatabaseModuleCfg;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/19 下午5:27
 * @version 1.0
 */
public class ConfigInfo {

    private final String dbName;

    private final String dbUserName;

    private final boolean removePrefix;

    private final List<String> tablePrefixes;

    private final List<String> tableList;

    private final List<String> tableExcludeList;

    private final boolean useBaseEntity;

    private final boolean useClassSuffix;

    private final boolean useChainMode;

    private final boolean useStateSupport;

    private final IEntityNamedFilter namedFilter;

    private final List<String> readonlyFields;

    private final String packageName;

    private final String outputPath;

    public ConfigInfo(YMP owner) {
        IConfig _config = owner.getConfig();
        //
        this.useBaseEntity = BlurObject.bind(_config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_USE_BASE_ENTITY)).toBooleanValue();
        this.useClassSuffix = BlurObject.bind(_config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_USE_CLASS_SUFFIX)).toBooleanValue();
        this.useChainMode = BlurObject.bind(_config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_USE_CHAIN_MODE)).toBooleanValue();
        this.useStateSupport = BlurObject.bind(_config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_USE_STATE_SUPPORT)).toBooleanValue();
        this.packageName = _config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_PACKAGE_NAME, "packages");
        this.outputPath = RuntimeUtils.replaceEnvVariable(owner.getConfig().getParam(IDatabaseModuleCfg.PARAMS_JDBC_OUTPUT_PATH, "${root}"));
        this.dbName = _config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_DB_NAME);
        this.dbUserName = _config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_DB_USERNAME);
        this.tablePrefixes = Arrays.asList(StringUtils.split(_config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_TABLE_PREFIX, StringUtils.EMPTY), '|'));
        this.removePrefix = new BlurObject(_config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_REMOVE_TABLE_PREFIX)).toBooleanValue();
        this.tableExcludeList = Arrays.asList(StringUtils.split(_config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_TABLE_EXCLUDE_LIST, StringUtils.EMPTY).toLowerCase(), "|"));
        this.tableList = Arrays.asList(StringUtils.split(_config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_TABLE_LIST, StringUtils.EMPTY), "|"));
        //
        this.namedFilter = ClassUtils.impl(_config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_NAMED_FILTER_CLASS), IEntityNamedFilter.class, this.getClass());
        this.readonlyFields = Arrays.asList(StringUtils.split(_config.getParam(IDatabaseModuleCfg.PARAMS_JDBC_READONLY_FIELD_LIST, StringUtils.EMPTY).toLowerCase(), '|'));
    }

    public ConfigInfo(String dbName, String dbUserName, boolean removePrefix, List<String> tablePrefixes, List<String> tableList, List<String> tableExcludeList, boolean useBaseEntity, boolean useClassSuffix, boolean useChainMode, boolean useStateSupport, IEntityNamedFilter namedFilter, List<String> readonlyFields, String packageName, String outputPath) {
        this.dbName = dbName;
        this.dbUserName = dbUserName;
        this.removePrefix = removePrefix;
        this.tablePrefixes = tablePrefixes;
        this.tableList = tableList;
        this.tableExcludeList = tableExcludeList;
        this.useBaseEntity = useBaseEntity;
        this.useClassSuffix = useClassSuffix;
        this.useChainMode = useChainMode;
        this.useStateSupport = useStateSupport;
        this.namedFilter = namedFilter;
        this.readonlyFields = readonlyFields;
        this.packageName = StringUtils.defaultIfBlank(packageName, "packages");
        this.outputPath = RuntimeUtils.replaceEnvVariable(StringUtils.defaultIfBlank(outputPath, "${root}"));
    }

    public PairObject<String, String> buildNamePrefix(String tableName) {
        String _modelName = null;
        for (String _prefix : tablePrefixes) {
            if (tableName.startsWith(_prefix)) {
                if (removePrefix) {
                    tableName = tableName.substring(_prefix.length());
                }
                _modelName = StringUtils.capitalize(EntityMeta.propertyNameToFieldName(namedFilter(tableName)));
                break;
            }
        }
        if (StringUtils.isBlank(_modelName)) {
            _modelName = StringUtils.capitalize(EntityMeta.propertyNameToFieldName(namedFilter(tableName)));
        }
        return new PairObject<String, String>(_modelName, tableName);
    }

    public String namedFilter(String original) {
        if (this.namedFilter != null) {
            return StringUtils.defaultIfBlank(this.namedFilter.doFilter(original), original);
        }
        return original;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> _returnValue = ClassUtils.wrapper(this).toMap(new ClassUtils.IFieldValueFilter() {

            private final List<String> __fields = Arrays.asList("packageName", "useBaseEntity", "useClassSuffix", "useChainMode", "useStateSupport");

            @Override
            public boolean filter(String fieldName, Object fieldValue) {
                return !__fields.contains(fieldName);
            }
        });
        _returnValue.put("lastUpdateTime", new Date());
        //
        return _returnValue;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbUserName() {
        return dbUserName;
    }

    public boolean isRemovePrefix() {
        return removePrefix;
    }

    public List<String> getTablePrefixes() {
        return tablePrefixes;
    }

    public List<String> getTableList() {
        return tableList;
    }

    public List<String> getTableExcludeList() {
        return tableExcludeList;
    }

    public boolean isUseBaseEntity() {
        return useBaseEntity;
    }

    public boolean isUseClassSuffix() {
        return useClassSuffix;
    }

    public boolean isUseChainMode() {
        return useChainMode;
    }

    public boolean isUseStateSupport() {
        return useStateSupport;
    }

    public IEntityNamedFilter getNamedFilter() {
        return namedFilter;
    }

    public List<String> getReadonlyFields() {
        return readonlyFields;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getOutputPath() {
        return outputPath;
    }
}
