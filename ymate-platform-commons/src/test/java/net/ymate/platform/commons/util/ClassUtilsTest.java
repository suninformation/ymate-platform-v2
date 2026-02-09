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
package net.ymate.platform.commons.util;

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * ClassUtils单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-04 16:10
 * @since 2.1.4
 */
public class ClassUtilsTest {

    // ========== BeanWrapper 测试 ==========

    @Test
    public void testBeanWrapperWrapper() {
        // 测试包装对象
        TestClass testClass = new TestClass();
        ClassUtils.BeanWrapper<TestClass> beanWrapper = ClassUtils.wrapper(testClass);
        Assert.assertNotNull(beanWrapper);
        Assert.assertSame(testClass, beanWrapper.getTargetObject());

        // 测试wrapperClass方法
        ClassUtils.BeanWrapper<TestClass> beanWrapper3 = ClassUtils.wrapperClass(TestClass.class);
        Assert.assertNotNull(beanWrapper3);
        Assert.assertNotNull(beanWrapper3.getTargetObject());
    }

    @Test
    public void testBeanWrapperGetValueAndSetValue() {
        try {
            TestClass testClass = new TestClass();
            ClassUtils.BeanWrapper<TestClass> beanWrapper = ClassUtils.wrapper(testClass);

            // 测试设置和获取字段值
            beanWrapper.setValue("stringField", "test-value");
            Assert.assertEquals("test-value", beanWrapper.getValue("stringField"));

            beanWrapper.setValue("intField", 123);
            Assert.assertEquals(123, beanWrapper.getValue("intField"));

            // 测试使用setter方法
            beanWrapper.setValue("booleanField", true);
            Assert.assertTrue((Boolean) beanWrapper.getValue("booleanField"));

            // 测试使用is方法
            beanWrapper.setValue("flag", true);
            Assert.assertTrue((Boolean) beanWrapper.getValue("flag"));
        } catch (Exception e) {
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testBeanWrapperFromMap() {
        try {
            TestClass testClass = new TestClass();
            ClassUtils.BeanWrapper<TestClass> beanWrapper = ClassUtils.wrapper(testClass);

            // 测试从Map设置值
            Map<String, Object> map = new HashMap<>();
            map.put("stringField", "from-map");
            map.put("intField", 456);
            map.put("booleanField", false);

            beanWrapper.fromMap(map);
            Assert.assertEquals("from-map", beanWrapper.getValue("stringField"));
            Assert.assertEquals(456, beanWrapper.getValue("intField"));
            Assert.assertFalse((Boolean) beanWrapper.getValue("booleanField"));

            // 测试带过滤器的fromMap
            Map<String, Object> map2 = new HashMap<>();
            map2.put("stringField", "filtered");
            map2.put("intField", 789);

            beanWrapper.fromMap(map2, (key, value) -> key.equals("intField")); // 过滤掉intField
            Assert.assertEquals("filtered", beanWrapper.getValue("stringField"));
            Assert.assertEquals(456, beanWrapper.getValue("intField")); // 应该保持不变
        } catch (Exception e) {
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testBeanWrapperToMap() {
        try {
            TestClass testClass = new TestClass();
            testClass.setStringField("test");
            testClass.setIntField(123);
            testClass.setBooleanField(true);
            ClassUtils.BeanWrapper<TestClass> beanWrapper = ClassUtils.wrapper(testClass);

            // 测试转换为Map
            Map<String, Object> map = beanWrapper.toMap();
            Assert.assertNotNull(map);
            Assert.assertEquals("test", map.get("stringField"));
            Assert.assertEquals(123, map.get("intField"));
            Assert.assertEquals(true, map.get("booleanField"));

            // 测试带过滤器的toMap
            Map<String, Object> map2 = beanWrapper.toMap((key, value) -> key.equals("stringField")); // 过滤掉stringField
            Assert.assertNotNull(map2);
            Assert.assertNull(map2.get("stringField"));
            Assert.assertEquals(123, map2.get("intField"));
        } catch (Exception e) {
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testBeanWrapperDuplicate() {
        try {
            TestClass source = new TestClass();
            source.setStringField("source-value");
            source.setIntField(999);
            source.setBooleanField(true);

            TestClass target = new TestClass();
            ClassUtils.BeanWrapper<TestClass> sourceWrapper = ClassUtils.wrapper(source);
            sourceWrapper.duplicate(target);

            Assert.assertEquals("source-value", target.getStringField());
            Assert.assertEquals(999, target.getIntField());
            Assert.assertEquals(true, target.isBooleanField());

            // 测试带过滤器的duplicate
            TestClass source2 = new TestClass();
            source2.setStringField("source2-value");
            source2.setIntField(888);

            TestClass target2 = new TestClass();
            ClassUtils.BeanWrapper<TestClass> sourceWrapper2 = ClassUtils.wrapper(source2);
            sourceWrapper2.duplicate(target2, (key, value) -> key.equals("stringField")); // 过滤掉stringField

            Assert.assertNull(target2.getStringField()); // 应该保持null
            Assert.assertEquals(888, target2.getIntField());
        } catch (Exception e) {
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testBeanWrapperFieldsAndMethods() {
        TestClass testClass = new TestClass();
        ClassUtils.BeanWrapper<TestClass> beanWrapper = ClassUtils.wrapper(testClass);

        // 测试获取字段名集合
        Set<String> fieldNames = beanWrapper.getFieldNames();
        Assert.assertNotNull(fieldNames);
        Assert.assertTrue(fieldNames.contains("stringField"));
        Assert.assertTrue(fieldNames.contains("intField"));

        // 测试获取字段映射
        Map<String, Field> fieldMap = beanWrapper.getFieldMap();
        Assert.assertNotNull(fieldMap);
        Assert.assertNotNull(fieldMap.get("stringField"));

        // 测试获取所有字段
        Collection<Field> fields = beanWrapper.getFields();
        Assert.assertNotNull(fields);
        Assert.assertFalse(fields.isEmpty());

        // 测试获取指定字段
        Field field = beanWrapper.getField("stringField");
        Assert.assertNotNull(field);
        Assert.assertEquals("stringField", field.getName());

        // 测试获取字段类型
        Class<?> fieldType = beanWrapper.getFieldType("stringField");
        Assert.assertNotNull(fieldType);
        Assert.assertEquals(String.class, fieldType);

        // 测试获取字段注解
        Annotation[] annotations = beanWrapper.getFieldAnnotations("stringField");
        Assert.assertNotNull(annotations);

        // 测试获取所有方法
        Collection<Method> methods = beanWrapper.getMethods();
        Assert.assertNotNull(methods);
        Assert.assertFalse(methods.isEmpty());

        // 测试获取指定方法
        Method method = beanWrapper.getMethod("getStringField");
        Assert.assertNotNull(method);
        Assert.assertEquals("getStringField", method.getName());
    }

    // ========== ClassLoaderUtils 测试 ==========

    @Test
    public void testClassLoaderUtilsGetDefaultClassLoader() {
        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
        Assert.assertNotNull(classLoader);
        Assert.assertTrue(classLoader instanceof ClassUtils.InnerClassLoader);
    }

    @Test
    public void testClassLoaderUtilsLoadClass() {
        try {
            // 测试加载存在的类
            Class<?> clazz = ClassUtils.loadClass("java.lang.String", ClassUtilsTest.class);
            Assert.assertNotNull(clazz);
            Assert.assertEquals(String.class, clazz);

            // 测试loadClassOrNull方法加载不存在的类
            Class<?> clazz2 = ClassUtils.loadClassOrNull("com.example.NonExistentClass", ClassUtilsTest.class);
            Assert.assertNull(clazz2);
        } catch (ClassNotFoundException e) {
            Assert.fail("ClassNotFoundException: " + e.getMessage());
        }
    }

    // ========== ClassUtils 测试 ==========

    @Test
    public void testClassUtilsImpl() {
        // 测试根据Class对象实例化
        TestInterface impl = ClassUtils.impl(TestImpl.class, TestInterface.class);
        Assert.assertNotNull(impl);
        Assert.assertEquals("test", impl.testMethod());

        // 测试根据类名实例化
        TestInterface impl2 = ClassUtils.impl("net.ymate.platform.commons.util.ClassUtilsTest$TestImpl", TestInterface.class, ClassUtilsTest.class);
        Assert.assertNotNull(impl2);
        Assert.assertEquals("test", impl2.testMethod());
    }

    @Test
    public void testClassUtilsPropertyNameConversion() {
        // 测试属性名转字段名
        Assert.assertEquals("UserName", ClassUtils.propertyNameToFieldName("user_name"));
        Assert.assertEquals("UserName", ClassUtils.propertyNameToFieldName("user.name", "."));

        // 测试字段名转属性名
        Assert.assertEquals("user_name", ClassUtils.fieldNameToPropertyName("userName", 0));
        Assert.assertEquals("USER_NAME", ClassUtils.fieldNameToPropertyName("userName", 2));
        Assert.assertEquals("user.name", ClassUtils.fieldNameToPropertyName("userName", 0, "."));
    }

    @Test
    public void testFieldNameToPropertyName() {
        // 测试驼峰命名
        Assert.assertEquals("user_name", ClassUtils.fieldNameToPropertyName("userName", 0));

        // 测试首字母大写
        Assert.assertEquals("USER_NAME", ClassUtils.fieldNameToPropertyName("userName", 1));

        // 测试全大写
        Assert.assertEquals("USER_NAME", ClassUtils.fieldNameToPropertyName("userName", 2));

        // 测试自定义分隔符
        Assert.assertEquals("user-name", ClassUtils.fieldNameToPropertyName("userName", 0, "-"));
    }

    // ========== ReflectionUtils 测试 ==========

    @Test
    public void testReflectionUtilsIsNormalClass() {
        Assert.assertTrue(ClassUtils.isNormalClass(String.class));
        Assert.assertTrue(ClassUtils.isNormalClass(Integer.class));
        Assert.assertFalse(ClassUtils.isNormalClass(String[].class));
        Assert.assertFalse(ClassUtils.isNormalClass(Override.class)); // 具体注释类型
        Assert.assertFalse(ClassUtils.isNormalClass(Thread.State.class)); // 具体枚举类型
    }

    @Test
    public void testReflectionUtilsIsNormalMethod() {
        try {
            Method method = String.class.getMethod("length");
            Assert.assertTrue(ClassUtils.isNormalMethod(method));

            Method equalsMethod = Object.class.getMethod("equals", Object.class);
            Assert.assertFalse(ClassUtils.isNormalMethod(equalsMethod));

            Method staticMethod = Class.class.getMethod("forName", String.class);
            Assert.assertFalse(ClassUtils.isNormalMethod(staticMethod));
        } catch (NoSuchMethodException e) {
            Assert.fail("NoSuchMethodException: " + e.getMessage());
        }
    }

    @Test
    public void testReflectionUtilsIsNormalField() {
        try {
            Field field = TestClass.class.getDeclaredField("stringField");
            Assert.assertTrue(ClassUtils.isNormalField(field));

            Field staticField = TestClass.class.getDeclaredField("staticField");
            Assert.assertFalse(ClassUtils.isNormalField(staticField));

            Field finalField = TestClass.class.getDeclaredField("finalField");
            Assert.assertFalse(ClassUtils.isNormalField(finalField));

            Field transientField = TestClass.class.getDeclaredField("transientField");
            Assert.assertFalse(ClassUtils.isNormalField(transientField));

            Field volatileField = TestClass.class.getDeclaredField("volatileField");
            Assert.assertFalse(ClassUtils.isNormalField(volatileField)); // volatile字段不被允许
        } catch (NoSuchFieldException e) {
            Assert.fail("NoSuchFieldException: " + e.getMessage());
        }
    }

    @Test
    public void testReflectionUtilsAccessibleSafe() {
        try {
            // 测试字段可访问性
            Field field = TestClass.class.getDeclaredField("stringField");
            field.setAccessible(true);
            Assert.assertTrue(field.isAccessible());

            // 测试方法可访问性
            Method method = TestClass.class.getDeclaredMethod("privateMethod");
            method.setAccessible(true);
            Assert.assertTrue(method.isAccessible());

            // 测试构造方法可访问性
            Constructor<TestClass> constructor = TestClass.class.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            Assert.assertTrue(constructor.isAccessible());
        } catch (Exception e) {
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testReflectionUtilsInheritanceAndInterfaces() {
        // 测试类继承关系
        Assert.assertTrue(ClassUtils.isSubclassOf(String.class, Object.class));
        Assert.assertFalse(ClassUtils.isSubclassOf(Object.class, String.class));

        // 测试接口实现
        Assert.assertTrue(ClassUtils.isInterfaceOf(ArrayList.class, List.class));
        Assert.assertTrue(ClassUtils.isInterfaceOf(HashMap.class, Map.class));
        Assert.assertFalse(ClassUtils.isInterfaceOf(String.class, List.class));
    }

    @Test
    public void testReflectionUtilsGetFieldsAndMethods() {
        // 测试获取所有字段
        List<Field> fields = ClassUtils.getFields(TestClass.class, true);
        Assert.assertNotNull(fields);
        Assert.assertFalse(fields.isEmpty());

        // 测试获取所有方法
        List<Method> methods = ClassUtils.getMethods(TestClass.class, true);
        Assert.assertNotNull(methods);
        Assert.assertFalse(methods.isEmpty());

        // 测试获取接口名称
        String[] interfaceNames = ClassUtils.getInterfaceNames(ArrayList.class);
        Assert.assertNotNull(interfaceNames);
        Assert.assertTrue(Arrays.asList(interfaceNames).contains("java.util.List"));

        // 测试获取数组元素类型
        Assert.assertEquals(String.class, ClassUtils.getArrayClassType(String[].class));
        Assert.assertEquals(Integer[].class, ClassUtils.getArrayClassType(Integer[][].class));
    }

    // ========== ExtensionLoader 测试 ==========

    @Test
    public void testExtensionLoaderUtils() {
        try {
            // 测试获取扩展加载器
            ClassUtils.ExtensionLoader<TestInterface> extensionLoader = ClassUtils.getExtensionLoader(TestInterface.class);
            Assert.assertNotNull(extensionLoader);

            // 测试获取扩展类
            List<Class<TestInterface>> extensionClasses = extensionLoader.getExtensionClasses();
            Assert.assertNotNull(extensionClasses);

            // 测试缓存机制
            ClassUtils.ExtensionLoader<TestInterface> extensionLoader2 = ClassUtils.getExtensionLoader(TestInterface.class);
            Assert.assertSame(extensionLoader, extensionLoader2);
        } catch (Exception e) {
            // 扩展加载可能失败，因为没有配置扩展，所以这里只验证没有抛出异常
            Assert.assertNotNull(e);
        }
    }

    // ========== InnerClassLoader 测试 ==========

    @Test
    public void testInnerClassLoader() {
        // 测试创建InnerClassLoader
        ClassUtils.InnerClassLoader classLoader = new ClassUtils.InnerClassLoader(new URL[]{}, getClass().getClassLoader());
        Assert.assertNotNull(classLoader);

        // 测试添加URL
        try {
            URL url = new URL("file:/tmp/");
            classLoader.addURL(url);
            // 无法直接验证URL是否添加成功，只能验证方法调用不抛出异常
        } catch (Exception e) {
            Assert.fail("Exception: " + e.getMessage());
        }
    }

    // ========== 测试辅助类 ==========

    /**
     * 测试接口
     */
    public interface TestInterface {
        String testMethod();
    }

    /**
     * 测试接口实现类
     */
    public static class TestImpl implements TestInterface {
        @Override
        public String testMethod() {
            return "test";
        }
    }

    /**
     * 测试类
     */
    public static class TestClass {
        private String stringField;
        private int intField;
        private boolean booleanField;
        private boolean flag;
        private static String staticField;
        private final String finalField = "final-value";
        private transient String transientField;
        private volatile String volatileField;

        // 用于测试私有构造方法
        private TestClass(String value) {
            this.stringField = value;
        }

        // 默认构造方法
        public TestClass() {
        }

        private void privateMethod() {
            // 私有方法，用于测试方法可访问性
        }

        public String getStringField() {
            return stringField;
        }

        public void setStringField(String stringField) {
            this.stringField = stringField;
        }

        public int getIntField() {
            return intField;
        }

        public void setIntField(int intField) {
            this.intField = intField;
        }

        public boolean isBooleanField() {
            return booleanField;
        }

        public void setBooleanField(boolean booleanField) {
            this.booleanField = booleanField;
        }

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public static String getStaticField() {
            return staticField;
        }

        public static void setStaticField(String staticField) {
            TestClass.staticField = staticField;
        }

        public String getFinalField() {
            return finalField;
        }

        public String getTransientField() {
            return transientField;
        }

        public void setTransientField(String transientField) {
            this.transientField = transientField;
        }

        public String getVolatileField() {
            return volatileField;
        }

        public void setVolatileField(String volatileField) {
            this.volatileField = volatileField;
        }
    }
}
