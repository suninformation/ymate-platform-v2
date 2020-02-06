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
package net.ymate.platform.commons;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

import java.io.Writer;

/**
 * @author 刘镇 (suninformation@163.com) on 2014年3月15日 上午10:38:26
 */
public class XStreamHelper {

    public interface INodeFilter {

        /**
         * 执行节点过滤
         *
         * @param name 节点名称
         * @return 返回true则添加CDATA标签
         */
        boolean doFilter(String name);
    }

    public static class WeChatDomDriver extends DomDriver {

        private INodeFilter nodeFilter;

        public WeChatDomDriver(String encoding, NameCoder nameCoder) {
            super(encoding, nameCoder);
        }

        public WeChatDomDriver(String encoding, NameCoder nameCoder, INodeFilter nodeFilter) {
            super(encoding, nameCoder);
            this.nodeFilter = nodeFilter;
        }

        @Override
        public HierarchicalStreamWriter createWriter(Writer out) {
            return new PrettyPrintWriter(out, this.getNameCoder()) {

                private String name;

                private boolean cdata = false;

                @Override
                public void startNode(String name, Class clazz) {
                    super.startNode(name, clazz);
                    this.name = name;
                    this.cdata = String.class.equals(clazz);
                }

                @Override
                protected void writeText(QuickWriter writer, String text) {
                    if (cdata && (nodeFilter == null || nodeFilter.doFilter(name))) {
                        writer.write("<![CDATA[");
                        writer.write(text);
                        writer.write("]]>");
                    } else {
                        writer.write(text);
                    }
                }
            };
        }
    }

    /**
     * 初始化XStream可支持String类型字段加入CDATA标签"&lt;![CDATA["和结尾处加上"]]&gt;"， 以供XStream输出时进行识别
     *
     * @param isAddCDATA 是否支持CDATA标签
     * @param nodeFilter CDATA节点过滤
     * @return 返回构建后的XStream实例对象
     */
    public static XStream createXStream(boolean isAddCDATA, INodeFilter nodeFilter) {
        XStream xstream;
        if (isAddCDATA) {
            xstream = new XStream(new WeChatDomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_"), nodeFilter));
        } else {
            xstream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
        }
        return xstream;
    }

    public static XStream createXStream(boolean isAddCDATA) {
        return createXStream(isAddCDATA, null);
    }

}
