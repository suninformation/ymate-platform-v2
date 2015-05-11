/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.persistence.jdbc.dialect;

import net.ymate.platform.core.util.ExpressionUtils;
import net.ymate.platform.persistence.base.EntityMeta;
import net.ymate.platform.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.query.Fields;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 数据库方言接口抽象实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-30 下午01:55:13
 * @version 1.0
 */
public abstract class AbstractDialect implements IDialect {

    /**
     * 引用标识符-开始
     */
    protected String identifierQuote_begin = "";

    /**
     * 引用标识符-结束
     */
    protected String identifierQuote_end = "";

    public String wrapIdentifierQuote(String origin) {
        return identifierQuote_begin.concat(origin).concat(identifierQuote_end);
    }

    public Object[] getGeneratedKey(Statement statement) throws SQLException {
        // 检索由于执行此 Statement 对象而创建的所有自动生成的键
        List<Long> _ids = new ArrayList<Long>();
        ResultSet _keyRSet = statement.getGeneratedKeys();
        while (_keyRSet.next()) {
            _ids.add(_keyRSet.getLong(1));
        }
        return _ids.toArray();
    }

    public String getSequenceNextValSql(String sequenceName) {
        throw new UnsupportedOperationException();
    }

    public String buildPagedQuerySQL(String originSql, int page, int pageSize) {
        throw new UnsupportedOperationException();
    }

    public String buildCreateSQL(Class<? extends IEntity> entityClass, String prefix) {
        throw new UnsupportedOperationException();
    }

    public String buildDropSQL(Class<? extends IEntity> entityClass, String prefix) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param fields    字段名称集合
     * @param suffix    字段名称后缀，可选
     * @param separator 分隔符，可选，默认“, ”
     * @return 将字段名称集合转换成为采用separator分隔的字符串
     */
    protected String __doGenerateFieldsFormatStr(Fields fields, String suffix, String separator) {
        StringBuilder _fieldsSB = new StringBuilder();
        Iterator<String> _fieldsIt = fields.getFields().iterator();
        suffix = StringUtils.defaultIfBlank(suffix, "");
        separator = StringUtils.defaultIfBlank(separator, ", ");
        while (_fieldsIt.hasNext()) {
            _fieldsSB.append(this.wrapIdentifierQuote(_fieldsIt.next())).append(suffix);
            if (_fieldsIt.hasNext()) {
                _fieldsSB.append(separator);
            }
        }
        return _fieldsSB.toString();
    }

    public String buildTableName(String prefix, String entityName) {
        return this.wrapIdentifierQuote(StringUtils.defaultIfBlank(prefix, "").concat(entityName));
    }

    /**
     * 验证字段是否合法有效
     *
     * @param entityMeta    数据实体属性描述对象
     * @param fields        字段名称集合
     * @param isPrimaryKeys fields中存放的是否为主键
     */
    protected void __doValidProperty(EntityMeta entityMeta, Fields fields, boolean isPrimaryKeys) {
        if (isPrimaryKeys) {
            for (String _pkField : fields.getFields()) {
                if (!entityMeta.isPrimaryKey(_pkField)) {
                    throw new IllegalArgumentException("'".concat(_pkField).concat("' isn't primary key field"));
                }
            }
        } else {
            for (String _field : fields.getFields()) {
                if (!entityMeta.containsProperty(_field)) {
                    throw new IllegalArgumentException("'".concat(_field).concat("' isn't table field"));
                }
            }
        }
    }

    public String buildInsertSQL(Class<? extends IEntity> entityClass, String prefix, Fields fields) {
        EntityMeta _meta = EntityMeta.createAndGet(entityClass);
        ExpressionUtils _exp = ExpressionUtils.bind("INSERT INTO ${table_name} (${fields}) VALUES (${values})")
                .set("table_name", buildTableName(prefix, _meta.getEntityName()));
        //
        Fields _fields = Fields.create();
        if (fields == null || fields.getFields().isEmpty()) {
            _fields.addAll(_meta.getPropertyNames());
        } else {
            _fields.add(fields);
            __doValidProperty(_meta, _fields, false);
        }
        return _exp.set("fields", __doGenerateFieldsFormatStr(_fields, null, null)).set("values", StringUtils.repeat("?", ", ", _fields.getFields().size())).getResult();
    }

