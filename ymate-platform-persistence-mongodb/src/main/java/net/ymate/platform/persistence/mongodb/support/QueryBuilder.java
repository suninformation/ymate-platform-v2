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
package net.ymate.platform.persistence.mongodb.support;

import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IOperator;
import org.bson.BsonType;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/10/10 11:51 下午
 * @since 2.1.0
 */
public class QueryBuilder {

    private final Query query = Query.create();

    public static Query query() {
        return Query.create();
    }

    public static Query query(String key, IOperator operator) {
        return Query.create(key, operator);
    }

    public static Operator operator() {
        return Operator.create();
    }

    public QueryBuilder cond(String key, IOperator operator) {
        query.cond(key, operator);
        return this;
    }

    public QueryBuilder cmp(Object exp1, Object exp2) {
        query.cmp(IMongo.Opt.CMP, new Object[]{exp1, exp2});
        return this;
    }

    public QueryBuilder eq(Object param) {
        query.eq(param);
        return this;
    }

    public QueryBuilder eq(Params params) {
        query.eq(params);
        return this;
    }

    public QueryBuilder ne(Object param) {
        query.ne(param);
        return this;
    }

    public QueryBuilder ne(Params params) {
        query.ne(params);
        return this;
    }

    public QueryBuilder gt(Object param) {
        query.gt(param);
        return this;
    }

    public QueryBuilder gt(Params params) {
        query.gt(params);
        return this;
    }

    public QueryBuilder gte(Object param) {
        query.gte(param);
        return this;
    }

    public QueryBuilder gte(Params params) {
        query.gte(params);
        return this;
    }

    public QueryBuilder lt(Object param) {
        query.lt(param);
        return this;
    }

    public QueryBuilder lt(Params params) {
        query.lt(params);
        return this;
    }

    public QueryBuilder lte(Object param) {
        query.lte(param);
        return this;
    }

    public QueryBuilder lte(Params params) {
        query.lte(params);
        return this;
    }

    public QueryBuilder in(Params values) {
        query.in(values);
        return this;
    }

    public QueryBuilder nin(Params values) {
        query.nin(values);
        return this;
    }

    public QueryBuilder or(Query... queries) {
        query.or(queries);
        return this;
    }

    public QueryBuilder and(Query... queries) {
        query.and(queries);
        return this;
    }

    public QueryBuilder not(IOperator operator) {
        query.not(operator);
        return this;
    }

    public QueryBuilder nor(Query... queries) {
        query.nor(queries);
        return this;
    }

    public QueryBuilder exists(boolean exists) {
        query.exists(exists);
        return this;
    }

    public QueryBuilder type(BsonType type) {
        query.type(type);
        return this;
    }

    public QueryBuilder mod(int divisor, int remainder) {
        query.mod(divisor, remainder);
        return this;
    }

    public QueryBuilder mod(String divisor, String remainder) {
        query.mod(divisor, remainder);
        return this;
    }

    public QueryBuilder regex(String regex) {
        query.regex(regex);
        return this;
    }

    public QueryBuilder text(String search) {
        return text(search, null);
    }

    public QueryBuilder text(String search, String language) {
        query.text(search, language);
        return this;
    }

    public QueryBuilder where(String jsFunction) {
        query.where(jsFunction);
        return this;
    }

    public QueryBuilder all(Params params) {
        query.all(params);
        return this;
    }

    public QueryBuilder elemMatch(IOperator... operators) {
        query.elemMatch(operators);
        return this;
    }

    public QueryBuilder elemMatch(Query... queries) {
        query.elemMatch(queries);
        return this;
    }

    public QueryBuilder size(int size) {
        query.size(size);
        return this;
    }

    public QueryBuilder size(Object size) {
        query.size(size);
        return this;
    }

    public QueryBuilder meta(String meta) {
        query.meta(meta);
        return this;
    }

    public QueryBuilder slice(int slice) {
        query.slice(slice);
        return this;
    }

    public QueryBuilder slice(int skip, int limit) {
        query.slice(skip, limit);
        return this;
    }

    //

    public QueryBuilder inc(String field, Number amount) {
        query.inc(field, amount);
        return this;
    }

    public QueryBuilder mul(String field, Number number) {
        query.mul(field, number);
        return this;
    }

    public QueryBuilder rename(String field, String newName) {
        query.rename(field, newName);
        return this;
    }

    public QueryBuilder setOnInsert(String field, Object value) {
        query.setOnInsert(field, value);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public QueryBuilder setOnInsert(Map object) {
        query.setOnInsert(object);
        return this;
    }

    public QueryBuilder set(String field, Object value) {
        query.set(field, value);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public QueryBuilder set(Map object) {
        query.set(object);
        return this;
    }

    public QueryBuilder unset(String field) {
        query.unset(field);
        return this;
    }

    public QueryBuilder unset(Fields fields) {
        query.unset(fields);
        return this;
    }

    public QueryBuilder min(String field, Object value) {
        query.min(field, value);
        return this;
    }

    public QueryBuilder min(Object value) {
        query.min(value);
        return this;
    }

    public QueryBuilder max(String field, Object value) {
        query.max(field, value);
        return this;
    }

    public QueryBuilder max(Object value) {
        query.max(value);
        return this;
    }

    public QueryBuilder addToSet(String field, Object value) {
        query.addToSet(field, value);
        return this;
    }

    public QueryBuilder addToSet(Object value) {
        query.addToSet(value);
        return this;
    }

    public QueryBuilder each(Object value) {
        query.each(value);
        return this;
    }

    public QueryBuilder sort(boolean asc) {
        query.sort(asc);
        return this;
    }

    public QueryBuilder position(int position) {
        query.position(position);
        return this;
    }

    public QueryBuilder isolated() {
        query.isolated();
        return this;
    }

    public QueryBuilder push(String field, Object value) {
        query.push(field, value);
        return this;
    }

    public QueryBuilder pushAll(String field, Params value) {
        query.pushAll(field, value);
        return this;
    }

    public QueryBuilder pull(String field, Query query) {
        query.pull(field, query);
        return this;
    }

    public QueryBuilder pullAll(String field, Query... queries) {
        query.pullAll(field, queries);
        return this;
    }

    public QueryBuilder pop(String field, boolean first) {
        query.pop(field, first);
        return this;
    }

    public QueryBuilder sum(Object expression) {
        query.sum(expression);
        return this;
    }

    public QueryBuilder avg(Object expression) {
        query.avg(expression);
        return this;
    }

    public QueryBuilder first(Object expression) {
        query.first(expression);
        return this;
    }

    public QueryBuilder last(Object expression) {
        query.last(expression);
        return this;
    }

    public QueryBuilder substr(String string, int start, int length) {
        query.substr(string, start, length);
        return this;
    }

    public Query build() {
        return query;
    }
}
