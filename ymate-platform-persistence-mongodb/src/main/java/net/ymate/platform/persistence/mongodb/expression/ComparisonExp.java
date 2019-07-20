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

import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.mongodb.AbstractOperator;
import net.ymate.platform.persistence.mongodb.IMongo;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/27 下午6:17
 */
public class ComparisonExp extends AbstractOperator {

    public static ComparisonExp cmp(Object exp1, Object exp2) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.CMP, new Object[]{exp1, exp2});
        return comparisonExp;
    }

    public static ComparisonExp eq(Object param) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.EQ, param);
        return comparisonExp;
    }

    public static ComparisonExp eq(Params params) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.EQ, params);
        return comparisonExp;
    }

    public static ComparisonExp ne(Object param) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.NE, param);
        return comparisonExp;
    }

    public static ComparisonExp ne(Params params) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.NE, params);
        return comparisonExp;
    }

    public static ComparisonExp gt(Object param) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.GT, param);
        return comparisonExp;
    }

    public static ComparisonExp gt(Params params) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.GT, params);
        return comparisonExp;
    }

    public static ComparisonExp gte(Object param) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.GTE, param);
        return comparisonExp;
    }

    public static ComparisonExp gte(Params params) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.GTE, params);
        return comparisonExp;
    }

    public static ComparisonExp lt(Object param) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.LT, param);
        return comparisonExp;
    }

    public static ComparisonExp lt(Params params) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.LT, params);
        return comparisonExp;
    }

    public static ComparisonExp lte(Object param) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.LTE, param);
        return comparisonExp;
    }

    public static ComparisonExp lte(Params params) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.LTE, params);
        return comparisonExp;
    }

    public static ComparisonExp in(Params values) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.IN, values.toArray());
        return comparisonExp;
    }

    public static ComparisonExp nin(Params values) {
        ComparisonExp comparisonExp = new ComparisonExp();
        comparisonExp.addOperator(IMongo.Opt.NIN, values.toArray());
        return comparisonExp;
    }
}
