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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.util.Objects;

public class MockRequestDispatcher implements RequestDispatcher {

    private final Log LOG = LogFactory.getLog(getClass());

    private final String resource;

    public MockRequestDispatcher(String resource) {
        Objects.requireNonNull(resource, "resource must not be null");
        this.resource = resource;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(response, "Response must not be null");
        if (response.isCommitted()) {
            throw new IllegalStateException("Cannot perform forward - response is already committed");
        }
        getMockHttpServletResponse(response).setForwardedUrl(this.resource);
        if (LOG.isDebugEnabled()) {
            LOG.debug("MockRequestDispatcher: forwarding to [" + this.resource + "]");
        }
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(response, "Response must not be null");
        getMockHttpServletResponse(response).addIncludedUrl(this.resource);
        if (LOG.isDebugEnabled()) {
            LOG.debug("MockRequestDispatcher: including [" + this.resource + "]");
        }
    }

    protected MockHttpServletResponse getMockHttpServletResponse(ServletResponse response) {
        if (response instanceof MockHttpServletResponse) {
            return (MockHttpServletResponse) response;
        }
        if (response instanceof HttpServletResponseWrapper) {
            return getMockHttpServletResponse(((HttpServletResponseWrapper) response).getResponse());
        }
        throw new IllegalArgumentException("MockRequestDispatcher requires MockHttpServletResponse");
    }

    public static class Builder {
        private String resource;

        public static Builder create() {
            return new Builder();
        }

        public Builder resource(String resource) {
            this.resource = resource;
            return this;
        }

        public MockRequestDispatcher build() {
            return new MockRequestDispatcher(resource);
        }
    }
}
