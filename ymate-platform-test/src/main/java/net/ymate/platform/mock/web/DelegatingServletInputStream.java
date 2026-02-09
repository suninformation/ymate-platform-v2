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

import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class DelegatingServletInputStream extends ServletInputStream {

    private final InputStream sourceStream;

    private boolean closed = false;

    public DelegatingServletInputStream(InputStream sourceStream) {
        Objects.requireNonNull(sourceStream, "Source InputStream must not be null");
        this.sourceStream = sourceStream;
    }

    public final InputStream getSourceStream() {
        return this.sourceStream;
    }

    @Override
    public int read() throws IOException {
        if (closed) {
            return -1;
        }
        int result = this.sourceStream.read();
        if (result == -1) {
            closed = true;
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            this.sourceStream.close();
            closed = true;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (closed) {
            return -1;
        }
        int result = this.sourceStream.read(b);
        if (result == -1) {
            closed = true;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (closed) {
            return -1;
        }
        int result = this.sourceStream.read(b, off, len);
        if (result == -1) {
            closed = true;
        }
        return result;
    }

    public static class Builder {
        private InputStream sourceStream;

        public static Builder create() {
            return new Builder();
        }

        public Builder sourceStream(InputStream sourceStream) {
            this.sourceStream = sourceStream;
            return this;
        }

        public Builder content(byte[] content) {
            this.sourceStream = new ByteArrayInputStream(content);
            return this;
        }

        public Builder content(String content) {
            this.sourceStream = new ByteArrayInputStream(content.getBytes());
            return this;
        }

        public DelegatingServletInputStream build() {
            Objects.requireNonNull(sourceStream, "Source InputStream must not be null");
            return new DelegatingServletInputStream(sourceStream);
        }
    }
}
