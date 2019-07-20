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
package net.ymate.platform.persistence.mongodb.expression;

import com.mongodb.BasicDBObject;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.mongodb.AbstractOperator;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.support.Query;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/28 下午3:23
 */
public class UpdateExp extends AbstractOperator {

    public static UpdateExp inc(String field, Number amount) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.INC, field, amount);
        return updateExp;
    }

    public static UpdateExp mul(String field, Number number) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.MUL, field, number);
        return updateExp;
    }

    public static UpdateExp rename(String field, String newName) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.RENAME, field, newName);
        return updateExp;
    }

    public static UpdateExp setOnInsert(String field, Object value) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.SET_ON_INSERT, field, value);
        return updateExp;
    }

    public static UpdateExp setOnInsert(Map object) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.SET_ON_INSERT, object);
        return updateExp;
    }

    public static UpdateExp set(String field, Object value) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.SET, field, value);
        return updateExp;
    }

    public static UpdateExp set(Map object) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.SET, object);
        return updateExp;
    }

    public static UpdateExp unset(String field) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.UNSET, field, StringUtils.EMPTY);
        return updateExp;
    }

    public static UpdateExp unset(Fields fields) {
        UpdateExp updateExp = new UpdateExp();
        Map<String, String> fieldMap = new HashMap<>(fields.fields().size());
        fields.fields().forEach((field) -> fieldMap.put(field, StringUtils.EMPTY));
        updateExp.putOperator(IMongo.Opt.UNSET, fieldMap);
        return updateExp;
    }

    public static UpdateExp min(String field, Object value) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.MIN, field, value);
        return updateExp;
    }

    public static UpdateExp min(Object value) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.addOperator(IMongo.Opt.MIN, value);
        return updateExp;
    }

    public static UpdateExp max(String field, Object value) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.MAX, field, value);
        return updateExp;
    }

    public static UpdateExp max(Object value) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.addOperator(IMongo.Opt.MAX, value);
        return updateExp;
    }

    public static UpdateExp addToSet(String field, Object value) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.ADD_TO_SET, field, value);
        return updateExp;
    }

    public static UpdateExp addToSet(Object value) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.addOperator(IMongo.Opt.ADD_TO_SET, value);
        return updateExp;
    }

    public static UpdateExp each(Object value) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.addOperator(IMongo.Opt.EACH, value);
        return updateExp;
    }

    public static UpdateExp sort(boolean asc) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.addOperator(IMongo.Opt.SORT, asc ? 1 : -1);
        return updateExp;
    }

    public static UpdateExp position(int position) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.addOperator(IMongo.Opt.POSITION, position);
        return updateExp;
    }

    public static UpdateExp isolated() {
        UpdateExp updateExp = new UpdateExp();
        updateExp.addOperator(IMongo.Opt.ISOLATED, 1);
        return updateExp;
    }

    public static UpdateExp push(String field, Object value) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.PUSH, field, value);
        return updateExp;
    }

    public static UpdateExp pushAll(String field, Params value) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.PUSH_ALL, field, value.toArray());
        return updateExp;
    }

    public static UpdateExp pull(String field, Query query) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.PULL, field, query.toBson());
        return updateExp;
    }

    public static UpdateExp pullAll(String field, Query... queries) {
        UpdateExp updateExp = new UpdateExp();
        List<BasicDBObject> dbObjects = new ArrayList<>();
        for (Query query : queries) {
            dbObjects.add(query.toBson());
        }
        updateExp.putOperator(IMongo.Opt.PULL_ALL, field, dbObjects);
        return updateExp;
    }

    public static UpdateExp pop(String field, boolean first) {
        UpdateExp updateExp = new UpdateExp();
        updateExp.putOperator(IMongo.Opt.POP, field, first ? -1 : 1);
        return updateExp;
    }
}
