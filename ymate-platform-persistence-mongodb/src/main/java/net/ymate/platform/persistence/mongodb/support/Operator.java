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
package net.ymate.platform.persistence.mongodb.support;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.mongodb.AbstractOperator;
import net.ymate.platform.persistence.mongodb.IMongo;
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
 * @author 刘镇 (suninformation@163.com) on 15/11/24 下午1:24
 */
public final class Operator extends AbstractOperator {

    public static Operator create() {
        return new Operator();
    }

    private Operator() {
    }

    public Operator cmp(Object exp1, Object exp2) {
        addOperator(IMongo.Opt.CMP, new Object[]{exp1, exp2});
        return this;
    }

    public Operator eq(Object param) {
        addOperator(IMongo.Opt.EQ, param);
        return this;
    }

    public Operator eq(Params params) {
        addOperator(IMongo.Opt.EQ, params);
        return this;
    }

    public Operator ne(Object param) {
        addOperator(IMongo.Opt.NE, param);
        return this;
    }

    public Operator ne(Params params) {
        addOperator(IMongo.Opt.NE, params);
        return this;
    }

    public Operator gt(Object param) {
        addOperator(IMongo.Opt.GT, param);
        return this;
    }

    public Operator gt(Params params) {
        addOperator(IMongo.Opt.GT, params);
        return this;
    }

    public Operator gte(Object param) {
        addOperator(IMongo.Opt.GTE, param);
        return this;
    }

    public Operator gte(Params params) {
        addOperator(IMongo.Opt.GTE, params);
        return this;
    }

    public Operator lt(Object param) {
        addOperator(IMongo.Opt.LT, param);
        return this;
    }

    public Operator lt(Params params) {
        addOperator(IMongo.Opt.LT, params);
        return this;
    }

    public Operator lte(Object param) {
        addOperator(IMongo.Opt.LTE, param);
        return this;
    }

    public Operator lte(Params params) {
        addOperator(IMongo.Opt.LTE, params);
        return this;
    }

    public Operator in(Params values) {
        addOperator(IMongo.Opt.IN, values.toArray());
        return this;
    }

    public Operator nin(Params values) {
        addOperator(IMongo.Opt.NIN, values.toArray());
        return this;
    }

