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
package net.ymate.platform.serv.nio.support;

import java.nio.ByteBuffer;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/13 下午3:29
 */
public final class ByteBufferBuilder {

    private ByteBuffer byteBuffer;

    public static ByteBufferBuilder allocate() {
        return new ByteBufferBuilder(0);
    }

    public static ByteBufferBuilder allocate(int capacity) {
        return new ByteBufferBuilder(capacity);
    }

    public static ByteBufferBuilder allocateDirect(int capacity) {
        return new ByteBufferBuilder(ByteBuffer.allocateDirect(capacity));
    }

    public static ByteBufferBuilder wrap(ByteBuffer buffer) {
        return new ByteBufferBuilder(buffer);
    }

    private ByteBufferBuilder(int capacity) {
        if (capacity > 0) {
            byteBuffer = ByteBuffer.allocate(capacity);
        }
    }

    private ByteBufferBuilder(ByteBuffer buffer) {
        byteBuffer = buffer;
    }

    private void bufferSafety(int length) {
        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.allocate(length * 2);
        } else {
            int currentSize = byteBuffer.capacity();
            int newSize = byteBuffer.position() + length;
            while (newSize > currentSize) {
                currentSize *= 2;
            }
            if (currentSize != byteBuffer.capacity()) {
                ByteBuffer newBuffer;
                if (byteBuffer.isDirect()) {
                    newBuffer = ByteBuffer.allocateDirect(currentSize);
                } else {
                    newBuffer = ByteBuffer.allocate(currentSize);
                }
                newBuffer.put(byteBuffer.array());
                newBuffer.position(byteBuffer.position());
                byteBuffer = newBuffer;
            }
        }
    }

    public ByteBufferBuilder append(byte[] src, int offset, int length) {
        bufferSafety(length);
        byteBuffer.put(src, offset, length);
        return this;
    }

    public ByteBufferBuilder append(byte value) {
        return append(new byte[]{value});
    }

    public ByteBufferBuilder append(byte[] src) {
        return append(src, 0, src.length);
    }

    public ByteBufferBuilder append(char c) {
        bufferSafety(2);
        byteBuffer.putChar(c);
        return this;
    }

    public ByteBufferBuilder append(short value) {
        bufferSafety(2);
        byteBuffer.putShort(value);
        return this;
    }

    public ByteBufferBuilder append(long value) {
        bufferSafety(8);
        byteBuffer.putLong(value);
        return this;
    }

    public ByteBufferBuilder append(int value) {
        bufferSafety(4);
        byteBuffer.putInt(value);
        return this;
    }

    public ByteBufferBuilder append(String value) {
        append(value.getBytes());
        return this;
    }

    public ByteBufferBuilder append(ByteBuffer buffer) {
        bufferSafety(buffer.capacity());
        byteBuffer.put(buffer);
        return this;
    }

    public byte get() {
        return byteBuffer.get();
    }

    public ByteBufferBuilder get(byte[] dst) {
        byteBuffer.get(dst);
        return this;
    }

    public ByteBufferBuilder get(byte[] dst, int offset, int length) {
        byteBuffer.get(dst, offset, length);
        return this;
    }

    public short getShort() {
        return byteBuffer.getShort();
    }

    public int getInt() {
        return byteBuffer.getInt();
    }

    public long getLong() {
        return byteBuffer.getLong();
    }

    public ByteBufferBuilder clear() {
        byteBuffer.clear();
        return this;
    }

    public ByteBufferBuilder flip() {
        byteBuffer.flip();
        return this;
    }

    public ByteBufferBuilder mark() {
        byteBuffer.mark();
        return this;
    }

    public ByteBufferBuilder reset() {
        byteBuffer.reset();
        return this;
    }

    public int remaining() {
        return byteBuffer.remaining();
    }

    public ByteBufferBuilder rewind() {
        byteBuffer.rewind();
        return this;
    }

    public int position() {
        return byteBuffer.position();
    }

    public ByteBufferBuilder position(int newPosition) {
        byteBuffer.position(newPosition);
        return this;
    }

    public int limit() {
        return byteBuffer.limit();
    }

    public ByteBufferBuilder limit(int newLimit) {
        byteBuffer.limit(newLimit);
        return this;
    }

    public ByteBufferBuilder compact() {
        byteBuffer.compact();
        return this;
    }

    public ByteBufferBuilder duplicate() {
        return ByteBufferBuilder.wrap(byteBuffer.duplicate());
    }

    public byte[] array() {
        return byteBuffer.array();
    }

    public ByteBuffer buffer() {
        return byteBuffer;
    }
}
