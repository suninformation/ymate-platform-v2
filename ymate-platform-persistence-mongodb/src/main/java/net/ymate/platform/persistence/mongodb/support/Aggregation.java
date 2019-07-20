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
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.persistence.mongodb.IBsonable;
import net.ymate.platform.persistence.mongodb.IMongo;
import org.bson.BSONObject;
import org.bson.conversions.Bson;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/23 下午5:07
 */
public final class Aggregation implements IBsonable {

    private final BasicDBObject pipeline;

    public static Aggregation create() {
        return new Aggregation();
    }

    private Aggregation() {
        pipeline = new BasicDBObject();
    }

    public Aggregation project(Bson project) {
        pipeline.put(IMongo.Opt.PROJECT, project);
        return this;
    }

    public Aggregation project(Fields fields) {
        BasicDBObject dbObject = new BasicDBObject();
        fields.fields().forEach((field) -> dbObject.put(field, 1));
        return project(dbObject);
    }

    public Aggregation match(Bson match) {
        pipeline.put(IMongo.Opt.MATCH, match);
        return this;
    }

    public Aggregation match(Query query) {
        return match(query.toBson());
    }

    public Aggregation redact(Bson expression) {
        pipeline.put(IMongo.Opt.REDACT, expression);
        return this;
    }

    public Aggregation limit(int n) {
        pipeline.put(IMongo.Opt.LIMIT, n);
        return this;
    }

    public Aggregation skip(int n) {
        pipeline.put(IMongo.Opt.SKIP, n);
        return this;
    }

    public Aggregation unwind(String field) {
        if (!field.startsWith("$")) {
            field = "$" + field;
        }
        pipeline.put(IMongo.Opt.UNWIND, field);
        return this;
    }

    public Aggregation group(Bson expression) {
        pipeline.put(IMongo.Opt.GROUP, expression);
        return this;
    }

    public Aggregation group(Operator id, Query... queries) {
        BasicDBObject dbObject = new BasicDBObject(IMongo.Opt.ID, id == null ? null : id.toBson());
        for (Query query : queries) {
            dbObject.putAll((BSONObject) query.toBson());
        }
        return group(dbObject);
    }

    public Aggregation sort(OrderBy orderBy) {
        pipeline.put(IMongo.Opt.SORT, orderBy.toBson());
        return this;
    }

    public Aggregation out(String targetCollection) {
        pipeline.put(IMongo.Opt.OUT, targetCollection);
        return this;
    }

    @Override
    public BasicDBObject toBson() {
        return pipeline;
    }
}