    @SuppressWarnings("unchecked")
    public Operator or(Query... queries) {
        List<Bson> bsons = (List<Bson>) toBson().get(IMongo.Opt.OR);
        if (bsons == null) {
            bsons = new ArrayList<>();
            addOperator(IMongo.Opt.OR, bsons);
        }
        for (Query query : queries) {
            bsons.add(query.toBson());
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public Operator and(Query... queries) {
        List<Bson> bsons = (List<Bson>) toBson().get(IMongo.Opt.AND);
        if (bsons == null) {
            bsons = new ArrayList<>();
            addOperator(IMongo.Opt.AND, bsons);
        }
        for (Query query : queries) {
            bsons.add(query.toBson());
        }
        return this;
    }

    public Operator not(Operator operator) {
        addOperator(IMongo.Opt.NOT, operator.toBson());
        return this;
    }

    @SuppressWarnings("unchecked")
    public Operator nor(Query... queries) {
        List<Bson> bsons = (List<Bson>) toBson().get(IMongo.Opt.NOR);
        if (bsons == null) {
            bsons = new ArrayList<>();
            addOperator(IMongo.Opt.NOR, bsons);
        }
        for (Query query : queries) {
            bsons.add(query.toBson());
        }
        return this;
    }

    public Operator exists(boolean exists) {
        addOperator(IMongo.Opt.EXISTS, exists);
        return this;
    }

    public Operator type(BsonType type) {
        addOperator(IMongo.Opt.TYPE, type);
        return this;
    }

    public Operator mod(int divisor, int remainder) {
        addOperator(IMongo.Opt.MOD, new int[]{divisor, remainder});
        return this;
    }

    public Operator mod(String divisor, String remainder) {
        addOperator(IMongo.Opt.MOD, new String[]{divisor, remainder});
        return this;
    }

    public Operator regex(String regex) {
        addOperator(IMongo.Opt.REGEX, Pattern.compile(regex));
        return this;
    }

    public Operator text(String search) {
        return text(search, null);
    }

    public Operator text(String search, String language) {
        DBObject dbObject = new BasicDBObject(IMongo.Opt.SEARCH, search);
        if (language != null) {
            dbObject.put(IMongo.Opt.LANGUAGE, language);
        }
        addOperator(IMongo.Opt.TEXT, dbObject);
        return this;
    }

    public Operator where(String jsFunction) {
        addOperator(IMongo.Opt.WHERE, jsFunction);
        return this;
    }

    public Operator all(Params params) {
        addOperator(IMongo.Opt.ALL, params.toArray());
        return this;
    }

    public Operator elemMatch(Operator... operators) {
        DBObject dbObject = new BasicDBObject();
        for (Operator operator : operators) {
            dbObject.putAll((BSONObject) operator.toBson());
        }
        addOperator(IMongo.Opt.ELEM_MATCH, dbObject);
        return this;
    }

    public Operator elemMatch(Query... queries) {
        DBObject dbObject = new BasicDBObject();
        for (Query query : queries) {
            dbObject.putAll((BSONObject) query.toBson());
        }
        addOperator(IMongo.Opt.ELEM_MATCH, dbObject);
        return this;
    }

    public Operator size(int size) {
        addOperator(IMongo.Opt.SIZE, size);
        return this;
    }

    public Operator size(Object size) {
        addOperator(IMongo.Opt.SIZE, size);
        return this;
    }

    public Operator meta(String meta) {
        addOperator(IMongo.Opt.META, meta);
        return this;
    }

    public Operator slice(int slice) {
        addOperator(IMongo.Opt.SLICE, slice);
        return this;
    }

    public Operator slice(int skip, int limit) {
        addOperator(IMongo.Opt.SLICE, new int[]{skip, limit});
        return this;
    }

    //

    public Operator inc(String field, Number amount) {
        putOperator(IMongo.Opt.INC, field, amount);
        return this;
    }

    public Operator mul(String field, Number number) {
        putOperator(IMongo.Opt.MUL, field, number);
        return this;
    }

    public Operator rename(String field, String newName) {
        putOperator(IMongo.Opt.RENAME, field, newName);
        return this;
    }

    public Operator setOnInsert(String field, Object value) {
        putOperator(IMongo.Opt.SET_ON_INSERT, field, value);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public Operator setOnInsert(Map object) {
        putOperator(IMongo.Opt.SET_ON_INSERT, object);
        return this;
    }

    public Operator set(String field, Object value) {
        putOperator(IMongo.Opt.SET, field, value);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public Operator set(Map object) {
        putOperator(IMongo.Opt.SET, object);
        return this;
    }

    public Operator unset(String field) {
        putOperator(IMongo.Opt.UNSET, field, StringUtils.EMPTY);
        return this;
    }

    public Operator unset(Fields fields) {
        Map<String, String> fieldMap = new HashMap<>(fields.fields().size());
        fields.fields().forEach((field) -> fieldMap.put(field, StringUtils.EMPTY));
        putOperator(IMongo.Opt.UNSET, fieldMap);
        return this;
    }

    public Operator min(String field, Object value) {
        putOperator(IMongo.Opt.MIN, field, value);
        return this;
    }

    public Operator min(Object value) {
        addOperator(IMongo.Opt.MIN, value);
        return this;
    }

    public Operator max(String field, Object value) {
        putOperator(IMongo.Opt.MAX, field, value);
        return this;
    }

    public Operator max(Object value) {
        addOperator(IMongo.Opt.MAX, value);
        return this;
    }

    public Operator addToSet(String field, Object value) {
        putOperator(IMongo.Opt.ADD_TO_SET, field, value);
        return this;
    }

    public Operator addToSet(Object value) {
        addOperator(IMongo.Opt.ADD_TO_SET, value);
        return this;
    }

    public Operator each(Object value) {
        addOperator(IMongo.Opt.EACH, value);
        return this;
    }

    public Operator sort(boolean asc) {
        addOperator(IMongo.Opt.SORT, asc ? 1 : -1);
        return this;
    }

    public Operator position(int position) {
        addOperator(IMongo.Opt.POSITION, position);
        return this;
    }

    public Operator isolated() {
        addOperator(IMongo.Opt.ISOLATED, 1);
        return this;
    }

    public Operator push(String field, Object value) {
        putOperator(IMongo.Opt.PUSH, field, value);
        return this;
    }

    public Operator pushAll(String field, Params value) {
        putOperator(IMongo.Opt.PUSH_ALL, field, value.toArray());
        return this;
    }

    public Operator pull(String field, Query query) {
        putOperator(IMongo.Opt.PULL, field, query.toBson());
        return this;
    }

    public Operator pullAll(String field, Query... queries) {
        List<DBObject> dbObjects = new ArrayList<>();
        for (Query query : queries) {
            dbObjects.add(query.toBson());
        }
        putOperator(IMongo.Opt.PULL_ALL, field, dbObjects);
        return this;
    }

    public Operator pop(String field, boolean first) {
        putOperator(IMongo.Opt.POP, field, first ? -1 : 1);
        return this;
    }

    public Operator sum(Object expression) {
        addOperator(IMongo.Opt.SUM, expression);
        return this;
    }

    public Operator avg(Object expression) {
        addOperator(IMongo.Opt.AVG, expression);
        return this;
    }

    public Operator first(Object expression) {
        addOperator(IMongo.Opt.FIRST, expression);
        return this;
    }

    public Operator last(Object expression) {
        addOperator(IMongo.Opt.LAST, expression);
        return this;
    }

    public Operator substr(String string, int start, int length) {
        addOperator(IMongo.Opt.SUBSTR, new Object[]{string, start, length});
        return this;
    }
}
