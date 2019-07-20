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
package net.ymate.platform.persistence.jdbc.repo.impl;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.persistence.jdbc.repo.IRepositoryDataFilter;
import net.ymate.platform.persistence.jdbc.repo.IRepositoryScriptProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/12/10 下午10:26
 */
public class DefaultRepositoryScriptProcessor implements IRepositoryScriptProcessor {

    private static final Log LOG = LogFactory.getLog(DefaultRepositoryScriptProcessor.class);

    public static final String JAVASCRIPT = "JavaScript";

    private Invocable invocable;

    private boolean filterable;

    private IRepositoryDataFilter filter;

    @Override
    public void init(String scriptStatement) throws Exception {
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(JAVASCRIPT);
        scriptEngine.eval(scriptStatement);
        invocable = (Invocable) scriptEngine;
    }

    @Override
    public String process(String name, Object... params) throws Exception {
        Object result = invocable.invokeFunction(name, params);
        String sqlStr = null;
        if (result instanceof String) {
            sqlStr = (String) result;
        } else {
            try {
                filter = invocable.getInterface(result, IRepositoryDataFilter.class);
                if (filter != null) {
                    sqlStr = filter.sql();
                    filterable = true;
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
                throw new IllegalStateException("Invalid script statement code.");
            }
        }
        return sqlStr;
    }

    @Override
    public boolean isFilterable() {
        return filterable;
    }

    @Override
    public Object doFilter(Object results) {
        if (filterable && filter != null) {
            return filter.filter(results);
        }
        return results;
    }
}
