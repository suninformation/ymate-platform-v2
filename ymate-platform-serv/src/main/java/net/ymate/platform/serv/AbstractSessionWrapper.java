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
package net.ymate.platform.serv;

import net.ymate.platform.serv.nio.INioSession;

import java.io.Serializable;

/**
 * @param <SESSION_TYPE> 会话类型
 * @param <SESSION_ID>   会话标识类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/14 2:35 PM
 */
public abstract class AbstractSessionWrapper<SESSION_TYPE extends INioSession, SESSION_ID extends Serializable> implements ISessionWrapper<SESSION_TYPE, SESSION_ID> {

    private static final long serialVersionUID = 1L;
}
