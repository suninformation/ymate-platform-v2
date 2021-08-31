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
 import net.ymate.platform.core.persistence.Params;
 import net.ymate.platform.persistence.jdbc.IDatabase;
 import net.ymate.platform.persistence.jdbc.JDBC;
 import org.apache.commons.lang.NullArgumentException;
 import org.apache.commons.lang3.StringUtils;

 /**
  * @author 刘镇 (suninformation@163.com) on 2021/01/07 22:42
  * @since 2.1.0
  */
 public class FieldCondition implements IConditionBuilder {

     private final Cond cond;

     private final String fieldName;

     public static FieldCondition create(String fieldName) {
         IDatabase database = JDBC.get();
         return new FieldCondition(database, database.getConfig().getDefaultDataSourceName(), fieldName);
     }

     public static FieldCondition create(String prefix, String fieldName) {
         IDatabase database = JDBC.get();
         return new FieldCondition(database, database.getConfig().getDefaultDataSourceName(), prefix, fieldName);
     }

     public static FieldCondition create(Query<?> query, String fieldName) {
         return new FieldCondition(query, fieldName);
     }

     public static FieldCondition create(Query<?> query, String prefix, String fieldName) {
         return new FieldCondition(query, prefix, fieldName);
     }

     public static FieldCondition create(IDatabase owner, String dataSourceName, String fieldName) {
         return new FieldCondition(owner, dataSourceName, fieldName);
     }

     public static FieldCondition create(IDatabase owner, String dataSourceName, String prefix, String fieldName) {
         return new FieldCondition(owner, dataSourceName, prefix, fieldName);
     }

     public FieldCondition(Query<?> query, String prefix, String fieldName) {
         this(query.owner(), query.dataSourceName(), prefix, fieldName);
     }

     public FieldCondition(Query<?> query, String fieldName) {
         this(query.owner(), query.dataSourceName(), fieldName);
     }

     public FieldCondition(IDatabase owner, String dataSourceName, String prefix, String fieldName) {
         this(owner, dataSourceName, Fields.field(prefix, fieldName));
     }

     public FieldCondition(IDatabase owner, String dataSourceName, String fieldName) {
         if (StringUtils.isBlank(fieldName)) {
             throw new NullArgumentException("fieldName");
         }
         cond = Cond.create(owner, dataSourceName);
         this.fieldName = fieldName;
     }

     public FieldCondition eq() {
         cond.eq(fieldName);
         return this;
     }

     public FieldCondition eqWrap() {
         cond.eqWrap(fieldName);
         return this;
     }

     public FieldCondition eq(String otherField) {
         cond.opt(fieldName, Cond.OPT.EQ, otherField);
         return this;
     }

     public FieldCondition eq(String prefix, String otherField) {
         cond.opt(fieldName, Cond.OPT.EQ, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition eqWrap(String otherField) {
         cond.optWrap(fieldName, Cond.OPT.EQ, otherField);
         return this;
     }

     public FieldCondition eqWrap(String prefix, String otherField) {
         cond.optWrap(fieldName, Cond.OPT.EQ, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition eq(IFunction func) {
         cond.opt(fieldName, Cond.OPT.EQ, func.build()).param(func.params());
         return this;
     }

     public FieldCondition eqValue(Object value) {
         cond.eq(fieldName).param(value);
         return this;
     }

     public FieldCondition eqValueWrap(Object value) {
         cond.eqWrap(fieldName).param(value);
         return this;
     }

     // ------

     public FieldCondition notEq() {
         cond.notEq(fieldName);
         return this;
     }

     public FieldCondition notEqWrap() {
         cond.notEqWrap(fieldName);
         return this;
     }

     public FieldCondition notEq(String otherField) {
         cond.opt(fieldName, Cond.OPT.NOT_EQ, otherField);
         return this;
     }

     public FieldCondition notEq(String prefix, String otherField) {
         cond.opt(fieldName, Cond.OPT.NOT_EQ, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition notEqWrap(String otherField) {
         cond.optWrap(fieldName, Cond.OPT.NOT_EQ, otherField);
         return this;
     }

     public FieldCondition notEqWrap(String prefix, String otherField) {
         cond.optWrap(fieldName, Cond.OPT.NOT_EQ, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition notEq(IFunction func) {
         cond.opt(fieldName, Cond.OPT.NOT_EQ, func.build()).param(func.params());
         return this;
     }

     public FieldCondition notEqValue(Object value) {
         cond.notEq(fieldName).param(value);
         return this;
     }

     public FieldCondition notEqValueWrap(Object value) {
         cond.notEqWrap(fieldName).param(value);
         return this;
     }

     // ------

     public FieldCondition gtEq() {
         cond.gtEq(fieldName);
         return this;
     }

     public FieldCondition gtEqWrap() {
         cond.gtEqWrap(fieldName);
         return this;
     }

     public FieldCondition gtEq(String otherField) {
         cond.opt(fieldName, Cond.OPT.GT_EQ, otherField);
         return this;
     }

     public FieldCondition gtEq(String prefix, String otherField) {
         cond.opt(fieldName, Cond.OPT.GT_EQ, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition gtEqWrap(String otherField) {
         cond.optWrap(fieldName, Cond.OPT.GT_EQ, otherField);
         return this;
     }

     public FieldCondition gtEqWrap(String prefix, String otherField) {
         cond.optWrap(fieldName, Cond.OPT.GT_EQ, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition gtEq(IFunction func) {
         cond.opt(fieldName, Cond.OPT.GT_EQ, func.build()).param(func.params());
         return this;
     }

     public FieldCondition gtEqValue(Object value) {
         cond.gtEq(fieldName).param(value);
         return this;
     }

     public FieldCondition gtEqValueWrap(Object value) {
         cond.gtEqWrap(fieldName).param(value);
         return this;
     }

     // ------

     public FieldCondition gt() {
         cond.gt(fieldName);
         return this;
     }

     public FieldCondition gtWrap() {
         cond.gtWrap(fieldName);
         return this;
     }

     public FieldCondition gt(String otherField) {
         cond.opt(fieldName, Cond.OPT.GT, otherField);
         return this;
     }

     public FieldCondition gt(String prefix, String otherField) {
         cond.opt(fieldName, Cond.OPT.GT, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition gtWrap(String otherField) {
         cond.optWrap(fieldName, Cond.OPT.GT, otherField);
         return this;
     }

     public FieldCondition gtWrap(String prefix, String otherField) {
         cond.optWrap(fieldName, Cond.OPT.GT, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition gt(IFunction func) {
         cond.opt(fieldName, Cond.OPT.GT, func.build()).param(func.params());
         return this;
     }

     public FieldCondition gtValue(Object value) {
         cond.gt(fieldName).param(value);
         return this;
     }

     public FieldCondition gtValueWrap(Object value) {
         cond.gtWrap(fieldName).param(value);
         return this;
     }

     // ------

     public FieldCondition ltEq() {
         cond.ltEq(fieldName);
         return this;
     }

     public FieldCondition ltEqWrap() {
         cond.ltEqWrap(fieldName);
         return this;
     }

     public FieldCondition ltEq(String otherField) {
         cond.opt(fieldName, Cond.OPT.LT_EQ, otherField);
         return this;
     }

     public FieldCondition ltEq(String prefix, String otherField) {
         cond.opt(fieldName, Cond.OPT.LT_EQ, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition ltEqWrap(String otherField) {
         cond.optWrap(fieldName, Cond.OPT.LT_EQ, otherField);
         return this;
     }

     public FieldCondition ltEqWrap(String prefix, String otherField) {
         cond.optWrap(fieldName, Cond.OPT.LT_EQ, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition ltEq(IFunction func) {
         cond.opt(fieldName, Cond.OPT.LT_EQ, func.build()).param(func.params());
         return this;
     }

     public FieldCondition ltEqValue(Object value) {
         cond.ltEq(fieldName).param(value);
         return this;
     }

     public FieldCondition ltEqValueWrap(Object value) {
         cond.ltEqWrap(fieldName).param(value);
         return this;
     }

     // ------

     public FieldCondition lt() {
         cond.lt(fieldName);
         return this;
     }

     public FieldCondition ltWrap() {
         cond.ltWrap(fieldName);
         return this;
     }

     public FieldCondition lt(String otherField) {
         cond.opt(fieldName, Cond.OPT.LT, otherField);
         return this;
     }

     public FieldCondition lt(String prefix, String otherField) {
         cond.opt(fieldName, Cond.OPT.LT, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition ltWrap(String otherField) {
         cond.optWrap(fieldName, Cond.OPT.LT, otherField);
         return this;
     }

     public FieldCondition ltWrap(String prefix, String otherField) {
         cond.optWrap(fieldName, Cond.OPT.LT, Fields.field(prefix, otherField));
         return this;
     }

     public FieldCondition lt(IFunction func) {
         cond.opt(fieldName, Cond.OPT.LT, func.build()).param(func.params());
         return this;
     }

     public FieldCondition ltValue(Object value) {
         cond.lt(fieldName).param(value);
         return this;
     }

     public FieldCondition ltValueWrap(Object value) {
         cond.ltWrap(fieldName).param(value);
         return this;
     }

     // ------

     public FieldCondition like(String likeStr) {
         cond.like(fieldName).param(likeStr);
         return this;
     }

     public FieldCondition likeWrap(String likeStr) {
         cond.likeWrap(fieldName).param(likeStr);
         return this;
     }

     // ------

     public FieldCondition between(Object valueOne, Object valueTwo) {
         cond.between(fieldName, valueOne, valueTwo);
         return this;
     }

     public FieldCondition betweenWrap(Object valueOne, Object valueTwo) {
         cond.betweenWrap(fieldName, valueOne, valueTwo);
         return this;
     }

     // ------

     public FieldCondition range(Number valueOne, Number valueTwo) {
         cond.range(fieldName, valueOne, valueTwo, null);
         return this;
     }

     public FieldCondition rangeWrap(Number valueOne, Number valueTwo) {
         cond.rangeWrap(fieldName, valueOne, valueTwo, null);
         return this;
     }

     // ------

     public FieldCondition isNull() {
         cond.isNull(fieldName);
         return this;
     }

     public FieldCondition isNullWrap() {
         cond.isNullWrap(fieldName);
         return this;
     }

     // ------

     public FieldCondition isNotNull() {
         cond.isNotNull(fieldName);
         return this;
     }

     public FieldCondition isNotNullWrap() {
         cond.isNotNullWrap(fieldName);
         return this;
     }

     // ------

     public FieldCondition in(SQL subSql) {
         cond.in(fieldName, subSql);
         return this;
     }

     public FieldCondition inWrap(SQL subSql) {
         cond.inWrap(fieldName, subSql);
         return this;
     }

     public FieldCondition in(Select subSql) {
         cond.in(fieldName, subSql);
         return this;
     }

     public FieldCondition inWrap(Select subSql) {
         cond.inWrap(fieldName, subSql);
         return this;
     }

     public FieldCondition in(Params params) {
         cond.in(fieldName, params);
         return this;
     }

     public FieldCondition inWrap(Params params) {
         cond.inWrap(fieldName, params);
         return this;
     }

     // ------

     public FieldCondition and() {
         cond.and();
         return this;
     }

     public FieldCondition or() {
         cond.or();
         return this;
     }

     public FieldCondition not() {
         cond.not();
         return this;
     }

     // ------

     public FieldCondition bracketBegin() {
         cond.bracketBegin();
         return this;
     }

     public FieldCondition bracketEnd() {
         cond.bracketEnd();
         return this;
     }

     public FieldCondition param(Object param) {
         cond.param(param);
         return this;
     }

     // ------

     @Override
     public Cond build() {
         return cond;
     }

     public String getFieldName() {
         return fieldName;
     }
 }
