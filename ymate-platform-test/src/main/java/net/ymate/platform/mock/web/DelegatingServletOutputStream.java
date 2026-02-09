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

import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class DelegatingServletOutputStream extends ServletOutputStream {

    private final OutputStream targetStream;

    public DelegatingServletOutputStream(OutputStream targetStream) {
        Objects.requireNonNull(targetStream, "Target OutputStream must not be null");
        this.targetStream = targetStream;
    }

    public final OutputStream getTargetStream() {
        return this.targetStream;
    }

    @Override
    public void write(int b) throws IOException {
        this.targetStream.write(b);
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        this.targetStream.flush();
    }

    @Override
    public void close() throws IOException {
        this.targetStream.close();
    }

    public static class Builder {
        private OutputStream targetStream;

        public static Builder create() {
            return new Builder();
        }

        public Builder targetStream(OutputStream targetStream) {
            this.targetStream = targetStream;
            return this;
        }

        public Builder byteArray() {
            this.targetStream = new ByteArrayOutputStream();
            return this;
        }

        public DelegatingServletOutputStream build() {
            Objects.requireNonNull(targetStream, "Target OutputStream must not be null");
            return new DelegatingServletOutputStream(targetStream);
        }
    }
}
