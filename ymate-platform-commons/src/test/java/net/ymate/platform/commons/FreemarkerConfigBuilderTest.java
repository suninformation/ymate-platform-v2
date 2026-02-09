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
package net.ymate.platform.commons;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * FreemarkerConfigBuilder测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-07 13:13:33
 * @since 2.1.4
 */
public class FreemarkerConfigBuilderTest {

    @Test
    public void testDefaultValues() {
        FreemarkerConfigBuilder builder = FreemarkerConfigBuilder.create();

        // Test default version
        Assert.assertEquals(Configuration.VERSION_2_3_29, builder.getVersion());

        // Test default encoding
        Assert.assertEquals("UTF-8", builder.getEncoding());

        // Test default output encoding (should be null initially)
        Assert.assertNull(builder.getOutputEncoding());

        // Test default template exception handler (should be null initially)
        Assert.assertNull(builder.getTemplateExceptionHandler());
    }

    @Test
    public void testSetterMethods() {
        FreemarkerConfigBuilder builder = FreemarkerConfigBuilder.create();

        // Test setVersion
        Version customVersion = Configuration.VERSION_2_3_30;
        FreemarkerConfigBuilder result = builder.setVersion(customVersion);
        Assert.assertSame(builder, result); // Should return this
        Assert.assertEquals(customVersion, builder.getVersion());

        // Test setEncoding
        result = builder.setEncoding("GBK");
        Assert.assertSame(builder, result);
        Assert.assertEquals("GBK", builder.getEncoding());

        // Test setOutputEncoding
        result = builder.setOutputEncoding("ISO-8859-1");
        Assert.assertSame(builder, result);
        Assert.assertEquals("ISO-8859-1", builder.getOutputEncoding());

        // Test setTemplateExceptionHandler
        TemplateExceptionHandler customHandler = TemplateExceptionHandler.RETHROW_HANDLER;
        result = builder.setTemplateExceptionHandler(customHandler);
        Assert.assertSame(builder, result);
        Assert.assertEquals(customHandler, builder.getTemplateExceptionHandler());
    }

    @Test
    public void testAddTemplateLoader() {
        FreemarkerConfigBuilder builder = FreemarkerConfigBuilder.create();

        // Create a custom template loader
        TemplateLoader customLoader = new StringTemplateLoader();

        // Test addTemplateLoader with single loader
        FreemarkerConfigBuilder result = builder.addTemplateLoader(customLoader);
        Assert.assertSame(builder, result);

        // Test addTemplateLoader with multiple loaders
        TemplateLoader anotherLoader = new StringTemplateLoader();
        result = builder.addTemplateLoader(customLoader, anotherLoader);
        Assert.assertSame(builder, result);

        // Test addTemplateLoader with null
        result = builder.addTemplateLoader((TemplateLoader) null);
        Assert.assertSame(builder, result);
    }

    @Test
    public void testAddTemplateFileDir() {
        FreemarkerConfigBuilder builder = FreemarkerConfigBuilder.create();

        // Create a temporary directory
        File tempDir = new File(System.getProperty("java.io.tmpdir"));

        // Test addTemplateFileDir with single directory
        FreemarkerConfigBuilder result = builder.addTemplateFileDir(tempDir);
        Assert.assertSame(builder, result);

        // Test addTemplateFileDir with multiple directories
        result = builder.addTemplateFileDir(tempDir, tempDir);
        Assert.assertSame(builder, result);

        // Test addTemplateFileDir with null
        result = builder.addTemplateFileDir((File) null);
        Assert.assertSame(builder, result);
    }

