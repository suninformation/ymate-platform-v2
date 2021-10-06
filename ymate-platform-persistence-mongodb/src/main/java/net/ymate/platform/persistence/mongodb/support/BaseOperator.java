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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.mongodb.AbstractOperator;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IOperator;
import org.apache.commons.lang3.StringUtils;
import org.bson.BSONObject;
import org.bson.BsonType;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/10/11 00:44
 * @since 2.1.0
 */
@SuppressWarnings("unchecked")
public abstract class BaseOperator<T extends BaseOperator<?>> extends AbstractOperator {

    protected BaseOperator() {
    }

    public T cmp(Object exp1, Object exp2) {
        addOperator(IMongo.Opt.CMP, new Object[]{exp1, exp2});
        return (T) this;
    }

    public T eq(Object param) {
        addOperator(IMongo.Opt.EQ, param);
        return (T) this;
    }

    public T eq(Params params) {
        addOperator(IMongo.Opt.EQ, params);
        return (T) this;
    }

    public T ne(Object param) {
        addOperator(IMongo.Opt.NE, param);
        return (T) this;
    }

    public T ne(Params params) {
        addOperator(IMongo.Opt.NE, params);
        return (T) this;
    }

    public T gt(Object param) {
        addOperator(IMongo.Opt.GT, param);
        return (T) this;
    }

    public T gt(Params params) {
        addOperator(IMongo.Opt.GT, params);
        return (T) this;
    }

    public T gte(Object param) {
        addOperator(IMongo.Opt.GTE, param);
        return (T) this;
    }

    public T gte(Params params) {
        addOperator(IMongo.Opt.GTE, params);
        return (T) this;
    }

    public T lt(Object param) {
        addOperator(IMongo.Opt.LT, param);
        return (T) this;
    }

    public T lt(Params params) {
        addOperator(IMongo.Opt.LT, params);
        return (T) this;
    }

    public T lte(Object param) {
        addOperator(IMongo.Opt.LTE, param);
        return (T) this;
    }

    public T lte(Params params) {
        addOperator(IMongo.Opt.LTE, params);
        return (T) this;
    }

    public T in(Params values) {
        addOperator(IMongo.Opt.IN, values.toArray());
        return (T) this;
    }

