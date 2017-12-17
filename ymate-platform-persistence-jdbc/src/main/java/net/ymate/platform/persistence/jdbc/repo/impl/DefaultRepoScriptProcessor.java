/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.persistence.jdbc.repo.IRepoDataFilter;
import net.ymate.platform.persistence.jdbc.repo.IRepoScriptProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/12/10 下午10:26
 * @version 1.0
 */
public class DefaultRepoScriptProcessor implements IRepoScriptProcessor {

    private static final Log _LOG = LogFactory.getLog(DefaultRepoScriptProcessor.class);

    public static final String JAVASCRIPT = "JavaScript";

    private Invocable __inv;

    private boolean __filterable;

    private IRepoDataFilter __filter;

    @Override
    public String getName() {
        return JAVASCRIPT;
    }

    @Override
    public void init(String scriptStatement) throws Exception {
        ScriptEngine _engine = new ScriptEngineManager().getEngineByName(JAVASCRIPT);
        _engine.eval(scriptStatement);
        __inv = (Invocable) _engine;
    }

    @Override
    public String process(String name, Object... params) throws Exception {
        Object _result = __inv.invokeFunction(name, params);
        String _targetSQL = null;
        if (_result instanceof String) {
            _targetSQL = (String) _result;
        } else {
            try {
                __filter = __inv.getInterface(_result, IRepoDataFilter.class);
                if (__filter != null) {
                    _targetSQL = __filter.sql();
                    __filterable = true;
                }
            } catch (Exception e) {
                _LOG.warn("", RuntimeUtils.unwrapThrow(e));
                throw new IllegalStateException("Invalid script statement code.");
            }
        }
        return _targetSQL;
    }

    @Override
    public boolean isFilterable() {
        return __filterable;
    }

    @Override
    public Object doFilter(Object results) {
        if (__filterable && __filter != null) {
            return __filter.filter(results);
        }
        return results;
    }
}
