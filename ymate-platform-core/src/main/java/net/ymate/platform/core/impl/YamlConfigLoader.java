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
package net.ymate.platform.core.impl;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.configuration.impl.MapSafeConfigReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * YAML 配置文件加载与扁平化处理器
 * <p>
 * 负责检测运行时环境中是否存在 SnakeYAML 类库，并将 YAML 层级结构解析为扁平化的键值对集合（点号分隔键名），
 * 以适配框架内部基于 Map&lt;String, String&gt; 的配置读取体系，具体扁平化规则如下：
 * <ul>
 *     <li>嵌套 Map：递归展开，键名以点号分隔拼接，如 key1.key2.key3</li>
 *     <li>集合或数组：以竖线（|）连接各元素，与 MapSafeConfigReader 数组解析约定一致</li>
 *     <li>null 值及空集合：跳过，不生成对应键值对</li>
 *     <li>其它标量值：直接转换为字符串</li>
 * </ul>
 * <p>
 * 使用示例（YAML 内容与其扁平化结果）：
 * <pre>
 * ymp:
 *   dev_mode: true
 *   excluded_packages:
 *     - com.test
 *     - com.demo
 *   configs:
 *     jdbc:
 *       connection_url: jdbc:mysql://localhost/db
 * </pre>
 * <pre>
 * ymp.dev_mode = true
 * ymp.excluded_packages = com.test|com.demo
 * ymp.configs.jdbc.connection_url = jdbc:mysql://localhost/db
 * </pre>
 * <p>
 * 同时支持反向转换：将扁平化的键值对集合（如 Properties 加载结果）还原为 YAML 层级结构并输出为格式化文本。
 *
 * @author 刘镇 (suninformation@163.com) on 2026-08-28 09:45
 * @since 2.1.4
 */
public final class YamlConfigLoader {

    private static final Log LOG = LogFactory.getLog(YamlConfigLoader.class);

    private static final String KEY_SEPARATOR = ".";

    private static final String ARRAY_SEPARATOR = "|";

    /**
     * SnakeYAML 类库可用性检测结果缓存
     */
    private static final boolean YAML_AVAILABLE;

    static {
        YAML_AVAILABLE = ClassUtils.loadClassOrNull("org.yaml.snakeyaml.Yaml", YamlConfigLoader.class) != null;
    }

    /**
     * 检测运行时 classpath 中是否存在 SnakeYAML 类库
     *
     * @return 若 SnakeYAML 类库存在则返回 true
     * @since 2.1.4
     */
    public static boolean isYamlAvailable() {
        return YAML_AVAILABLE;
    }

