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

import javax.servlet.SessionCookieConfig;

public class MockSessionCookieConfig implements SessionCookieConfig {

    private String name;

    private String domain;

    private String path;

    private String comment;

    private boolean httpOnly;

    private boolean secure;

    private int maxAge = -1;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String getDomain() {
        return this.domain;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    @Override
    public boolean isHttpOnly() {
        return this.httpOnly;
    }

    @Override
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public boolean isSecure() {
        return this.secure;
    }

    @Override
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public int getMaxAge() {
        return this.maxAge;
    }

    public static class Builder {
        private String name;
        private String domain;
        private String path;
        private String comment;
        private boolean httpOnly = false;
        private boolean secure = false;
        private int maxAge = -1;

        public static Builder create() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder httpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }

        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        public Builder maxAge(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public MockSessionCookieConfig build() {
            MockSessionCookieConfig cookieConfig = new MockSessionCookieConfig();
            cookieConfig.setName(name);
            cookieConfig.setDomain(domain);
            cookieConfig.setPath(path);
            cookieConfig.setComment(comment);
            cookieConfig.setHttpOnly(httpOnly);
            cookieConfig.setSecure(secure);
            cookieConfig.setMaxAge(maxAge);
            return cookieConfig;
        }
    }
}
