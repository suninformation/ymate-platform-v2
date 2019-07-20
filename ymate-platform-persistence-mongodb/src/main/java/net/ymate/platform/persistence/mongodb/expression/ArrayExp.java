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
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.mongodb.AbstractOperator;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IOperator;
import net.ymate.platform.persistence.mongodb.support.Query;
import org.bson.BSONObject;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/28 下午3:03
 */
public class ArrayExp extends AbstractOperator {

    public static ArrayExp all(Params params) {
        ArrayExp arrayExp = new ArrayExp();
        arrayExp.addOperator(IMongo.Opt.ALL, params.toArray());
        return arrayExp;
    }

    public static ArrayExp elemMatch(IOperator... operators) {
        ArrayExp arrayExp = new ArrayExp();
        BasicDBObject dbObject = new BasicDBObject();
        for (IOperator operator : operators) {
            dbObject.putAll((BSONObject) operator.toBson());
        }
        arrayExp.addOperator(IMongo.Opt.ELEM_MATCH, dbObject);
        return arrayExp;
    }

    public static ArrayExp elemMatch(Query... queries) {
        ArrayExp arrayExp = new ArrayExp();
        BasicDBObject dbObject = new BasicDBObject();
        for (Query query : queries) {
            dbObject.putAll((BSONObject) query.toBson());
        }
        arrayExp.addOperator(IMongo.Opt.ELEM_MATCH, dbObject);
        return arrayExp;
    }

    public static ArrayExp size(int size) {
        ArrayExp arrayExp = new ArrayExp();
        arrayExp.addOperator(IMongo.Opt.SIZE, size);
        return arrayExp;
    }

    public static ArrayExp size(Object size) {
        ArrayExp arrayExp = new ArrayExp();
        arrayExp.addOperator(IMongo.Opt.SIZE, size);
        return arrayExp;
    }
}