    /**
     * 加载 YAML 输入流并扁平化为键值对集合，仅解析第一个文档（多文档中的后续文档将被忽略）
     *
     * @param inputStream YAML 输入流
     * @return 返回扁平化后的键值对集合，若 SnakeYAML 类库不可用或 YAML 内容为空则返回空集合
     * @throws IOException 可能产生的任何异常
     * @since 2.1.4
     */
    public static Map<String, String> loadAndFlatten(InputStream inputStream) throws IOException {
        if (!YAML_AVAILABLE) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("SnakeYAML library was not found in classpath, the YAML configuration file parsing has been skipped.");
            }
            return Collections.emptyMap();
        }
        Map<String, Object> sourceMap = SnakeYamlProcessor.load(inputStream);
        return sourceMap == null ? Collections.emptyMap() : flatten(sourceMap);
    }

    /**
     * 将 YAML 层级 Map 递归扁平化为点号分隔键名的键值对集合，元素顺序与原始 Map 保持一致
     *
     * @param source YAML 层级 Map 对象
     * @return 返回扁平化后的键值对集合，若源 Map 为空则返回空集合
     * @since 2.1.4
     */
    public static Map<String, String> flatten(Map<String, Object> source) {
        Map<String, String> returnValue = new LinkedHashMap<>();
        if (source != null && !source.isEmpty()) {
            doFlatten(StringUtils.EMPTY, source, returnValue);
        }
        return returnValue;
    }

    /**
     * 递归扁平化 YAML 层级 Map 中的每一项数据
     *
     * @param prefix 当前层级的前缀键名，顶级层级为空字符串
     * @param source 当前层级的 Map 对象
     * @param result 扁平化结果的键值对集合
     * @since 2.1.4
     */
    private static void doFlatten(String prefix, Map<String, Object> source, Map<String, String> result) {
        source.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            String fullKey = prefix.isEmpty() ? String.valueOf(key) : prefix + KEY_SEPARATOR + key;
            if (value instanceof Map) {
                doFlatten(fullKey, castToSourceMap(value), result);
            } else if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                if (!collection.isEmpty()) {
                    result.put(fullKey, StringUtils.join(collection, ARRAY_SEPARATOR));
                }
            } else if (value instanceof Object[]) {
                Object[] arrays = (Object[]) value;
                if (arrays.length > 0) {
                    result.put(fullKey, StringUtils.join(arrays, ARRAY_SEPARATOR));
                }
            } else {
                result.put(fullKey, String.valueOf(value));
            }
        });
    }

    /**
     * 将扁平化的键值对集合（点号分隔键名，如 Properties 加载结果）转换为 YAML 格式文本
     * <p>
     * 转换规则与 {@link #flatten(Map)} 互逆：点号分隔的键名还原为层级嵌套结构，以竖线（|）连接的值还原为列表，
     * 各层级键名按字典顺序排列；注意扁平化结构无法区分键名中本身包含点号的场景。
     *
     * @param source 扁平化的键值对集合，可为 Properties 对象
     * @return 返回 YAML 格式文本，若 SnakeYAML 类库不可用则返回 null，若源集合为空则返回空字符串
     * @since 2.1.4
     */
    public static String toYamlText(Map<?, ?> source) {
        if (!YAML_AVAILABLE) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("SnakeYAML library was not found in classpath, the YAML text conversion has been skipped.");
            }
            return null;
        }
        if (source == null || source.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return SnakeYamlProcessor.dump(unflatten(source));
    }

    /**
     * 将 Properties 对象转换为 YAML 格式文本
     *
     * @param properties Properties 对象
     * @return 返回 YAML 格式文本，若 SnakeYAML 类库不可用则返回 null，若对象为空则返回空字符串
     * @since 2.1.4
     */
    public static String toYamlText(Properties properties) {
        return toYamlText((Map<?, ?>) properties);
    }

    /**
     * 将扁平化的键值对集合还原为 YAML 层级嵌套 Map 结构，各层级键名按字典顺序排列
     *
     * @param source 扁平化的键值对集合
     * @return 返回层级嵌套的 Map 对象
     * @since 2.1.4
     */
    private static Map<String, Object> unflatten(Map<?, ?> source) {
        Map<String, Object> returnValue = new TreeMap<>();
        source.forEach((key, value) -> {
            if (key == null || value == null) {
                return;
            }
            String[] keyParts = String.valueOf(key).split("\\" + KEY_SEPARATOR);
            Map<String, Object> current = returnValue;
            for (int i = 0; i < keyParts.length - 1; i++) {
                Object child = current.get(keyParts[i]);
                if (!(child instanceof Map)) {
                    child = new TreeMap<String, Object>();
                    current.put(keyParts[i], child);
                }
                current = castToSourceMap(child);
            }
            current.put(keyParts[keyParts.length - 1], restoreValue(String.valueOf(value)));
        });
        return returnValue;
    }

    /**
     * 还原键值数据：以竖线（|）连接的值转换为列表，与 {@link MapSafeConfigReader} 数组解析约定一致，其它值保持字符串
     *
     * @param value 原始字符串值
     * @return 返回还原后的列表或字符串对象
     * @since 2.1.4
     */
    private static Object restoreValue(String value) {
        if (value.contains(ARRAY_SEPARATOR)) {
            return Arrays.asList(StringUtils.split(value, ARRAY_SEPARATOR));
        }
        return value;
    }

    /**
     * 将 Map 类型的 YAML 节点值转换为目标泛型 Map 对象
     *
     * @param value YAML 节点值对象
     * @return 返回转换后的 Map 对象
     * @since 2.1.4
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> castToSourceMap(Object value) {
        return (Map<String, Object>) value;
    }

    /**
     * SnakeYAML 调用隔离处理器，仅当 YAML_AVAILABLE 为 true 时才会被类加载器加载
     *
     * @since 2.1.4
     */
    private static final class SnakeYamlProcessor {

        /**
         * 使用 SnakeYAML 解析输入流并返回层级 Map 对象
         *
         * @param inputStream YAML 输入流
         * @return 返回解析后的层级 Map 对象，若 YAML 内容为空则返回 null
         * @since 2.1.4
         */
        static Map<String, Object> load(InputStream inputStream) {
            return new Yaml().load(inputStream);
        }

        /**
         * 使用 SnakeYAML 将层级 Map 对象序列化为 YAML 格式文本，采用块级风格输出且不折行
         *
         * @param sourceMap 层级 Map 对象
         * @return 返回 YAML 格式文本
         * @since 2.1.4
         */
        static String dump(Map<String, Object> sourceMap) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setWidth(Integer.MAX_VALUE);
            return new Yaml(options).dump(sourceMap);
        }
    }
}
