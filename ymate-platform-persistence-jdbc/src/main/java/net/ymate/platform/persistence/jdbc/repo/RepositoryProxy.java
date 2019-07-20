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
package net.ymate.platform.persistence.jdbc.repo;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.beans.annotation.Order;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;
import net.ymate.platform.core.configuration.IConfiguration;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseSession;
import net.ymate.platform.persistence.jdbc.base.IResultSetHandler;
import net.ymate.platform.persistence.jdbc.query.SQL;
import net.ymate.platform.persistence.jdbc.repo.annotation.Repository;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBC存储器代理
 *
 * @author 刘镇 (suninformation@163.com) on 16/4/22 下午2:06
 */
@Order(80000)
public class RepositoryProxy implements IProxy {

    private static final Log LOG = LogFactory.getLog(RepositoryProxy.class);

    private final IDatabase owner;

    public RepositoryProxy(IDatabase owner) {
        this.owner = owner;
    }

    private IDatabaseSession doOpenSession(IProxyChain proxyChain, Repository repositoryAnn) throws Exception {
        IDatabaseSession session = null;
        if (repositoryAnn != null) {
            if (StringUtils.isNotBlank(repositoryAnn.dsName())) {
                session = owner.openSession(repositoryAnn.dsName());
            } else {
                repositoryAnn = proxyChain.getTargetClass().getAnnotation(Repository.class);
                if (repositoryAnn != null) {
                    if (StringUtils.isNotBlank(repositoryAnn.dsName())) {
                        session = owner.openSession(repositoryAnn.dsName());
                    } else {
                        session = owner.openSession();
                    }
                }
            }
        }
        return session;
    }

    private SQL doBuildSqlStr(String targetSql, Method targetMethod, Object[] params, boolean page) {
        Map<String, Object> paramMap = new HashMap<>(params.length);
        String[] paramNames = ClassUtils.getMethodParamNames(targetMethod);
        if (paramNames != null && paramNames.length > 0) {
            for (int idx = 0; idx < paramNames.length - (page ? 2 : 1); idx++) {
                paramMap.put(paramNames[idx], params[idx]);
            }
            if (!paramMap.isEmpty()) {
                ExpressionUtils exp = ExpressionUtils.bind(targetSql);
                Params paramValues = Params.create();
                exp.getVariables().stream().peek((paramName) -> exp.set(paramName, "?")).forEachOrdered((paramName) -> paramValues.add(paramMap.get(paramName)));
                return SQL.create(exp.getResult()).param(paramValues);
            }
        }
        return SQL.create(targetSql);
    }

    @Override
    public Object doProxy(IProxyChain proxyChain) throws Throwable {
        Repository repositoryAnn = proxyChain.getTargetMethod().getAnnotation(Repository.class);
        if (repositoryAnn != null) {
            try (IDatabaseSession session = doOpenSession(proxyChain, repositoryAnn)) {
                IRepositoryScriptProcessor processor = null;
                String sqlStr = repositoryAnn.value();
                if (StringUtils.isBlank(sqlStr) && proxyChain.getTargetObject() instanceof IRepository) {
                    try {
                        IConfiguration configuration = ((IRepository) proxyChain.getTargetObject()).getConfig();
                        if (configuration != null) {
                            String keyStr = StringUtils.lowerCase(String.format("%s_%s", repositoryAnn.item(), session.getConnectionHolder().getDialect().getName()));
                            Map<String, String> statementMap = configuration.getMap(keyStr);
                            if (statementMap == null || statementMap.isEmpty()) {
                                keyStr = StringUtils.lowerCase(repositoryAnn.item());
                                statementMap = configuration.getMap(keyStr);
                            }
                            if (statementMap == null || statementMap.isEmpty()) {
                                throw new NullArgumentException(keyStr);
                            } else {
                                sqlStr = configuration.getString(keyStr);
                            }
                            String languageType = StringUtils.trimToNull(statementMap.get("language"));
                            if (StringUtils.isNotBlank(languageType)) {
                                processor = IRepositoryScriptProcessor.Manager.getScriptProcessor(languageType);
                                if (processor != null) {
                                    processor.init(sqlStr);
                                    sqlStr = processor.process(repositoryAnn.item(), proxyChain.getMethodParams());
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                        }
                    }
                }
                if (StringUtils.isNotBlank(sqlStr)) {
                    Object result;
                    if (repositoryAnn.update()) {
                        result = session.executeForUpdate(doBuildSqlStr(sqlStr, proxyChain.getTargetMethod(), proxyChain.getMethodParams(), repositoryAnn.page()));
                    } else {
                        Page page = repositoryAnn.page() ? (Page) proxyChain.getMethodParams()[proxyChain.getMethodParams().length - 2] : null;
                        result = session.find(doBuildSqlStr(sqlStr, proxyChain.getTargetMethod(), proxyChain.getMethodParams(), repositoryAnn.page()), IResultSetHandler.ARRAY, page);
                        if (processor != null && processor.isFilterable()) {
                            result = processor.doFilter(result);
                        }
                    }
                    if (repositoryAnn.useFilter()) {
                        // 将执行结果赋予目标方法的最后一个参数
                        int position = proxyChain.getMethodParams().length - 1;
                        Object lastParam = proxyChain.getMethodParams()[position];
                        Class<?> paramType = lastParam != null ? proxyChain.getMethodParams()[position].getClass() : null;
                        if (paramType != null && paramType.isArray()) {
                            if (result != null) {
                                proxyChain.getMethodParams()[position] = ArrayUtils.add((Object[]) proxyChain.getMethodParams()[position], result);
                            }
                        } else {
                            proxyChain.getMethodParams()[position] = result;
                        }
                    } else {
                        return result;
                    }
                }
            }
        }
        return proxyChain.doProxyChain();
    }
}
