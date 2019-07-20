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
import net.ymate.platform.persistence.mongodb.AbstractOperator;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IOperator;
import net.ymate.platform.persistence.mongodb.support.Query;
import org.bson.BSONObject;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/28 下午3:08
 */
public class ProjectionExp extends AbstractOperator {

    public static ProjectionExp elemMatch(IOperator... operators) {
        ProjectionExp projectionExp = new ProjectionExp();
        BasicDBObject dbObject = new BasicDBObject();
        for (IOperator operator : operators) {
            dbObject.putAll((BSONObject) operator.toBson());
        }
        projectionExp.addOperator(IMongo.Opt.ELEM_MATCH, dbObject);
        return projectionExp;
    }

    public static ProjectionExp elemMatch(Query... queries) {
        ProjectionExp projectionExp = new ProjectionExp();
        BasicDBObject dbObject = new BasicDBObject();
        for (Query query : queries) {
            dbObject.putAll((BSONObject) query.toBson());
        }
        projectionExp.addOperator(IMongo.Opt.ELEM_MATCH, dbObject);
        return projectionExp;
    }

    public static ProjectionExp meta(String meta) {
        ProjectionExp projectionExp = new ProjectionExp();
        projectionExp.addOperator(IMongo.Opt.META, meta);
        return projectionExp;
    }

    public static ProjectionExp slice(int slice) {
        ProjectionExp projectionExp = new ProjectionExp();
        projectionExp.addOperator(IMongo.Opt.SLICE, slice);
        return projectionExp;
    }

    public static ProjectionExp slice(int skip, int limit) {
        ProjectionExp projectionExp = new ProjectionExp();
        projectionExp.addOperator(IMongo.Opt.SLICE, new int[]{skip, limit});
        return projectionExp;
    }
}