    public String buildDeleteByPkSQL(Class<? extends IEntity> entityClass, String prefix, Fields pkFields) {
        EntityMeta _meta = EntityMeta.createAndGet(entityClass);
        ExpressionUtils _exp = ExpressionUtils.bind("DELETE FROM ${table_name} WHERE ${pk}")
                .set("table_name", buildTableName(prefix, _meta.getEntityName()));
        //
        Fields _fields = Fields.create();
        if (pkFields == null || pkFields.getFields().isEmpty()) {
            _fields.addAll(_meta.getPrimaryKeys());
        } else {
            _fields.add(pkFields);
            __doValidProperty(_meta, _fields, true);
        }
        return _exp.set("pk", __doGenerateFieldsFormatStr(_fields, " = ?", " and ")).getResult();
    }

    public String buildUpdateByPkSQL(Class<? extends IEntity> entityClass, String prefix, Fields pkFields, Fields fields) {
        EntityMeta _meta = EntityMeta.createAndGet(entityClass);
        ExpressionUtils _exp = ExpressionUtils.bind("UPDATE ${table_name} SET ${fields} WHERE ${pk}")
                .set("table_name", buildTableName(prefix, _meta.getEntityName()));
        //
        Fields _fields = Fields.create();
        for (String _field : (fields == null || fields.getFields().isEmpty()) ? _meta.getPropertyNames() : fields.getFields()) {
            if (_meta.containsProperty(_field)) {
                if (_meta.isPrimaryKey(_field)) {
                    // 排除主键
                    continue;
                }
                _fields.add(_field);
            } else {
                throw new IllegalArgumentException("'".concat(_field).concat("' isn't table field"));
            }
        }
        _exp.set("fields", __doGenerateFieldsFormatStr(_fields, " = ?", null));
        //
        if (pkFields != null && !pkFields.getFields().isEmpty()) {
            _fields = pkFields;
            __doValidProperty(_meta, _fields, true);
        } else {
            _fields = Fields.create().addAll(_meta.getPrimaryKeys());
        }
        return _exp.set("pk", __doGenerateFieldsFormatStr(_fields, " = ?", " and ")).getResult();
    }

    public String buildSelectByPkSQL(Class<? extends IEntity> entityClass, String prefix, Fields pkFields, Fields fields) {
        EntityMeta _meta = EntityMeta.createAndGet(entityClass);
        ExpressionUtils _exp = ExpressionUtils.bind("SELECT ${fields} FROM ${table_name} WHERE ${pk}")
                .set("table_name", buildTableName(prefix, _meta.getEntityName()));
        //
        if (fields == null || fields.getFields().isEmpty()) {
            fields = Fields.create().addAll(_meta.getPropertyNames());
        } else {
            __doValidProperty(_meta, fields, false);
        }
        _exp.set("fields", __doGenerateFieldsFormatStr(fields, null, null));
        //
        if (pkFields != null && !pkFields.getFields().isEmpty()) {
            __doValidProperty(_meta, pkFields, true);
        } else {
            pkFields = Fields.create().addAll(_meta.getPrimaryKeys());
        }
        return _exp.set("pk", __doGenerateFieldsFormatStr(pkFields, " = ?", " and ")).getResult();
    }

    public String buildSelectSQL(Class<? extends IEntity> entityClass, String prefix, Fields fields) {
        EntityMeta _meta = EntityMeta.createAndGet(entityClass);
        ExpressionUtils _exp = ExpressionUtils.bind("SELECT ${fields} FROM ${table_name}")
                .set("table_name", buildTableName(prefix, _meta.getEntityName()));
        //
        if (fields == null || fields.getFields().isEmpty()) {
            fields = Fields.create().addAll(_meta.getPropertyNames());
        } else {
            __doValidProperty(_meta, fields, false);
        }
        return _exp.set("fields", __doGenerateFieldsFormatStr(fields, null, null)).getResult();
    }
}
