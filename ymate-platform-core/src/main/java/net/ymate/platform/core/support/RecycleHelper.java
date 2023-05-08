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
package net.ymate.platform.core.support;

import net.ymate.platform.commons.ConcurrentHashSet;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.commons.util.ThreadUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 对象资源回收助手
 * <p>
 * 主要是开辟一块空地，让涉及到需要进行资源销毁的对象主动的将其销毁方法和手段注册进来，从而达到通过此对象一次性全部回收处理；
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/4 下午2:15
 */
public final class RecycleHelper {

    private static final Log LOG = LogFactory.getLog(RecycleHelper.class);

    private static final RecycleHelper INSTANCE = new RecycleHelper();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> INSTANCE.recycle(true)));
    }

    private final Set<IDestroyable> destroyableSet = new ConcurrentHashSet<>();

    /**
     * @return 获取全局实例对象
     */
    public static RecycleHelper getInstance() {
        return INSTANCE;
    }

    /**
     * @return 创建新的实例对象
     */
    public static RecycleHelper create() {
        return new RecycleHelper();
    }

    private RecycleHelper() {
    }

    /**
     * 注册资源回收对象
     *
     * @param destroyable 资源回收对象
     * @return 返回当前对象资源回收助手实例对象
     */
    public RecycleHelper register(IDestroyable destroyable) {
        if (destroyable != null) {
            destroyableSet.add(destroyable);
        }
        return this;
    }

    /**
     * @return 返回待回收资源数量
     */
    public int size() {
        return destroyableSet.size();
    }

    private void doRecycling(IDestroyable destroyable) {
        if (destroyable != null) {
            try {
                destroyable.close();
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(String.format("An exception occurred while destroying object [%s].", destroyable.getClass().getName()), RuntimeUtils.unwrapThrow(e));
                }
            }
        }
    }

    /**
     * 执行资源回收
     */
    public void recycle() {
        destroyableSet.forEach(this::doRecycling);
    }

    /**
     * 执行资源回收
     *
     * @param async 是否异步
     */
    public void recycle(boolean async) {
        if (!destroyableSet.isEmpty()) {
            if (async) {
                ExecutorService executorService = ThreadUtils.newFixedThreadPool(destroyableSet.size());
                for (IDestroyable destroyable : destroyableSet) {
                    executorService.submit(() -> doRecycling(destroyable));
                }
                executorService.shutdown();
            } else {
                recycle();
            }
        }
    }
}