    public T nin(Params values) {
        addOperator(IMongo.Opt.NIN, values.toArray());
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T or(Query... queries) {
        List<Bson> bsons = (List<Bson>) toBson().get(IMongo.Opt.OR);
        if (bsons == null) {
            bsons = new ArrayList<>();
            addOperator(IMongo.Opt.OR, bsons);
        }
        for (Query query : queries) {
            bsons.add(query.toBson());
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T and(Query... queries) {
        List<Bson> bsons = (List<Bson>) toBson().get(IMongo.Opt.AND);
        if (bsons == null) {
            bsons = new ArrayList<>();
            addOperator(IMongo.Opt.AND, bsons);
        }
        for (Query query : queries) {
            bsons.add(query.toBson());
        }
        return (T) this;
    }

    public T not(IOperator operator) {
        addOperator(IMongo.Opt.NOT, operator.toBson());
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T nor(Query... queries) {
        List<Bson> bsons = (List<Bson>) toBson().get(IMongo.Opt.NOR);
        if (bsons == null) {
            bsons = new ArrayList<>();
            addOperator(IMongo.Opt.NOR, bsons);
        }
        for (Query query : queries) {
            bsons.add(query.toBson());
        }
        return (T) this;
    }

    public T exists(boolean exists) {
        addOperator(IMongo.Opt.EXISTS, exists);
        return (T) this;
    }

    public T type(BsonType type) {
        addOperator(IMongo.Opt.TYPE, type);
        return (T) this;
    }

    public T mod(int divisor, int remainder) {
        addOperator(IMongo.Opt.MOD, new int[]{divisor, remainder});
        return (T) this;
    }

    public T mod(String divisor, String remainder) {
        addOperator(IMongo.Opt.MOD, new String[]{divisor, remainder});
        return (T) this;
    }

    public T regex(String regex) {
        addOperator(IMongo.Opt.REGEX, Pattern.compile(regex));
        return (T) this;
    }

    public T text(String search) {
        return text(search, null);
    }

    public T text(String search, String language) {
        DBObject dbObject = new BasicDBObject(IMongo.Opt.SEARCH, search);
        if (language != null) {
            dbObject.put(IMongo.Opt.LANGUAGE, language);
        }
        addOperator(IMongo.Opt.TEXT, dbObject);
        return (T) this;
    }

    public T where(String jsFunction) {
        addOperator(IMongo.Opt.WHERE, jsFunction);
        return (T) this;
    }

    public T all(Params params) {
        addOperator(IMongo.Opt.ALL, params.toArray());
        return (T) this;
    }

    public T elemMatch(IOperator... operators) {
        DBObject dbObject = new BasicDBObject();
        for (IOperator operator : operators) {
            dbObject.putAll((BSONObject) operator.toBson());
        }
        addOperator(IMongo.Opt.ELEM_MATCH, dbObject);
        return (T) this;
    }

    public T elemMatch(Query... queries) {
        DBObject dbObject = new BasicDBObject();
        for (Query query : queries) {
            dbObject.putAll((BSONObject) query.toBson());
        }
        addOperator(IMongo.Opt.ELEM_MATCH, dbObject);
        return (T) this;
    }

    public T size(int size) {
        addOperator(IMongo.Opt.SIZE, size);
        return (T) this;
    }

    public T size(Object size) {
        addOperator(IMongo.Opt.SIZE, size);
        return (T) this;
    }

    public T meta(String meta) {
        addOperator(IMongo.Opt.META, meta);
        return (T) this;
    }

    public T slice(int slice) {
        addOperator(IMongo.Opt.SLICE, slice);
        return (T) this;
    }

    public T slice(int skip, int limit) {
        addOperator(IMongo.Opt.SLICE, new int[]{skip, limit});
        return (T) this;
    }

    //

    public T inc(String field, Number amount) {
        putOperator(IMongo.Opt.INC, field, amount);
        return (T) this;
    }

    public T mul(String field, Number number) {
        putOperator(IMongo.Opt.MUL, field, number);
        return (T) this;
    }

    public T rename(String field, String newName) {
        putOperator(IMongo.Opt.RENAME, field, newName);
        return (T) this;
    }

    public T setOnInsert(String field, Object value) {
        putOperator(IMongo.Opt.SET_ON_INSERT, field, value);
        return (T) this;
    }

    @SuppressWarnings("rawtypes")
    public T setOnInsert(Map object) {
        putOperator(IMongo.Opt.SET_ON_INSERT, object);
        return (T) this;
    }

    public T set(String field, Object value) {
        putOperator(IMongo.Opt.SET, field, value);
        return (T) this;
    }

    @SuppressWarnings("rawtypes")
    public T set(Map object) {
        putOperator(IMongo.Opt.SET, object);
        return (T) this;
    }

    public T unset(String field) {
        putOperator(IMongo.Opt.UNSET, field, StringUtils.EMPTY);
        return (T) this;
    }

    public T unset(Fields fields) {
        Map<String, String> fieldMap = new HashMap<>(fields.fields().size());
        fields.fields().forEach((field) -> fieldMap.put(field, StringUtils.EMPTY));
        putOperator(IMongo.Opt.UNSET, fieldMap);
        return (T) this;
    }

    public T min(String field, Object value) {
        putOperator(IMongo.Opt.MIN, field, value);
        return (T) this;
    }

    public T min(Object value) {
        addOperator(IMongo.Opt.MIN, value);
        return (T) this;
    }

    public T max(String field, Object value) {
        putOperator(IMongo.Opt.MAX, field, value);
        return (T) this;
    }

    public T max(Object value) {
        addOperator(IMongo.Opt.MAX, value);
        return (T) this;
    }

    public T addToSet(String field, Object value) {
        putOperator(IMongo.Opt.ADD_TO_SET, field, value);
        return (T) this;
    }

    public T addToSet(Object value) {
        addOperator(IMongo.Opt.ADD_TO_SET, value);
        return (T) this;
    }

    public T each(Object value) {
        addOperator(IMongo.Opt.EACH, value);
        return (T) this;
    }

    public T sort(boolean asc) {
        addOperator(IMongo.Opt.SORT, asc ? 1 : -1);
        return (T) this;
    }

    public T position(int position) {
        addOperator(IMongo.Opt.POSITION, position);
        return (T) this;
    }

    public T isolated() {
        addOperator(IMongo.Opt.ISOLATED, 1);
        return (T) this;
    }

    public T push(String field, Object value) {
        putOperator(IMongo.Opt.PUSH, field, value);
        return (T) this;
    }

    public T pushAll(String field, Params value) {
        putOperator(IMongo.Opt.PUSH_ALL, field, value.toArray());
        return (T) this;
    }

    public T pull(String field, Query query) {
        putOperator(IMongo.Opt.PULL, field, query.toBson());
        return (T) this;
    }

    public T pullAll(String field, Query... queries) {
        List<DBObject> dbObjects = new ArrayList<>();
        for (Query query : queries) {
            dbObjects.add(query.toBson());
        }
        putOperator(IMongo.Opt.PULL_ALL, field, dbObjects);
        return (T) this;
    }

    public T pop(String field, boolean first) {
        putOperator(IMongo.Opt.POP, field, first ? -1 : 1);
        return (T) this;
    }

    public T sum(Object expression) {
        addOperator(IMongo.Opt.SUM, expression);
        return (T) this;
    }

    public T avg(Object expression) {
        addOperator(IMongo.Opt.AVG, expression);
        return (T) this;
    }

    public T first(Object expression) {
        addOperator(IMongo.Opt.FIRST, expression);
        return (T) this;
    }

    public T last(Object expression) {
        addOperator(IMongo.Opt.LAST, expression);
        return (T) this;
    }

    public T substr(String string, int start, int length) {
        addOperator(IMongo.Opt.SUBSTR, new Object[]{string, start, length});
        return (T) this;
    }
}
