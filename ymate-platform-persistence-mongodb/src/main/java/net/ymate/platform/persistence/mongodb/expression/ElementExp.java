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
import org.bson.BsonType;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/28 下午2:57
 */
public class ElementExp extends AbstractOperator {

    public static ElementExp exists(boolean exists) {
        ElementExp elementExp = new ElementExp();
        elementExp.addOperator(IMongo.Opt.EXISTS, exists);
        return elementExp;
    }

    public static ElementExp type(BsonType type) {
        ElementExp elementExp = new ElementExp();
        elementExp.addOperator(IMongo.Opt.TYPE, type);
        return elementExp;
    }
}
