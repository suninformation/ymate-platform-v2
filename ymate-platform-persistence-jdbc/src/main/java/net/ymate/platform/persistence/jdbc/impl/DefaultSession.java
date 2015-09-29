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
package net.ymate.platform.persistence.jdbc.impl;

import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.lang.PairObject;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.ExpressionUtils;
import net.ymate.platform.core.util.UUIDUtils;
import net.ymate.platform.persistence.base.EntityMeta;
import net.ymate.platform.persistence.base.IEntity;
import net.ymate.platform.persistence.base.IEntityPK;
import net.ymate.platform.persistence.jdbc.IConnectionHolder;
import net.ymate.platform.persistence.jdbc.ISession;
import net.ymate.platform.persistence.jdbc.ISessionEvent;
import net.ymate.platform.persistence.jdbc.base.*;
import net.ymate.platform.persistence.jdbc.base.impl.*;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import net.ymate.platform.persistence.jdbc.query.*;
import net.ymate.platform.persistence.jdbc.transaction.Transactions;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * 默认数据库会话操作接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-27 下午03:09:46
 * @version 1.0
 */
public class DefaultSession implements ISession {

    private String __id;

    private IConnectionHolder __connectionHolder;
    private IDialect __dialect;
    private String __tablePrefix;
    //
    private ISessionEvent __sessionEvent;

    public DefaultSession(IConnectionHolder connectionHolder) {
        this.__id = UUIDUtils.UUID();
        this.__connectionHolder = connectionHolder;
        //
        __dialect = connectionHolder.getDialect();
        __tablePrefix = connectionHolder.getDataSourceCfgMeta().getTablePrefix();
    }

    public String getId() {
        return __id;
    }

    public IConnectionHolder getConnectionHolder() {
        return __connectionHolder;
    }

    public ISession setSessionEvent(ISessionEvent sessionEvent) {
        this.__sessionEvent = sessionEvent;
        // TODO 会话事件有待进一步确认后处理
        return this;
    }

    public void close() {
        // 同时需要判断当前连接是否参与事务，若存在事务则不进行关闭操作
        if (__connectionHolder != null) {
            if (Transactions.get() == null) {
                __connectionHolder.release();
            }
        }
    }

    public <T> IResultSet<T> find(SQL sql, IResultSetHandler<T> handler) throws Exception {
        IQueryOperator<T> _opt = new DefaultQueryOperator<T>(sql.getSQL(), this.__connectionHolder, handler);
        for (Object _param : sql.getParams().getParams()) {
            _opt.addParameter(_param);
        }
        _opt.execute();
        return new DefaultResultSet<T>(_opt.getResultSet());
    }

    public <T> IResultSet<T> find(SQL sql, IResultSetHandler<T> handler, Page page) throws Exception {
        String _pagedSql = __dialect.buildPagedQuerySQL(sql.getSQL(), page.getPage(), page.getPageSize());
        //
        long _count = 0;
        if (page.isCount()) {
            _count = this.count(sql);
        }
        //
        IQueryOperator<T> _opt = new DefaultQueryOperator<T>(_pagedSql, this.__connectionHolder, handler);
        for (Object _param : sql.getParams().getParams()) {
            _opt.addParameter(_param);
        }
        _opt.execute();
        return new DefaultResultSet<T>(_opt.getResultSet(), page.getPage(), page.getPageSize(), _count);
    }

