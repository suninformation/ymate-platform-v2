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
package net.ymate.platform.persistence.jdbc.repo;

import net.ymate.platform.configuration.IConfiguration;
import net.ymate.platform.core.beans.annotation.Order;
import net.ymate.platform.core.beans.annotation.Proxy;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.ExpressionUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.persistence.IResultSet;
import net.ymate.platform.persistence.Params;
import net.ymate.platform.persistence.jdbc.*;
import net.ymate.platform.persistence.jdbc.base.IResultSetHandler;
import net.ymate.platform.persistence.jdbc.query.SQL;
import net.ymate.platform.persistence.jdbc.repo.annotation.Repository;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBC存储器代理
 *
 * @author 刘镇 (suninformation@163.com) on 16/4/22 下午2:06
 * @version 1.0
 */
@Proxy(annotation = Repository.class, order = @Order(888))
public class RepoProxy implements IProxy {

    private static final Log _LOG = LogFactory.getLog(RepoProxy.class);

    @Override
    public Object doProxy(IProxyChain proxyChain) throws Throwable {
        Repository _repo = proxyChain.getTargetMethod().getAnnotation(Repository.class);
        if (_repo == null) {
            return proxyChain.doProxyChain();
        }
        //
        IDatabase _db = JDBC.get(proxyChain.getProxyFactory().getOwner());
        IConnectionHolder _connHolder = null;
        if (StringUtils.isNotBlank(_repo.dsName())) {
            _connHolder = _db.getConnectionHolder(_repo.dsName());
        } else {
            Repository _superRepo = proxyChain.getTargetClass().getAnnotation(Repository.class);
            if (StringUtils.isNotBlank(_superRepo.dsName())) {
                _connHolder = _db.getConnectionHolder(_superRepo.dsName());
            } else {
                _connHolder = _db.getDefaultConnectionHolder();
            }
        }
        //
        IRepoScriptProcessor _processor = null;
        String _targetSQL = _repo.value();
        if (StringUtils.isBlank(_targetSQL)) {
            try {
                IConfiguration _conf = ((IRepository) proxyChain.getTargetObject()).getConfig();
                String _keyStr = StringUtils.lowerCase(_repo.item() + "_" + _connHolder.getDialect().getName());
                Map<String, String> _statementMap = _conf.getMap(_keyStr);
                if (_statementMap == null || _statementMap.isEmpty()) {
                    _keyStr = StringUtils.lowerCase(_repo.item());
                    _statementMap = _conf.getMap(_keyStr);
                }
                if (_statementMap == null || _statementMap.isEmpty()) {
                    throw new NullArgumentException(_keyStr);
                } else {
                    _targetSQL = _conf.getString(_keyStr);
                }
                String _targetType = StringUtils.trimToNull(_statementMap.get("language"));
                if (StringUtils.isNotBlank(_targetType)) {
                    _processor = IRepoScriptProcessor.Manager.getScriptProcessor(_targetType);
                    if (_processor != null) {
                        _processor.init(_targetSQL);
                        _targetSQL = _processor.process(_repo.item(), proxyChain.getMethodParams());
                    }
                }
            } catch (Exception e) {
                _LOG.warn("", RuntimeUtils.unwrapThrow(e));
            }
        }
        if (StringUtils.isNotBlank(_targetSQL)) {
            Object _result;
            switch (_repo.type()) {
                case UPDATE:
                    _result = __doUpdate(_db, _connHolder, _targetSQL, proxyChain.getTargetMethod(), proxyChain.getMethodParams());
                    break;
                default:
                    _result = __doQuery(_db, _connHolder, _targetSQL, proxyChain.getTargetMethod(), proxyChain.getMethodParams());
                    if (_processor != null && _processor.isFilterable()) {
                        _result = _processor.doFilter(_result);
                    }
            }
            if (_repo.useFilter()) {
                // 将执行结果赋予目标方法的最后一个参数
                int _position = proxyChain.getMethodParams().length - 1;
                Object _lastParam = proxyChain.getMethodParams()[_position];
                Class<?> _paramType = _lastParam != null ? proxyChain.getMethodParams()[_position].getClass() : null;
                if (_paramType != null && _paramType.isArray()) {
                    if (_result != null) {
                        proxyChain.getMethodParams()[_position] = ArrayUtils.add((Object[]) proxyChain.getMethodParams()[_position], _result);
                    }
                } else {
                    proxyChain.getMethodParams()[_position] = _result;
                }
            } else {
                return _result;
            }
        }
        return proxyChain.doProxyChain();
    }

    private IResultSet<Object[]> __doQuery(IDatabase db, IConnectionHolder connHolder, final String targetSql, final Method targetMethod, final Object[] params) throws Exception {
        return db.openSession(connHolder, new ISessionExecutor<IResultSet<Object[]>>() {
            @Override
            public IResultSet<Object[]> execute(ISession session) throws Exception {
                return session.find(__buildSQL(targetSql, targetMethod, params), IResultSetHandler.ARRAY);
            }
        });
    }

    private int __doUpdate(IDatabase db, IConnectionHolder connHolder, final String targetSql, final Method targetMethod, final Object[] params) throws Exception {
        return db.openSession(connHolder, new ISessionExecutor<Integer>() {
            @Override
            public Integer execute(ISession session) throws Exception {
                return session.executeForUpdate(__buildSQL(targetSql, targetMethod, params));
            }
        });
    }

    private SQL __buildSQL(String targetSql, Method targetMethod, Object[] params) {
        Map<String, Object> _paramMap = new HashMap<String, Object>();
        String[] _paramNames = ClassUtils.getMethodParamNames(targetMethod);
        if (_paramNames != null && _paramNames.length > 0) {
            for (int _idx = 0; _idx < _paramNames.length - 1; _idx++) {
                _paramMap.put(_paramNames[_idx], params[_idx]);
            }
            if (!_paramMap.isEmpty()) {
                ExpressionUtils _exp = ExpressionUtils.bind(targetSql);
                Params _paramValues = Params.create();
                for (String _paramName : _exp.getVariables()) {
                    _exp.set(_paramName, "?");
                    _paramValues.add(_paramMap.get(_paramName));
                }
                return SQL.create(_exp.getResult()).param(_paramValues);
            }
        }
        return SQL.create(targetSql);
    }
}
