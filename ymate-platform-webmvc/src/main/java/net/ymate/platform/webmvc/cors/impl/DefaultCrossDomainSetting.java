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
package net.ymate.platform.webmvc.cors.impl;

import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.cors.ICrossDomainSetting;
import net.ymate.platform.webmvc.cors.annotation.CrossDomainSetting;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-16 14:31
 * @since 2.1.0
 */
public final class DefaultCrossDomainSetting implements ICrossDomainSetting {

    private boolean optionsAutoReply;

    private boolean allowedCredentials;

    private long maxAge;

    private final Set<String> allowedOrigins = new HashSet<>();

    private final Set<String> allowedMethods = new HashSet<>();

    private final Set<String> allowedHeaders = new HashSet<>();

    private final Set<String> exposedHeaders = new HashSet<>();

    public static Builder builder() {
        return new Builder();
    }

    public static ICrossDomainSetting valueOf(CrossDomainSetting crossDomainSetting) {
        DefaultCrossDomainSetting.Builder builder = DefaultCrossDomainSetting.builder()
                .allowedCredentials(crossDomainSetting.allowedCredentials())
                .optionsAutoReply(crossDomainSetting.optionsAutoReply())
                .maxAge(crossDomainSetting.maxAge())
                .addAllowedOrigin(crossDomainSetting.allowedOrigins())
                .addAllowedHeader(crossDomainSetting.allowedHeaders())
                .addExposedHeader(crossDomainSetting.exposedHeaders());
        for (Type.HttpMethod method : crossDomainSetting.allowedMethods()) {
            builder.addAllowedMethod(method.name());
        }
        return builder.build();
    }

    public DefaultCrossDomainSetting() {
    }

    @Override
    public boolean isOptionsAutoReply() {
        return optionsAutoReply;
    }

    public void setOptionsAutoReply(boolean optionsAutoReply) {
        this.optionsAutoReply = optionsAutoReply;
    }

    @Override
    public boolean isAllowedCredentials() {
        return allowedCredentials;
    }

    public void setAllowedCredentials(boolean allowedCredentials) {
        this.allowedCredentials = allowedCredentials;
    }

    @Override
    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public Set<String> getAllowedOrigins() {
        return Collections.unmodifiableSet(allowedOrigins);
    }

    public void addAllowedOrigin(String... allowedOrigins) {
        if (ArrayUtils.isNotEmpty(allowedOrigins)) {
            Collections.addAll(this.allowedOrigins, allowedOrigins);
        }
    }

    @Override
    public Set<String> getAllowedMethods() {
        return Collections.unmodifiableSet(allowedMethods);
    }

    public void addAllowedMethod(String... allowedMethods) {
        if (ArrayUtils.isNotEmpty(allowedMethods)) {
            Collections.addAll(this.allowedMethods, allowedMethods);
        }
    }

    @Override
    public Set<String> getAllowedHeaders() {
        return Collections.unmodifiableSet(allowedHeaders);
    }

    public void addAllowedHeader(String... allowedHeaders) {
        if (ArrayUtils.isNotEmpty(allowedHeaders)) {
            Collections.addAll(this.allowedHeaders, allowedHeaders);
        }
    }

    @Override
    public Set<String> getExposedHeaders() {
        return Collections.unmodifiableSet(exposedHeaders);
    }

    public void addExposedHeader(String... exposedHeaders) {
        if (ArrayUtils.isNotEmpty(exposedHeaders)) {
            Collections.addAll(this.exposedHeaders, exposedHeaders);
        }
    }

    public static final class Builder {

        private final DefaultCrossDomainSetting setting;

        private Builder() {
            setting = new DefaultCrossDomainSetting();
        }

        public Builder optionsAutoReply(boolean optionsAutoReply) {
            setting.setOptionsAutoReply(optionsAutoReply);
            return this;
        }

        public Builder allowedCredentials(boolean allowedCredentials) {
            setting.setAllowedCredentials(allowedCredentials);
            return this;
        }

        public Builder maxAge(long maxAge) {
            setting.setMaxAge(maxAge);
            return this;
        }

        public Builder addAllowedOrigin(String... allowedOrigins) {
            if (ArrayUtils.isNotEmpty(allowedOrigins)) {
                setting.addAllowedOrigin(allowedOrigins);
            }
            return this;
        }

        public Builder addAllowedMethod(String... allowedMethods) {
            if (ArrayUtils.isNotEmpty(allowedMethods)) {
                setting.addAllowedMethod(allowedMethods);
            }
            return this;
        }

        public Builder addAllowedHeader(String... allowedHeaders) {
            if (ArrayUtils.isNotEmpty(allowedHeaders)) {
                setting.addAllowedHeader(allowedHeaders);
            }
            return this;
        }

        public Builder addExposedHeader(String... exposedHeaders) {
            if (ArrayUtils.isNotEmpty(exposedHeaders)) {
                setting.addExposedHeader(exposedHeaders);
            }
            return this;
        }

        public ICrossDomainSetting build() {
            return setting;
        }
    }
}
