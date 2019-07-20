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

import net.ymate.platform.persistence.mongodb.AbstractOperator;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IOperator;
import net.ymate.platform.persistence.mongodb.support.Query;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/28 下午2:40
 */
public class LogicalExp extends AbstractOperator {

    @SuppressWarnings("unchecked")
    public static LogicalExp or(Query... queries) {
        LogicalExp logicalExp = new LogicalExp();
        List<Bson> bsons = (List<Bson>) logicalExp.toBson().get(IMongo.Opt.OR);
        if (bsons == null) {
            bsons = new ArrayList<>();
            logicalExp.addOperator(IMongo.Opt.OR, bsons);
        }
        for (Query query : queries) {
            bsons.add(query.toBson());
        }
        return logicalExp;
    }

    @SuppressWarnings("unchecked")
    public static LogicalExp and(Query... queries) {
        LogicalExp logicalExp = new LogicalExp();
        List<Bson> bsons = (List<Bson>) logicalExp.toBson().get(IMongo.Opt.AND);
        if (bsons == null) {
            bsons = new ArrayList<>();
            logicalExp.addOperator(IMongo.Opt.AND, bsons);
        }
        for (Query query : queries) {
            bsons.add(query.toBson());
        }
        return logicalExp;
    }

    public static LogicalExp not(IOperator operator) {
        LogicalExp logicalExp = new LogicalExp();
        logicalExp.addOperator(IMongo.Opt.NOT, operator.toBson());
        return logicalExp;
    }

    @SuppressWarnings("unchecked")
    public static LogicalExp nor(Query... queries) {
        LogicalExp logicalExp = new LogicalExp();
        List<Bson> bsons = (List<Bson>) logicalExp.toBson().get(IMongo.Opt.NOR);
        if (bsons == null) {
            bsons = new ArrayList<>();
            logicalExp.addOperator(IMongo.Opt.NOR, bsons);
        }
        for (Query query : queries) {
            bsons.add(query.toBson());
        }
        return logicalExp;
    }
}
