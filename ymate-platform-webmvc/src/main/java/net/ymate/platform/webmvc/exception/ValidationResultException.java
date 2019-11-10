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
package net.ymate.platform.webmvc.exception;

import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.webmvc.view.IView;

import java.util.Map;

/**
 * 用于在验证器中设置控制器响应
 *
 * @author 刘镇 (suninformation@163.com) on 2018/8/12 下午3:56
 * @since 2.0.6
 */
public class ValidationResultException extends RuntimeException {

    private int httpStatus;

    private IView resultView;

    private Map<String, ValidateResult> validateResults;

    public ValidationResultException(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public ValidationResultException(IView resultView) {
        this.resultView = resultView;
    }

    public ValidationResultException(Map<String, ValidateResult> validateResults) {
        this.validateResults = validateResults;
    }

    public ValidationResultException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public IView getResultView() {
        return resultView;
    }

    public Map<String, ValidateResult> getValidateResults() {
        return validateResults;
    }
}
