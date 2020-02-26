/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.webmvc;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.webmvc.annotation.SignatureValidate;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/25 18:41
 * @since 2.1.0
 */
@Ignored
public interface ISignatureValidator {

    /**
     * 验证签名
     *
     * @param owner             所属应用容器实例
     * @param requestMeta       请求映射元数据描述
     * @param signatureValidate 签名验证规则注解
     * @return 返回true表示验证通过
     */
    boolean validate(IWebMvc owner, RequestMeta requestMeta, SignatureValidate signatureValidate);
}