    @Test
    public void testAddTemplateSource() {
        FreemarkerConfigBuilder builder = FreemarkerConfigBuilder.create();

        // Test addTemplateSource with valid values
        FreemarkerConfigBuilder result = builder.addTemplateSource("test", "Hello ${name}!");
        Assert.assertSame(builder, result);

        // Test addTemplateSource with blank name (should be ignored)
        result = builder.addTemplateSource("", "Hello");
        Assert.assertSame(builder, result);

        // Test addTemplateSource with blank template (should be ignored)
        result = builder.addTemplateSource("test2", "");
        Assert.assertSame(builder, result);

        // Test addTemplateSource with null values (should be ignored)
        result = builder.addTemplateSource(null, "Hello");
        Assert.assertSame(builder, result);

        result = builder.addTemplateSource("test3", null);
        Assert.assertSame(builder, result);
    }

    @Test
    public void testAddTemplateClass() {
        FreemarkerConfigBuilder builder = FreemarkerConfigBuilder.create();

        // Test addTemplateClass with valid values
        FreemarkerConfigBuilder result = builder.addTemplateClass(getClass(), "/templates");
        Assert.assertSame(builder, result);

        // Test addTemplateClass with null class (should be ignored)
        result = builder.addTemplateClass(null, "/templates");
        Assert.assertSame(builder, result);

        // Test addTemplateClass with blank path (should be ignored)
        result = builder.addTemplateClass(getClass(), "");
        Assert.assertSame(builder, result);
    }

    @Test
    public void testBuild() throws IOException {
        FreemarkerConfigBuilder builder = FreemarkerConfigBuilder.create();

        // Add some template sources
        builder.addTemplateSource("test_template", "Hello ${name}!");

        // Build configuration
        Configuration config = builder.build();
        Assert.assertNotNull(config);

        // Verify configuration settings
        Assert.assertEquals("UTF-8", config.getDefaultEncoding());
        Assert.assertEquals("UTF-8", config.getOutputEncoding());
        Assert.assertEquals(TemplateExceptionHandler.HTML_DEBUG_HANDLER, config.getTemplateExceptionHandler());
    }

    @Test
    public void testBuildWithCustomSettings() throws IOException {
        FreemarkerConfigBuilder builder = FreemarkerConfigBuilder.create()
                .setEncoding("GBK")
                .setOutputEncoding("ISO-8859-1")
                .setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Build configuration
        Configuration config = builder.build();
        Assert.assertNotNull(config);

        // Verify custom settings
        Assert.assertEquals("GBK", config.getDefaultEncoding());
        Assert.assertEquals("ISO-8859-1", config.getOutputEncoding());
        Assert.assertEquals(TemplateExceptionHandler.RETHROW_HANDLER, config.getTemplateExceptionHandler());
    }

    @Test
    public void testBuildWithMultipleTemplateLoaders() throws IOException {
        FreemarkerConfigBuilder builder = FreemarkerConfigBuilder.create();

        // Add multiple template sources
        builder.addTemplateSource("template1", "Template 1")
                .addTemplateSource("template2", "Template 2");

        // Add a class template loader
        builder.addTemplateClass(getClass(), "/");

        // Build configuration
        Configuration config = builder.build();
        Assert.assertNotNull(config);

        // Verify template loader is set (should be MultiTemplateLoader)
        Assert.assertNotNull(config.getTemplateLoader());
    }

    @Test
    public void testBuildWithSingleTemplateLoader() throws IOException {
        FreemarkerConfigBuilder builder = FreemarkerConfigBuilder.create();

        // Add only one template source
        builder.addTemplateSource("single_template", "Single template");

        // Build configuration
        Configuration config = builder.build();
        Assert.assertNotNull(config);

        // Verify template loader is set
        Assert.assertNotNull(config.getTemplateLoader());
    }

    @Test
    public void testBuildWithNoTemplateLoaders() throws IOException {
        FreemarkerConfigBuilder builder = FreemarkerConfigBuilder.create();

        // Build configuration without adding any template loaders
        Configuration config = builder.build();
        Assert.assertNotNull(config);

        // Template loader should be null in this case
        Assert.assertNull(config.getTemplateLoader());
    }
}
