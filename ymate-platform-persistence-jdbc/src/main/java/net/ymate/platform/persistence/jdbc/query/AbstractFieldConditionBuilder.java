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
 package net.ymate.platform.persistence.jdbc.query;

 import net.ymate.platform.persistence.jdbc.IDatabase;
 import net.ymate.platform.persistence.jdbc.JDBC;
 import org.apache.commons.lang3.StringUtils;

 /**
  * @author 刘镇 (suninformation@163.com) on 2021/01/10 10:21
  * @since 2.1.0
  */
 public abstract class AbstractFieldConditionBuilder {

     protected final IDatabase owner;

     protected final String dataSourceName;

     protected final String prefix;

     public AbstractFieldConditionBuilder(IDatabase owner, String dataSourceName, String prefix) {
         if (owner == null) {
             owner = JDBC.get();
         }
         if (StringUtils.isBlank(dataSourceName)) {
             dataSourceName = owner.getConfig().getDefaultDataSourceName();
         }
         this.owner = owner;
         this.dataSourceName = dataSourceName;
         this.prefix = prefix;
     }

     protected FieldCondition createFieldCondition(String fieldName) {
         return new FieldCondition(owner, dataSourceName, prefix, fieldName);
     }
 }
