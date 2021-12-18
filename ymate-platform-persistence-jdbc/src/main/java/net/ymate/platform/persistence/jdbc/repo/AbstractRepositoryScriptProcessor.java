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
package net.ymate.platform.persistence.jdbc.repo;

import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/12/18 1:58 下午
 * @since 2.1.0
 */
public abstract class AbstractRepositoryScriptProcessor implements IRepositoryScriptProcessor {

    private static final Log LOG = LogFactory.getLog(AbstractRepositoryScriptProcessor.class);

    private Invocable invocable;

    private boolean filterable;

    private IRepositoryDataFilter filter;

    private boolean initialized;

    @Override
    public void initialize(String scriptStatement) throws Exception {
        if (!initialized) {
            ScriptEngine scriptEngine = doBuildScriptEngine();
            invocable = doBuildInvocable(scriptEngine, scriptStatement);
            initialized = true;
        }
    }

    /**
     * @return 返回构建的脚本引擎实例对象
     */
    public abstract ScriptEngine doBuildScriptEngine();

    public Invocable doBuildInvocable(ScriptEngine scriptEngine, String scriptStatement) throws Exception {
        scriptEngine.eval(scriptStatement);
        return (Invocable) scriptEngine;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public String process(String name, Object... params) throws Exception {
        if (initialized) {
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
        return null;
    }

    @Override
    public boolean isFilterable() {
        return initialized && filterable;
    }

    @Override
    public Object filter(Object results) {
        if (initialized && filterable && filter != null) {
            return filter.filter(results);
        }
        return results;
    }
}
