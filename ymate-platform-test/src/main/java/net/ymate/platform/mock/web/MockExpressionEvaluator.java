/*
 * Copyright 2007-present the original author or authors.
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
package net.ymate.platform.mock.web;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

@SuppressWarnings("deprecation")
public class MockExpressionEvaluator extends javax.servlet.jsp.el.ExpressionEvaluator {

    private final PageContext pageContext;

    public MockExpressionEvaluator(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    @Override
    public javax.servlet.jsp.el.Expression parseExpression(final String expression, final Class expectedType,
                                                           final javax.servlet.jsp.el.FunctionMapper functionMapper) throws javax.servlet.jsp.el.ELException {
        return new javax.servlet.jsp.el.Expression() {
            @Override
            public Object evaluate(javax.servlet.jsp.el.VariableResolver variableResolver) throws javax.servlet.jsp.el.ELException {
                return doEvaluate(expression, expectedType, functionMapper);
            }
        };
    }

    @Override
    public Object evaluate(String expression, Class expectedType, javax.servlet.jsp.el.VariableResolver variableResolver,
                           javax.servlet.jsp.el.FunctionMapper functionMapper) throws javax.servlet.jsp.el.ELException {
        if (variableResolver != null) {
            throw new IllegalArgumentException("Custom VariableResolver not supported");
        }
        return doEvaluate(expression, expectedType, functionMapper);
    }

    @SuppressWarnings("rawtypes")
    protected Object doEvaluate(String expression, Class expectedType, javax.servlet.jsp.el.FunctionMapper functionMapper)
            throws javax.servlet.jsp.el.ELException {
        if (functionMapper != null) {
            throw new IllegalArgumentException("Custom FunctionMapper not supported");
        }
        try {
            return ExpressionEvaluatorManager.evaluate("JSP EL expression", expression, expectedType, this.pageContext);
        } catch (JspException ex) {
            throw new javax.servlet.jsp.el.ELException("Parsing of JSP EL expression \"" + expression + "\" failed", ex);
        }
    }

    public static class Builder {
        private PageContext pageContext;

        public static Builder create() {
            return new Builder();
        }

        public Builder pageContext(PageContext pageContext) {
            this.pageContext = pageContext;
            return this;
        }

        public Builder mockPageContext() {
            this.pageContext = new MockPageContext();
            return this;
        }

        public MockExpressionEvaluator build() {
            return new MockExpressionEvaluator(pageContext);
        }
    }
}
