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

/**
 * 速度统计监听器接口
 *
 * @author 刘镇 (suninformation@163.com) on 16/12/27 下午4:14
 * @version 1.0
 */
public interface ISpeedListener {

    /**
     * 处理监听数据
     *
     * @param speed        速度
     * @param averageSpeed 平均速度
     * @param maxSpeed     最大速度
     * @param minSpeed     最小速度
     */
    void listen(long speed, long averageSpeed, long maxSpeed, long minSpeed);
}
