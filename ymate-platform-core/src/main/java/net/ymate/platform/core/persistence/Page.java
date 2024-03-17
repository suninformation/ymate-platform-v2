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
package net.ymate.platform.core.persistence;

/**
 * 分页参数对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/7 下午1:20
 */
public final class Page {

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 分页大小
     */
    private int pageSize;

    /**
     * 当前页号
     */
    private final int page;

    /**
     * 是否执行总记录数统计
     */
    private boolean count;

    /**
     * @since 2.1.3
     */
    public static Page limitOne() {
        return limit(1);
    }

    /**
     * @since 2.1.3
     */
    public static Page limit(int limit) {
        return new Page(1).pageSize(Math.max(1, limit)).count(false);
    }

    public static Page create() {
        return new Page(1);
    }

    public static Page create(Integer page) {
        return new Page(page);
    }

    /**
     * @since 2.1.3
     */
    public static Page createIfNeed(Integer page) {
        return createIfNeed(page, DEFAULT_PAGE_SIZE, true);
    }

    /**
     * @since 2.1.3
     */
    public static Page createIfNeed(Integer page, boolean count) {
        return createIfNeed(page, DEFAULT_PAGE_SIZE, count);
    }

    public static Page createIfNeed(Integer page, Integer pageSize) {
        return createIfNeed(page, pageSize, true);
    }

    /**
     * 根据参数判断是否需要创建分页对象
     *
     * @param page     页号
     * @param pageSize 分页大小
     * @param count    是否执行总记录数统计
     * @return 返回分页对象或null
     * @since 2.1.3
     */
    public static Page createIfNeed(Integer page, Integer pageSize, boolean count) {
        if (page != null && page > 0 && pageSize != null && pageSize > 0) {
            return new Page(page).pageSize(pageSize).count(count);
        }
        return null;
    }

    private Page(Integer page) {
        this.page = page != null && page > 0 ? page : 1;
        this.count = true;
    }

    public int pageSize() {
        return pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
    }

    public Page pageSize(Integer pageSize) {
        this.pageSize = pageSize != null && pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        return this;
    }

    public int page() {
        return page;
    }

    public boolean isCount() {
        return count;
    }

    public Page count(boolean count) {
        this.count = count;
        return this;
    }
}
