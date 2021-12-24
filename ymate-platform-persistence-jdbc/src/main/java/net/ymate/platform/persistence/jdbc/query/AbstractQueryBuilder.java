 /*
  * Copyright 2007-2021 the original author or authors.
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

 import net.ymate.platform.core.persistence.Fields;
 import net.ymate.platform.core.persistence.IFunction;
 import net.ymate.platform.core.persistence.Page;
 import net.ymate.platform.core.persistence.Params;
 import net.ymate.platform.core.persistence.base.IEntity;
 import net.ymate.platform.persistence.jdbc.IDatabase;
 import net.ymate.platform.persistence.jdbc.JDBC;

 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map;
 import java.util.stream.Collectors;

 /**
  * @author 刘镇 (suninformation@163.com) on 2021/01/13 23:38
  * @since 2.1.0
  */
 @SuppressWarnings("rawtypes")
 public abstract class AbstractQueryBuilder<RESULT, QUERY_BUILDER extends AbstractQueryBuilder> extends Query<QUERY_BUILDER> {

     private RESULT result;

     public AbstractQueryBuilder() {
         this(JDBC.get());
     }

     public AbstractQueryBuilder(IDatabase owner) {
         super(owner, owner.getConfig().getDefaultDataSourceName());
     }

     public AbstractQueryBuilder(IDatabase owner, String dataSourceName) {
         super(owner, dataSourceName);
     }

     public RESULT build() {
         return result;
     }

     protected void build(RESULT result) {
         this.result = result;
     }

     // ------ SQL

     public SQL sql(String sql) {
         return SQL.create(owner(), sql);
     }

     public SQL sql(Select select) {
         return SQL.create(select);
     }

     public SQL sql(Insert insert) {
         return SQL.create(insert);
     }

     public SQL sql(Update update) {
         return SQL.create(update);
     }

     public SQL sql(Delete delete) {
         return SQL.create(delete);
     }

     public SQL sql(String expressionSqlStr, Map<String, Object> params) {
         return SQL.create(owner(), expressionSqlStr, params);
     }

     // ------ EntitySQL

     public <T extends IEntity> EntitySQL<T> entity(Class<T> entityClass) {
         return new EntitySQL<>(owner(), entityClass);
     }

     // ------ BatchSQL

     public BatchSQL batch(String batchSql) {
         return new BatchSQL(owner(), batchSql);
     }

     public BatchSQL batch() {
         return new BatchSQL(owner(), null);
     }

     public BatchSQL batch(Insert insert) {
         return new BatchSQL(insert.owner(), insert.toString());
     }

     public BatchSQL batch(Update update) {
         return new BatchSQL(update.owner(), update.toString());
     }

     public BatchSQL batch(Delete delete) {
         return new BatchSQL(delete.owner(), delete.toString());
     }


     // ------ SELECT

     public Select select() {
         return Select.create(owner(), dataSourceName());
     }

     public Select select(Select select) {
         return Select.create(select);
     }

     public Select select(Class<? extends IEntity> entityClass) {
         return select().from(null, entityClass, null);
     }

     public Select select(String prefix, Class<? extends IEntity> entityClass) {
         return select().from(prefix, entityClass, null);
     }

     public Select select(Class<? extends IEntity> entityClass, String alias) {
         return select().from(null, entityClass, alias);
     }

     public Select select(String prefix, Class<? extends IEntity> entityClass, String alias) {
         return select().from(prefix, entityClass, alias);
     }

     public Select select(String prefix, String from, String alias) {
         return new Select(owner(), dataSourceName(), prefix, from, alias, true);
     }

     public Select select(String from, String alias) {
         return new Select(owner(), dataSourceName(), null, from, alias, true);
     }

     public Select select(String from, String alias, boolean safePrefix) {
         return new Select(owner(), dataSourceName(), null, from, alias, safePrefix);
     }

     public Select select(String from) {
         return new Select(owner(), dataSourceName(), null, from, null, true);
     }

     public Select select(String from, boolean safePrefix) {
         return new Select(owner(), dataSourceName(), null, from, null, safePrefix);
     }

     public Select select(IFunction func) {
         return new Select(owner(), dataSourceName()).field(func);
     }

     public Select select(IFunction func, String alias) {
         return new Select(owner(), dataSourceName()).field(func, alias);
     }

     // ------ UPDATE

     public Update update() {
         return new Update(owner(), dataSourceName());
     }

     public Update update(String prefix, Class<? extends IEntity> entityClass, String alias) {
         return new Update(owner(), dataSourceName()).table(prefix, entityClass, alias);
     }

     public Update update(String prefix, Class<? extends IEntity> entityClass) {
         return new Update(owner(), dataSourceName()).table(prefix, entityClass, null);
     }

     public Update update(Class<? extends IEntity> entityClass) {
         return new Update(owner(), dataSourceName()).table(entityClass, null);
     }

     public Update update(String prefix, String tableName, String alias) {
         return new Update(owner(), dataSourceName(), prefix, tableName, alias, true);
     }

     public Update update(String prefix, String tableName, String alias, boolean safePrefix) {
         return new Update(owner(), dataSourceName(), prefix, tableName, alias, safePrefix);
     }

     public Update update(String tableName, String alias) {
         return new Update(owner(), dataSourceName(), null, tableName, alias, true);
     }

     public Update update(String tableName, String alias, boolean safePrefix) {
         return new Update(owner(), dataSourceName(), null, tableName, alias, safePrefix);
     }

     public Update update(String tableName) {
         return new Update(owner(), dataSourceName(), null, tableName, null, true);
     }

     public Update update(String tableName, boolean safePrefix) {
         return new Update(owner(), dataSourceName(), null, tableName, null, safePrefix);
     }

     // ------ INSERT

     public Insert insert(String prefix, Class<? extends IEntity> entityClass) {
         return new Insert(owner(), dataSourceName(), prefix, entityClass);
     }

     public Insert insert(IEntity<?> entity) {
         return insert(entity.getClass());
     }

     public Insert insert(Class<? extends IEntity> entityClass) {
         return new Insert(owner(), dataSourceName(), null, entityClass);
     }

     public Insert insert(String tableName) {
         return new Insert(owner(), dataSourceName(), null, tableName, true);
     }

     public Insert insert(String tableName, boolean safePrefix) {
         return new Insert(owner(), dataSourceName(), null, tableName, safePrefix);
     }

     // ------ DELETE

     public Delete delete() {
         return new Delete(owner(), dataSourceName());
     }

     public Delete delete(Class<? extends IEntity> entityClass) {
         return new Delete(owner(), dataSourceName()).from(entityClass);
     }

     public Delete delete(String prefix, Class<? extends IEntity> entityClass) {
         return new Delete(owner(), dataSourceName()).from(prefix, entityClass, null);
     }

     public Delete delete(Class<? extends IEntity> entityClass, String alias) {
         return new Delete(owner(), dataSourceName()).from(null, entityClass, alias);
     }

     public Delete delete(String prefix, Class<? extends IEntity> entityClass, String alias) {
         return new Delete(owner(), dataSourceName()).from(prefix, entityClass, alias);
     }

     public Delete delete(String prefix, String tableName, String alias) {
         return new Delete(owner(), dataSourceName(), prefix, tableName, alias, true);
     }

     public Delete delete(String tableName, String alias) {
         return new Delete(owner(), dataSourceName(), null, tableName, alias, true);
     }

     public Delete delete(String tableName, String alias, boolean safePrefix) {
         return new Delete(owner(), dataSourceName(), null, tableName, alias, safePrefix);
     }

     public Delete delete(String tableName) {
         return new Delete(owner(), dataSourceName(), null, tableName, null, true);
     }

     public Delete delete(String tableName, boolean safePrefix) {
         return new Delete(owner(), dataSourceName(), null, tableName, null, safePrefix);
     }

     // ------ Cond

     public Cond cond() {
         return new Cond(owner(), dataSourceName());
     }

     public FieldCondition fieldCond(String prefix, String fieldName) {
         return new FieldCondition(owner(), dataSourceName(), prefix, fieldName);
     }

     public FieldCondition fieldCond(String fieldName) {
         return new FieldCondition(owner(), dataSourceName(), fieldName);
     }

     // ------ GroupBy

     public GroupBy groupBy() {
         return new GroupBy(owner(), dataSourceName());
     }

     public GroupBy groupBy(Cond having) {
         return new GroupBy(owner(), dataSourceName()).having(having);
     }

     public GroupBy groupBy(String prefix, String field) {
         return new GroupBy(owner(), dataSourceName()).field(prefix, field);
     }

     public GroupBy groupBy(String prefix, String field, boolean wrapIdentifier) {
         return new GroupBy(owner(), dataSourceName()).field(prefix, field, wrapIdentifier);
     }

     public GroupBy groupBy(String field) {
         return new GroupBy(owner(), dataSourceName()).field(field);
     }

     public GroupBy groupBy(String field, boolean wrapIdentifier) {
         return new GroupBy(owner(), dataSourceName()).field(field, wrapIdentifier);
     }

     public GroupBy groupBy(Fields fields) {
         return new GroupBy(owner(), dataSourceName()).field(fields);
     }

     public GroupBy groupBy(Fields fields, boolean wrapIdentifier) {
         return new GroupBy(owner(), dataSourceName()).field(fields, wrapIdentifier);
     }

     public GroupBy groupBy(String prefix, Fields fields) {
         return new GroupBy(owner(), dataSourceName()).field(prefix, fields);
     }

     public GroupBy groupBy(String prefix, Fields fields, boolean wrapIdentifier) {
         return new GroupBy(owner(), dataSourceName()).field(prefix, fields, wrapIdentifier);
     }

     // ------ OrderBy

     public OrderBy orderBy() {
         return new OrderBy(owner(), dataSourceName());
     }

     // ------ Join

     public Join inner(Select select) {
         Join target = new Join(select.owner(), select.dataSourceName(), Join.Type.INNER.getName(), null, select.toString(), false);
         target.params().add(select.params());
         return target;
     }

     public Join inner(String from) {
         return new Join(owner(), dataSourceName(), Join.Type.INNER.getName(), null, from, true);
     }

     public Join inner(String from, boolean safePrefix) {
         return new Join(owner(), dataSourceName(), Join.Type.INNER.getName(), null, from, safePrefix);
     }

     public Join inner(String prefix, String from) {
         return new Join(owner(), dataSourceName(), Join.Type.INNER.getName(), prefix, from, true);
     }

     public Join inner(String prefix, String from, boolean safePrefix) {
         return new Join(owner(), dataSourceName(), Join.Type.INNER.getName(), prefix, from, safePrefix);
     }

     // ------

     public Join left(Select select) {
         Join target = new Join(select.owner(), select.dataSourceName(), Join.Type.LEFT.getName(), null, select.toString(), false);
         target.params().add(select.params());
         return target;
     }

     public Join left(String from) {
         return new Join(owner(), dataSourceName(), Join.Type.LEFT.getName(), null, from, true);
     }

     public Join left(String from, boolean safePrefix) {
         return new Join(owner(), dataSourceName(), Join.Type.LEFT.getName(), null, from, safePrefix);
     }

     public Join left(String prefix, String from) {
         return new Join(owner(), dataSourceName(), Join.Type.LEFT.getName(), prefix, from, true);
     }

     public Join left(String prefix, String from, boolean safePrefix) {
         return new Join(owner(), dataSourceName(), Join.Type.LEFT.getName(), prefix, from, safePrefix);
     }

     // ------

     public Join right(Select select) {
         Join target = new Join(select.owner(), select.dataSourceName(), Join.Type.RIGHT.getName(), null, select.toString(), false);
         target.params().add(select.params());
         return target;
     }

     public Join right(String from) {
         return new Join(owner(), dataSourceName(), Join.Type.RIGHT.getName(), null, from, true);
     }

     public Join right(String from, boolean safePrefix) {
         return new Join(owner(), dataSourceName(), Join.Type.RIGHT.getName(), null, from, safePrefix);
     }

     public Join right(String prefix, String from) {
         return new Join(owner(), dataSourceName(), Join.Type.RIGHT.getName(), prefix, from, true);
     }

     public Join right(String prefix, String from, boolean safePrefix) {
         return new Join(owner(), dataSourceName(), Join.Type.RIGHT.getName(), prefix, from, safePrefix);
     }

     // ------ Union

     public Union union(Select select) {
         return new Union(select);
     }

     // ------ Like

     public Like like(String originStr) {
         return new Like(originStr);
     }

     // ------ Where

     public Where where() {
         return new Where(owner(), dataSourceName());
     }

     public Where where(String whereCond) {
         return new Where(owner(), dataSourceName(), whereCond);
     }

     public Where where(Cond cond) {
         return new Where(cond);
     }

     // ------ Fields

     public String field(String prefix, String field, String alias) {
         return Fields.field(prefix, field, alias);
     }

     public String field(String prefix, String field) {
         return field(prefix, field, null);
     }

     public Fields fields(String... fields) {
         return Fields.create(fields);
     }

     public Fields fields(Collection<String> fields) {
         return Fields.create(fields);
     }

     public String fieldWrap(String prefix, String field, String alias) {
         return wrapIdentifierField(Fields.field(prefix, field, alias));
     }

     public String fieldWrap(String prefix, String field) {
         return fieldWrap(prefix, field, null);
     }

     public Fields fieldsWrap(String... fields) {
         return Fields.create(Arrays.stream(fields).map(this::wrapIdentifierField).collect(Collectors.toList()));
     }

     public Fields fieldsWrap(Collection<String> fields) {
         return Fields.create(fields.stream().map(this::wrapIdentifierField).collect(Collectors.toList()));
     }

     // ------ Params

     public Params params(Object... params) {
         return Params.create(params);
     }

     public Params params(Collection<?> params) {
         return Params.create(params);
     }

     // ------ Page

     public Page page() {
         return Page.create();
     }

     public Page page(Integer page) {
         return Page.create(page);
     }

     public Page pageIfNeed(Integer page, Integer pageSize) {
         return Page.createIfNeed(page, pageSize);
     }
 }
