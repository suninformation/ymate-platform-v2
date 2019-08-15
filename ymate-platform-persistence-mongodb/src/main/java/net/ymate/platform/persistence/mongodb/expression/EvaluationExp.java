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

import java.util.regex.Pattern;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/28 下午3:00
 */
public class EvaluationExp extends AbstractOperator {

    public static EvaluationExp mod(int divisor, int remainder) {
        EvaluationExp evaluationExp = new EvaluationExp();
        evaluationExp.addOperator(IMongo.Opt.MOD, new int[]{divisor, remainder});
        return evaluationExp;
    }

    public static EvaluationExp mod(String divisor, String remainder) {
        EvaluationExp evaluationExp = new EvaluationExp();
        evaluationExp.addOperator(IMongo.Opt.MOD, new String[]{divisor, remainder});
        return evaluationExp;
    }

    public static EvaluationExp regex(String regex) {
        EvaluationExp evaluationExp = new EvaluationExp();
        evaluationExp.addOperator(IMongo.Opt.REGEX, Pattern.compile(regex));
        return evaluationExp;
    }

    public static EvaluationExp text(String search) {
        return text(search, null);
    }

    public static EvaluationExp text(String search, String language) {
        EvaluationExp evaluationExp = new EvaluationExp();
        BasicDBObject dbObject = new BasicDBObject(IMongo.Opt.SEARCH, search);
        if (language != null) {
            dbObject.put(IMongo.Opt.LANGUAGE, language);
        }
        evaluationExp.addOperator(IMongo.Opt.TEXT, dbObject);
        return evaluationExp;
    }

    public static EvaluationExp where(String jsFunction) {
        EvaluationExp evaluationExp = new EvaluationExp();
        evaluationExp.addOperator(IMongo.Opt.WHERE, jsFunction);
        return evaluationExp;
    }
}
