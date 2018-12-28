/*
 * Copyright 2007-2018 the original author or authors.
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

import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.core.util.ThreadUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 对象资源回收助手
 * <p>
 * 主要是开辟一块空地，让涉及到需要进行资源销毁的对象主动的将其销毁方法和手段注册进来，从而达到通过此对象一次性全部回收处理；
 *
 * @author 刘镇 (suninformation@163.com) on 2018/4/4 下午2:15
 * @version 1.0
 */
public final class RecycleHelper {

    private static final Log _LOG = LogFactory.getLog(RecycleHelper.class);

    private static final RecycleHelper __instance = new RecycleHelper();

    private Set<IDestroyable> __destroyableSet = new ConcurrentHashSet<IDestroyable>();

    /**
     * @return 获取全局实例对象
     */
    public static RecycleHelper getInstance() {
        return __instance;
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
            __destroyableSet.add(destroyable);
        }
        return this;
    }

    /**
     * @return 返回待回收资源数量
     */
    public int size() {
        return __destroyableSet.size();
    }

    private void __recycle() {
        Iterator<IDestroyable> iterator = __destroyableSet.iterator();
        while (iterator.hasNext()) {
            IDestroyable _destroyable = iterator.next();
            iterator.remove();
            try {
                _destroyable.destroy();
            } catch (Throwable e) {
                if (_LOG.isWarnEnabled()) {
                    _LOG.warn("An exception occurs when the object is destroyed: ", RuntimeUtils.unwrapThrow(e));
                } else {
                    RuntimeUtils.unwrapThrow(e).printStackTrace();
                }
            }
        }
    }

    /**
     * 执行资源回收
     */
    public void recycle() {
        recycle(false);
    }

    /**
     * 执行资源回收
     *
     * @param async 是否异步
     */
    public void recycle(boolean async) {
        if (async) {
            ExecutorService _executor = ThreadUtils.newSingleThreadExecutor();
            _executor.submit(new Runnable() {
                @Override
                public void run() {
                    __recycle();
                }
            });
            _executor.shutdown();
        } else {
            __recycle();
        }
    }
}