    @SuppressWarnings("unchecked")
    public <T extends IEntity> IResultSet<T> find(T entity) throws Exception {
        Cond _cond = Cond.create();
        EntityMeta _meta = EntityMeta.createAndGet(entity.getClass());
        ClassUtils.BeanWrapper<T> _beanWrapper = ClassUtils.wrapper(entity);
        for (String _field : _meta.getPropertyNames()) {
            Object _value = null;
            if (_meta.isMultiplePrimaryKey() && _meta.isPrimaryKey(_field)) {
                _value = _meta.getPropertyByName(_field).getField().get(entity.getId());
            } else {
                _value = _beanWrapper.getValue(_meta.getPropertyByName(_field).getField().getName());
            }
            if (_value != null) {
                _cond.and().eq(_field).param(_value);
            }
        }
        return (IResultSet<T>) this.find(EntitySQL.create(entity.getClass()), Where.create(_cond));
    }

    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity) throws Exception {
        return this.find(entity, null, null);
    }

    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Where where) throws Exception {
        return this.find(entity, where, null);
    }

    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Where where, Page page) throws Exception {
        String _selectSql = __dialect.buildSelectSQL(entity.getEntityClass(), __tablePrefix, __doGetNotExcludedFields(EntityMeta.createAndGet(entity.getEntityClass()), entity.getFields(), false, true));
        if (where != null) {
            _selectSql = _selectSql.concat(" ").concat(where.getWhereSQL()).concat(" ").concat(where.getOrderBy().getOrderBySQL());
        }
        long _count = 0;
        if (page != null) {
            _selectSql = __dialect.buildPagedQuerySQL(_selectSql, page.getPage(), page.getPageSize());
            if (page.isCount()) {
                _count = this.count(entity.getEntityClass(), where);
            }
        }
        IQueryOperator<T> _opt = new DefaultQueryOperator<T>(_selectSql, this.__connectionHolder, new EntityResultSetHandler<T>(entity.getEntityClass()));
        if (where != null) {
            for (Object _param : where.getParams().getParams()) {
                _opt.addParameter(_param);
            }
        }
        _opt.execute();
        //
        if (page != null) {
            return new DefaultResultSet<T>(_opt.getResultSet(), page.getPage(), page.getPageSize(), _count);
        }
        return new DefaultResultSet<T>(_opt.getResultSet());
    }

    public <T extends IEntity> T find(EntitySQL<T> entity, Serializable id) throws Exception {
        EntityMeta _meta = EntityMeta.createAndGet(entity.getEntityClass());
        PairObject<Fields, Params> _entityPK = __doGetPrimaryKeyFieldAndValues(_meta, id, null);
        String _selectSql = __dialect.buildSelectByPkSQL(entity.getEntityClass(), __tablePrefix, _entityPK.getKey(), __doGetNotExcludedFields(_meta, entity.getFields(), false, true));
        IQueryOperator<T> _opt = new DefaultQueryOperator<T>(_selectSql, this.__connectionHolder, new EntityResultSetHandler<T>(entity.getEntityClass()));
        if (_meta.isMultiplePrimaryKey()) {
            for (Object _param : _entityPK.getValue().getParams()) {
                _opt.addParameter(_param);
            }
        } else {
            _opt.addParameter(id);
        }
        _opt.execute();
        return _opt.getResultSet().isEmpty() ? null : _opt.getResultSet().get(0);
    }

    public <T> T findFirst(SQL sql, IResultSetHandler<T> handler) throws Exception {
        String _selectSql = __dialect.buildPagedQuerySQL(sql.getSQL(), 1, 1);
        IQueryOperator<T> _opt = new DefaultQueryOperator<T>(_selectSql, this.__connectionHolder, handler);
        for (Object _param : sql.getParams().getParams()) {
            _opt.addParameter(_param);
        }
        _opt.execute();
        return _opt.getResultSet().isEmpty() ? null : _opt.getResultSet().get(0);
    }

    public <T extends IEntity> T findFirst(EntitySQL<T> entity, Where where) throws Exception {
        String _selectSql = __dialect.buildSelectSQL(entity.getEntityClass(), __tablePrefix, __doGetNotExcludedFields(EntityMeta.createAndGet(entity.getEntityClass()), entity.getFields(), false, true));
        if (where != null) {
            _selectSql = _selectSql.concat(" ").concat(where.getWhereSQL()).concat(" ").concat(where.getOrderBy().getOrderBySQL());
        }
        _selectSql = __dialect.buildPagedQuerySQL(_selectSql, 1, 1);
        IQueryOperator<T> _opt = new DefaultQueryOperator<T>(_selectSql, this.__connectionHolder, new EntityResultSetHandler<T>(entity.getEntityClass()));
        if (where != null) {
            for (Object _param : where.getParams().getParams()) {
                _opt.addParameter(_param);
            }
        }
        _opt.execute();
        //
        return _opt.getResultSet().isEmpty() ? null : _opt.getResultSet().get(0);
    }

    public int executeForUpdate(SQL sql) throws Exception {
        IUpdateOperator _opt = new DefaultUpdateOperator(sql.getSQL(), this.getConnectionHolder());
        for (Object _param : sql.getParams().getParams()) {
            _opt.addParameter(_param);
        }
        _opt.execute();
        return _opt.getEffectCounts();
    }

    public int[] executeForUpdate(BatchSQL sql) throws Exception {
        IBatchUpdateOperator _opt = null;
        if (sql.getSQL() != null) {
            _opt = new BatchUpdateOperator(sql.getSQL(), this.getConnectionHolder());
            for (Params _param : sql.getParams()) {
                SQLBatchParameter _batchParam = SQLBatchParameter.create();
                for (Object _p : _param.getParams()) {
                    _batchParam.addParameter(_p);
                }
                _opt.addBatchParameter(_batchParam);
            }
        } else {
            _opt = new BatchUpdateOperator(this.getConnectionHolder());
        }
        for (String _sql : sql.getSQLs()) {
            _opt.addBatchSQL(_sql);
        }
        _opt.execute();
        return _opt.getEffectCounts();
    }

    public <T extends IEntity> T update(T entity, Fields filter) throws Exception {
        EntityMeta _meta = EntityMeta.createAndGet(entity.getClass());
        PairObject<Fields, Params> _entity = __doGetPrimaryKeyFieldAndValues(_meta, entity, null);
        filter = __doGetNotExcludedFields(_meta, filter, true, false);
        String _updateSql = __dialect.buildUpdateByPkSQL(entity.getClass(), __tablePrefix, _entity.getKey(), filter);
        IUpdateOperator _opt = new DefaultUpdateOperator(_updateSql, this.__connectionHolder);
        // 先获取并添加需要更新的字段值
        for (Object _param : __doGetEntityFieldAndValues(_meta, entity, filter, false).getValue().getParams()) {
            _opt.addParameter(_param);
        }
        // 再获取并添加主键条件字段值
        for (Object _param : _entity.getValue().getParams()) {
            _opt.addParameter(_param);
        }
        _opt.execute();
        return entity;
    }

    public <T extends IEntity> List<T> update(List<T> entities, Fields filter) throws Exception {
        EntityMeta _meta = EntityMeta.createAndGet(entities.get(0).getClass());
        PairObject<Fields, Params> _entity = __doGetPrimaryKeyFieldAndValues(_meta, entities.get(0), null);
        filter = __doGetNotExcludedFields(_meta, filter, true, false);
        String _updateSql = __dialect.buildUpdateByPkSQL(entities.get(0).getClass(), __tablePrefix, _entity.getKey(), filter);
        IBatchUpdateOperator _opt = new BatchUpdateOperator(_updateSql, this.__connectionHolder);
        for (T entity : entities) {
            SQLBatchParameter _batchParam = SQLBatchParameter.create();
            _entity = __doGetEntityFieldAndValues(_meta, entity, filter, false);
            // 先获取并添加需要更新的字段值
            for (Object _param : _entity.getValue().getParams()) {
                _batchParam.addParameter(_param);
            }
            // 再获取并添加主键条件字段值
            _entity = __doGetPrimaryKeyFieldAndValues(_meta, entity, null);
            for (Object _param : _entity.getValue().getParams()) {
                _batchParam.addParameter(_param);
            }
            _opt.addBatchParameter(_batchParam);
        }
        _opt.execute();
        return entities;
    }

    public <T extends IEntity> T insert(T entity) throws Exception {
        return insert(entity, null);
    }

    public <T extends IEntity> T insert(T entity, Fields filter) throws Exception {
        EntityMeta _meta = EntityMeta.createAndGet(entity.getClass());
        PairObject<Fields, Params> _entity = __doGetEntityFieldAndValues(_meta, entity, filter, true);
        String _insertSql = __dialect.buildInsertSQL(entity.getClass(), __tablePrefix, _entity.getKey());
        IUpdateOperator _opt = new DefaultUpdateOperator(_insertSql, this.__connectionHolder);
        // 获取并添加字段值
        for (Object _param : _entity.getValue().getParams()) {
            _opt.addParameter(_param);
        }
        _opt.execute();
        return entity;
    }

    public <T extends IEntity> List<T> insert(List<T> entities) throws Exception {
        return insert(entities, null);
    }

    public <T extends IEntity> List<T> insert(List<T> entities, Fields filter) throws Exception {
        EntityMeta _meta = EntityMeta.createAndGet(entities.get(0).getClass());
        PairObject<Fields, Params> _entity = __doGetEntityFieldAndValues(_meta, entities.get(0), filter, true);
        String _insertSql = __dialect.buildInsertSQL(entities.get(0).getClass(), __tablePrefix, _entity.getKey());
        IBatchUpdateOperator _opt = new BatchUpdateOperator(_insertSql, this.__connectionHolder);
        for (T entity : entities) {
            SQLBatchParameter _batchParam = SQLBatchParameter.create();
            for (Object _param : __doGetEntityFieldAndValues(_meta, entity, filter, true).getValue().getParams()) {
                _batchParam.addParameter(_param);
            }
            _opt.addBatchParameter(_batchParam);
        }
        _opt.execute();
        return entities;
    }

    public <T extends IEntity> T delete(T entity) throws Exception {
        this.delete(entity.getClass(), entity.getId());
        return entity;
    }

    public <T extends IEntity> int delete(Class<T> entityClass, Serializable id) throws Exception {
        EntityMeta _meta = EntityMeta.createAndGet(entityClass);
        PairObject<Fields, Params> _entity = __doGetPrimaryKeyFieldAndValues(_meta, id, null);
        String _deleteSql = __dialect.buildDeleteByPkSQL(entityClass, __tablePrefix, _entity.getKey());
        IUpdateOperator _opt = new DefaultUpdateOperator(_deleteSql, this.__connectionHolder);
        // 获取并添加主键条件字段值
        for (Object _param : _entity.getValue().getParams()) {
            _opt.addParameter(_param);
        }
        _opt.execute();
        return _opt.getEffectCounts();
    }

    public <T extends IEntity> List<T> delete(List<T> entities) throws Exception {
        EntityMeta _meta = EntityMeta.createAndGet(entities.get(0).getClass());
        PairObject<Fields, Params> _entity = __doGetPrimaryKeyFieldAndValues(_meta, entities.get(0), null);
        String _deleteSql = __dialect.buildDeleteByPkSQL(entities.get(0).getClass(), __tablePrefix, _entity.getKey());
        IBatchUpdateOperator _opt = new BatchUpdateOperator(_deleteSql, this.__connectionHolder);
        for (T entity : entities) {
            SQLBatchParameter _batchParam = SQLBatchParameter.create();
            // 获取并添加主键条件字段值
            _entity = __doGetPrimaryKeyFieldAndValues(_meta, entity, null);
            for (Object _param : _entity.getValue().getParams()) {
                _batchParam.addParameter(_param);
            }
            _opt.addBatchParameter(_batchParam);
        }
        _opt.execute();
        return entities;
    }

    public <T extends IEntity> int[] delete(Class<T> entityClass, Serializable[] ids) throws Exception {
        EntityMeta _meta = EntityMeta.createAndGet(entityClass);
        PairObject<Fields, Params> _entity = __doGetPrimaryKeyFieldAndValues(_meta, ids[0], null);
        String _deleteSql = __dialect.buildDeleteByPkSQL(entityClass, __tablePrefix, _entity.getKey());
        IBatchUpdateOperator _opt = new BatchUpdateOperator(_deleteSql, this.__connectionHolder);
        for (Serializable _id : ids) {
            SQLBatchParameter _batchParam = SQLBatchParameter.create();
            // 获取并添加主键条件字段值
            _entity = __doGetPrimaryKeyFieldAndValues(_meta, _id, null);
            for (Object _param : _entity.getValue().getParams()) {
                _batchParam.addParameter(_param);
            }
            _opt.addBatchParameter(_batchParam);
        }
        _opt.execute();
        return _opt.getEffectCounts();
    }

    public <T extends IEntity> long count(Class<T> entityClass, Where where) throws Exception {
        EntityMeta _meta = EntityMeta.createAndGet(entityClass);
        ExpressionUtils _exp = ExpressionUtils.bind("SELECT count(1) FROM ${table_name} ${where}")
                .set("table_name", __dialect.buildTableName(__tablePrefix, _meta.getEntityName()))
                .set("where", where.getWhereSQL());
        IQueryOperator<Object[]> _opt = new DefaultQueryOperator<Object[]>(_exp.getResult(), this.getConnectionHolder(), IResultSetHandler.ARRAY);
        for (Object _param : where.getParams().getParams()) {
            _opt.addParameter(_param);
        }
        _opt.execute();
        return BlurObject.bind(((Object[]) _opt.getResultSet().get(0)[0])[1]).toLongValue();
    }

    public long count(SQL sql) throws Exception {
        String _sql = ExpressionUtils.bind("SELECT count(1) FROM (${sql}) c_t").set("sql", sql.getSQL()).getResult();
        IQueryOperator<Object[]> _opt = new DefaultQueryOperator<Object[]>(_sql, this.getConnectionHolder(), IResultSetHandler.ARRAY);
        for (Object _param : sql.getParams().getParams()) {
            _opt.addParameter(_param);
        }
        _opt.execute();
        return BlurObject.bind(((Object[]) _opt.getResultSet().get(0)[0])[1]).toLongValue();
    }

    /**
     * @param entityMeta
     * @param targetObj
     * @param filter     字段名称过滤集合
     * @return 获取主键对象的所有字段和值
     * @throws Exception
     */
    protected PairObject<Fields, Params> __doGetPrimaryKeyFieldAndValues(EntityMeta entityMeta, Object targetObj, Fields filter) throws Exception {
        Fields _fields = Fields.create();
        Params _values = Params.create();
        if (targetObj instanceof IEntityPK) {
            if (entityMeta.isMultiplePrimaryKey()) {
                for (String _pkFieldName : entityMeta.getPrimaryKeys()) {
                    Object _value = entityMeta.getPropertyByName(_pkFieldName).getField().get(targetObj);
                    if (_value != null) {
                        if (__doCheckField(filter, _pkFieldName)) {
                            _fields.add(_pkFieldName);
                            _values.add(_value);
                        }
                    }
                }
            } else {
                String _fieldName = entityMeta.getPrimaryKeys().get(0);
                if (__doCheckField(filter, _fieldName)) {
                    _fields.add(_fieldName);
                    _values.add(targetObj);
                }
            }
        } else if (targetObj instanceof IEntity) {
            if (entityMeta.isMultiplePrimaryKey()) {
                PairObject<Fields, Params> _tmpValues = __doGetPrimaryKeyFieldAndValues(entityMeta, ((IEntity) targetObj).getId(), filter);
                _fields.add(_tmpValues.getKey());
                _values.add(_tmpValues.getValue());
            } else {
                String _fieldName = entityMeta.getPrimaryKeys().get(0);
                if (__doCheckField(filter, _fieldName)) {
                    _fields.add(_fieldName);
                    _values.add(((IEntity) targetObj).getId());
                }
            }
        } else {
            String _fieldName = entityMeta.getPrimaryKeys().get(0);
            if (__doCheckField(filter, _fieldName)) {
                _fields.add(_fieldName);
                _values.add(targetObj);
            }
        }
        return new PairObject<Fields, Params>(_fields, _values);
    }

    /**
     * @param entityMeta
     * @param targetObj
     * @param filter     进滤的字段名称集合
     * @param includePK  是否提取主键对象的值
     * @return 获取实体的所有字段和值
     * @throws Exception
     */
    protected PairObject<Fields, Params> __doGetEntityFieldAndValues(EntityMeta entityMeta, IEntity targetObj, Fields filter, boolean includePK) throws Exception {
        Fields _fields = Fields.create();
        Params _values = Params.create();
        for (String _fieldName : entityMeta.getPropertyNames()) {
            if (__doCheckField(filter, _fieldName)) {
                Object _value = null;
                if (entityMeta.isPrimaryKey(_fieldName)) {
                    if (includePK) {
                        if (entityMeta.isMultiplePrimaryKey()) {
                            _value = entityMeta.getPropertyByName(_fieldName).getField().get(targetObj.getId());
                        } else {
                            _value = targetObj.getId();
                        }
                    }
                } else {
                    _value = entityMeta.getPropertyByName(_fieldName).getField().get(targetObj);
                }
                // 以下操作是为了使@Default起效果的同时也保证数据库中的字段默认值不被null值替代
                EntityMeta.PropertyMeta _propMeta = entityMeta.getPropertyByName(_fieldName);
                if (_value == null) {
                    // 如果value为空则尝试提取默认值
                    _value = BlurObject.bind(_propMeta.getDefaultValue()).toObjectValue(_propMeta.getField().getType());
                }
                if (_value != null || _propMeta.isNullable()) {
                    // 若value不为空则添加至返回对象中
                    _fields.add(_fieldName);
                    _values.add(_value);
                }
            }
        }
        return new PairObject<Fields, Params>(_fields, _values);
    }

    /**
     * @param filter    字段过滤对象
     * @param fieldName 数据表字段名称
     * @return 返回字段是否被过滤
     */
    protected boolean __doCheckField(Fields filter, String fieldName) {
        if (filter != null) {
            if (filter.isExcluded()) {
                return !filter.getFields().contains(fieldName);
            } else {
                return filter.getFields().contains(fieldName);
            }
        }
        return true;
    }

    /**
     * @param entityMeta 目标数据实体属性描述对象
     * @param filter     字段过滤对象
     * @param forUpdate  若是更新操作则需要过滤掉声明了@Readonly的字段
     * @param includePK  是否包含主键
     * @return 返回目标实体中所有未被过滤的字段名称集合
     */
    protected Fields __doGetNotExcludedFields(EntityMeta entityMeta, Fields filter, boolean forUpdate, boolean includePK) {
        Fields _returnValue = Fields.create();
        for (String _field : entityMeta.getPropertyNames()) {
            if (__doCheckField(filter, _field)) {
                if (!includePK && entityMeta.isPrimaryKey(_field)) {
                    continue;
                }
                if (forUpdate && entityMeta.isReadonly(_field)) {
                    continue;
                }
                _returnValue.add(_field);
            }
        }
        return _returnValue;
    }

    /**
     * 访问器配置接口私有实现，只为DefaultSession提供扩展服务
     */
    private class EntityAccessorConfig implements IAccessorConfig {

        private EntityMeta __entityMeta;
        private IDialect __dialect;
        private IEntity<?>[] __entities;

        public EntityAccessorConfig(EntityMeta entityMeta, IDialect dialect, IEntity<?>... entities) {
            __entityMeta = entityMeta;
            __dialect = dialect;
            __entities = entities;
        }

        public Statement getStatement(Connection conn) throws Exception {
            return conn.createStatement();
        }

        public CallableStatement getCallableStatement(Connection conn, String sql) throws Exception {
            return conn.prepareCall(sql);
        }

        public PreparedStatement getPreparedStatement(Connection conn, String sql) throws Exception {
            return conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        }

        public void beforeStatementExecution(AccessorEventContext context) throws Exception {
        }

        public void afterStatementExecution(AccessorEventContext context) throws Exception {
            if (__entities != null && __entityMeta.hasAutoincrement()) {
                // 注意：自动主键生成仅支持每个数据表一个自动主键
                Object[] _genKeyValue = __dialect.getGeneratedKey(context.getStatement());
                for (int _idx = 0; _idx < this.__entities.length; _idx++) {
                    for (String _autoFieldName : __entityMeta.getAutoincrementKeys()) {
                        Field _field = __entityMeta.getPropertyByName(_autoFieldName).getField();
                        if (__entityMeta.isMultiplePrimaryKey()) {
                            Object _fieldValue = _field.get(__entities[_idx].getId());
                            if (_fieldValue == null) {
                                // 若执行插入操作时已为自生成主键赋值则将不再自动填充
                                _field.set(__entities[_idx].getId(), _genKeyValue[_idx]);
                            }
                        } else {
                            Object _fieldValue = _field.get(__entities[_idx]);
                            if (_fieldValue == null) {
                                // 若执行插入操作时已为自生成主键赋值则将不再自动填充
                                _field.set(__entities[_idx], _genKeyValue[_idx]);
                            }
                        }
                    }
                }
            }
        }

        public int getFetchDirection() {
            return 0;
        }

        public int getFetchSize() {
            return 10000;
        }

        public int getMaxFieldSize() {
            return 0;
        }

        public int getMaxRows() {
            return 1000;
        }

        public int getQueryTimeout() {
            return 0;
        }
    }
}
