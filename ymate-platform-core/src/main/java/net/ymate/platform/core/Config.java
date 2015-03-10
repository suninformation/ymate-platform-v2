/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.core;

import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.RuntimeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.*;

/**
 * YMP框架配置类
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-9 下午2:50
 * @version 1.0
 */
public class Config {

    private static Config __config;

    private Properties __props = new Properties();

    private Boolean __isDevelopMode;

    private List<String> __packageNames;

    /**
     * 私有构造
     */
    private Config() {
        InputStream _in = null;
        try {
            if (RuntimeUtils.isWindows()) {
                _in = Config.class.getClassLoader().getResourceAsStream("ymp-conf_WIN.properties");
            } else if (RuntimeUtils.isUnixOrLinux()) {
                _in = Config.class.getClassLoader().getResourceAsStream("ymp-conf_UNIX.properties");
            }
            if (_in == null) {
                _in = Config.class.getClassLoader().getResourceAsStream("ymp-conf.properties");
            }
            if (_in != null) {
                __props.load(_in);
            }
        } catch (Exception e) {
            throw new RuntimeException(RuntimeUtils.unwrapThrow(e));
        } finally {
            try {
                _in.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * @return 获取配置对象实例
     */
    public static synchronized Config get() {
        if (__config == null) {
            __config = new Config();
        }
        return __config;
    }

    /**
     * @return 返回是否为开发模式
     */
    public boolean isDevelopMode() {
        if (__isDevelopMode == null) {
            __isDevelopMode = new BlurObject(__props.getProperty("ymp.dev_mode")).toBooleanValue();
        }
        return __isDevelopMode;
    }

    /**
     * @return 返回框架自动扫描的包路径集合
     */
    public List<String> getAutoscanPackages() {
        if (__packageNames == null) {
            String[] _packageNameArr = StringUtils.split(__props.getProperty("ymp.autoscan_packages"), "|");
            if (_packageNameArr != null) {
                __packageNames = new ArrayList<String>(Arrays.asList(_packageNameArr));
            } else {
                __packageNames = Collections.emptyList();
            }
        }
        return __packageNames;
    }
}
