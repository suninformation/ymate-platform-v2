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
package net.ymate.platform.commons.util;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.lang.PairObject;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 类操作相关工具
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-5 下午6:41:23
 */
public class ClassUtils {

    private static final Log LOG = LogFactory.getLog(ClassUtils.class);

    private static final String ANONYMOUS_CLASS_FLAG = "$$";

    public static final String PACKAGE_INFO = "package-info";

    public static final String PACKAGE_SEPARATOR = ".";

    private static final InnerClassLoader INNER_CLASS_LOADER = new InnerClassLoader(new URL[]{}, ClassUtils.class.getClassLoader());

    @SuppressWarnings("rawtypes")
    private static final Map<Class<?>, ExtensionLoader> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    public static final Set<String> EXCLUDED_METHOD_NAMES = Collections.unmodifiableSet(Arrays.stream(Object.class.getDeclaredMethods()).map(Method::getName).collect(Collectors.toSet()));

    /**
     * @return 返回默认类加载器对象
     */
    public static ClassLoader getDefaultClassLoader() {
        return INNER_CLASS_LOADER;
    }

    /**
     * 获得指定名称、限定接口的实现类
     *
     * @param <T>            接口类型
     * @param className      实现类名
     * @param interfaceClass 限制接口名
     * @param callingClass   调用者
     * @return 如果可以得到并且限定于指定实现，那么返回实例，否则为空
     */
    public static <T> T impl(String className, Class<T> interfaceClass, Class<?> callingClass) {
        if (StringUtils.isNotBlank(className)) {
            try {
                Class<?> implClass = loadClass(className, callingClass);
                return impl(implClass, interfaceClass);
            } catch (ClassNotFoundException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T impl(Class<?> implClass, Class<T> interfaceClass) {
        if (implClass != null) {
            if (interfaceClass == null || interfaceClass.isAssignableFrom(implClass)) {
                try {
                    return (T) implClass.newInstance();
                } catch (IllegalAccessException | InstantiationException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获得指定名称、限定接口，通过特定参数类型构造的实现类
     *
     * @param <T>               接口类型
     * @param className         实现类名
     * @param interfaceClass    限制接口名
     * @param callingClass      调用者
     * @param parameterTypes    构造方法参数类型集合
     * @param initArgs          构造方法参数值集合
     * @param allowNoSuchMethod 当发生NoSuchMethodException异常时是否输出日志
     * @return 如果可以得到并且限定于指定实现，那么返回实例，否则为空
     */
    public static <T> T impl(String className, Class<T> interfaceClass, Class<?> callingClass, Class<?>[] parameterTypes, Object[] initArgs, boolean allowNoSuchMethod) {
        if (StringUtils.isNotBlank(className)) {
            try {
                Class<?> implClass = loadClass(className, callingClass);
                return impl(implClass, interfaceClass, parameterTypes, initArgs, allowNoSuchMethod);
            } catch (ClassNotFoundException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T impl(Class<?> implClass, Class<T> interfaceClass, Class<?>[] parameterTypes, Object[] initArgs, boolean allowNoSuchMethod) {
        if (implClass != null) {
            if (interfaceClass == null || interfaceClass.isAssignableFrom(implClass)) {
                try {
                    if (parameterTypes != null && parameterTypes.length > 0) {
                        return (T) implClass.getConstructor(parameterTypes).newInstance(initArgs);
                    }
                    return (T) implClass.newInstance();
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                    boolean flag = true;
                    if (e instanceof NoSuchMethodException) {
                        flag = allowNoSuchMethod;
                    }
                    if (flag && LOG.isDebugEnabled()) {
                        LOG.debug(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
        }
        return null;
    }

    public static Class<?> loadClass(String className, Class<?> callingClass) throws ClassNotFoundException {
        Class<?> targetClass = null;
        if (StringUtils.isNotBlank(className)) {
            try {
                try {
                    targetClass = Thread.currentThread().getContextClassLoader().loadClass(className);
                } catch (ClassNotFoundException e) {
                    try {
                        targetClass = Class.forName(className, false, ClassUtils.class.getClassLoader());
                    } catch (ClassNotFoundException ex) {
                        try {
                            targetClass = INNER_CLASS_LOADER.loadClass(className);
                        } catch (ClassNotFoundException exc) {
                            targetClass = callingClass.getClassLoader().loadClass(className);
                        }
                    }
                }
            } catch (NoClassDefFoundError ignored) {
            }
        }
        return targetClass;
    }

    public static Class<?> loadClassOrNull(String className, Class<?> callingClass) {
        try {
            return loadClass(className, callingClass);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static <T> T loadClass(Class<T> clazz) {
        return loadClass(clazz, null);
    }

    public static <T> T loadClass(Class<T> clazz, Class<? extends T> defaultClass) {
        T instance = null;
        try {
            instance = getExtensionLoader(clazz).getExtension();
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        if (instance == null && defaultClass != null) {
            try {
                instance = defaultClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return instance;
    }

    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> clazz) throws Exception {
        return getExtensionLoader(clazz, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> clazz, boolean alwaysInternal) throws Exception {
        return ReentrantLockHelper.putIfAbsentAsync(EXTENSION_LOADERS, clazz, () -> new ExtensionLoader<>(clazz, alwaysInternal));
    }

    /**
     * @param clazz 目标类
     * @return 验证clazz是否不为空且仅是接口或类
     */
    public static boolean isNormalClass(Class<?> clazz) {
        return clazz != null && !clazz.isArray() && !clazz.isAnnotation() && !clazz.isEnum() && !clazz.isAnonymousClass();
    }

    /**
     * @param method 目标方法
     * @return 验证method是否为公有非静态、非抽象且不属于Object基类方法
     * @since 2.1.0
     */
    public static boolean isNormalMethod(Method method) {
        return !Modifier.isStatic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers()) && Modifier.isPublic(method.getModifiers()) && !method.getDeclaringClass().equals(Object.class) && !EXCLUDED_METHOD_NAMES.contains(method.getName());
    }

    /**
     * @param field 目标成员
     * @return 验证field是否正常成员
     * @since 2.1.0
     */
    public static boolean isNormalField(Field field) {
        return !Modifier.isStatic(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers())
                && !Modifier.isFinal(field.getModifiers())
                && !Modifier.isNative(field.getModifiers())
                && !Modifier.isVolatile(field.getModifiers());
    }

    /**
     * @param clazz      目标类
     * @param superClass 父类
     * @return 判断类clazz是否是superClass类的子类对象
     */
    public static boolean isSubclassOf(Class<?> clazz, Class<?> superClass) {
        boolean flag = false;
        do {
            Class<?> cc = clazz.getSuperclass();
            if (cc != null) {
                if (cc.equals(superClass)) {
                    flag = true;
                    break;
                } else {
                    clazz = clazz.getSuperclass();
                }
            } else {
                break;
            }
        } while (clazz != Object.class);
        return flag;
    }

    /**
     * @param clazz          目标对象
     * @param interfaceClass 接口类型
     * @return 判断clazz类中是否实现了interfaceClass接口
     */
    public static boolean isInterfaceOf(Class<?> clazz, Class<?> interfaceClass) {
        boolean flag = false;
        do {
            for (Class<?> cc : clazz.getInterfaces()) {
                if (cc.equals(interfaceClass)) {
                    flag = true;
                    break;
                }
            }
            clazz = clazz.getSuperclass();
        } while (!flag && (clazz != null && clazz != Object.class));
        return flag;
    }

    /**
     * @param target          目标对象，即可以是Field对象、Method对象或是Class对象
     * @param annotationClass 注解类对象
     * @return 判断target对象是否存在annotationClass注解
     */
    public static boolean isAnnotationOf(Object target, Class<? extends Annotation> annotationClass) {
        if (target instanceof AnnotatedElement) {
            return ((AnnotatedElement) target).isAnnotationPresent(annotationClass);
        }
        return false;
    }

    /**
     * @param target          目标类对象
     * @param annotationClass 注解类对象
     * @param <A>             注解类型
     * @return 尝试获取目标类上声明的注解, 若目标类为代理类则尝试去除代理
     */
    public static <A extends Annotation> A getAnnotation(Object target, Class<A> annotationClass) {
        return getAnnotation(target.getClass(), annotationClass);
    }

    public static <A extends Annotation> A getAnnotation(Class<?> targetClass, Class<A> annotationClass) {
        A annotation = targetClass.getAnnotation(annotationClass);
        if (annotation == null && StringUtils.contains(targetClass.getName(), ANONYMOUS_CLASS_FLAG)) {
            try {
                Class<?> clazz = loadClass(StringUtils.substringBefore(targetClass.getName(), ANONYMOUS_CLASS_FLAG), targetClass);
                if (clazz != null) {
                    annotation = clazz.getAnnotation(annotationClass);
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        return annotation;
    }

    /**
     * @param targetClass     目标类对象
     * @param annotationClass 注解类对象
     * @return 尝试获取目标类被指定注解声明的包对象(包含上级包直到包对象为空)
     * @since 2.1.0
     */
    public static Package getPackage(Class<?> targetClass, Class<? extends Annotation> annotationClass) {
        Package targetPackage = targetClass.getPackage();
        if (!targetPackage.isAnnotationPresent(annotationClass)) {
            Class<?> clazz = findParentPackage(targetClass);
            while (clazz != null) {
                targetPackage = clazz.getPackage();
                if (targetPackage.isAnnotationPresent(annotationClass)) {
                    return targetPackage;
                }
                clazz = findParentPackage(clazz);
            }
        }
        return targetPackage.isAnnotationPresent(annotationClass) ? targetPackage : null;
    }

    public static <A extends Annotation> A getPackageAnnotation(Class<?> targetClass, Class<A> annotationClass) {
        Package targetPackage = getPackage(targetClass, annotationClass);
        return targetPackage != null ? targetPackage.getAnnotation(annotationClass) : null;
    }

    /**
     * 查找目标类上层包对象(若上层为空则继续向上查找，直至找不到为止)
     *
     * @param targetClass 目标类
     * @return 返回包对象
     * @since 2.1.0
     */
    public static Class<?> findParentPackage(Class<?> targetClass) {
        String packageName = targetClass.getPackage().getName();
        while (StringUtils.contains(packageName, PACKAGE_SEPARATOR)) {
            packageName = StringUtils.substringBeforeLast(packageName, PACKAGE_SEPARATOR);
            try {
                Class<?> clazz = targetClass.getClassLoader().loadClass(String.format("%s.%s", packageName, PACKAGE_INFO));
                if (clazz != null) {
                    return clazz;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    /**
     * @param clazz 类型
     * @return 返回类中实现的接口名称集合
     */
    public static String[] getInterfaceNames(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        List<String> names = new ArrayList<>();
        for (Class<?> i : interfaces) {
            names.add(i.getName());
        }
        return names.toArray(new String[0]);
    }

    /**
     * @param clazz 类对象
     * @return 获取泛型的数据类型集合，注：不适用于泛型嵌套, 即泛型里若包含泛型则返回此泛型的RawType类型
     */
    public static List<Class<?>> getParameterizedTypes(Class<?> clazz) {
        List<Class<?>> classes = new ArrayList<>();
        Type types = clazz.getGenericSuperclass();
        if (ParameterizedType.class.isAssignableFrom(types.getClass())) {
            for (Type type : ((ParameterizedType) types).getActualTypeArguments()) {
                if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
                    classes.add((Class<?>) ((ParameterizedType) type).getRawType());
                } else {
                    classes.add((Class<?>) type);
                }
            }
        } else {
            classes.add((Class<?>) types);
        }
        return classes;
    }

    /**
     * 获取clazz指定的类对象所有的Field对象（若包含其父类对象，直至其父类为空）
     *
     * @param clazz  目标类
     * @param parent 是否包含其父类对象
     * @return Field对象集合
     */
    public static List<Field> getFields(Class<?> clazz, boolean parent) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
            if (parent) {
                clazz = clazz.getSuperclass();
            } else {
                clazz = null;
            }
        }
        return fieldList;
    }

    /**
     * 获取clazz指定的类对象所有的Method对象（若包含其父类对象，直至其父类为空）
     *
     * @param clazz  目标类
     * @param parent 是否包含其父类对象
     * @return Method对象集合
     * @since 2.1.0
     */
    public static List<Method> getMethods(Class<?> clazz, boolean parent) {
        List<Method> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            if (parent) {
                clazz = clazz.getSuperclass();
            } else {
                clazz = null;
            }
        }
        return fieldList;
    }

    /**
     * @param <A>             注解类型
     * @param clazz           目标类
     * @param annotationClazz 目标注解类
     * @return 获取clazz类中成员声明的所有annotationClazz注解
     */
    public static <A extends Annotation> List<PairObject<Field, A>> getFieldAnnotations(Class<?> clazz, Class<A> annotationClazz) {
        List<PairObject<Field, A>> annotations = new ArrayList<>();
        ClassUtils.getFields(clazz, true).forEach((field) -> {
            A annotation = field.getAnnotation(annotationClazz);
            if (annotation != null) {
                annotations.add(new PairObject<>(field, annotation));
            }
        });
        return annotations;
    }

    /**
     * @param <A>             注解类型
     * @param clazz           目标类
     * @param annotationClazz 目标注解类
     * @return 获取clazz类中成员声明的第一个annotationClazz注解
     */
    public static <A extends Annotation> PairObject<Field, A> getFieldAnnotationFirst(Class<?> clazz, Class<A> annotationClazz) {
        PairObject<Field, A> returnAnn = null;
        for (Field field : ClassUtils.getFields(clazz, true)) {
            if (isNormalField(field)) {
                A annotation = field.getAnnotation(annotationClazz);
                if (annotation != null) {
                    returnAnn = new PairObject<>(field, annotation);
                    break;
                }
            }
        }
        return returnAnn;
    }

    /**
     * @param method 目标方法
     * @return 获取方法的参数名集合，若找不到则返回元素数量为0的空数组
     */
    public static String[] getMethodParamNames(final Method method) {
        return new AdaptiveParanamer().lookupParameterNames(method, false);
    }

    /**
     * @param clazz 数组类型
     * @return 返回数组元素类型
     */
    public static Class<?> getArrayClassType(Class<?> clazz) {
        if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            if (componentType == null) {
                String clazzName = StringUtils.substringBetween(clazz.getName(), "[L", ";");
                if (StringUtils.isNotBlank(clazzName)) {
                    try {
                        componentType = Class.forName(clazzName, false, clazz.getClassLoader());
                    } catch (ClassNotFoundException e) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                        }
                    }
                }
            }
            return componentType;
        }
        return null;
    }

    /**
     * @param <T>   目标类型
     * @param clazz 目标类型
     * @return 创建一个类对象实例，包裹它并赋予其简单对象属性操作能力，可能返回空
     */
    public static <T> BeanWrapper<T> wrapperClass(Class<T> clazz) {
        try {
            return wrapper(clazz.newInstance());
        } catch (IllegalAccessException | InstantiationException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    /**
     * @param <T>    目标类型
     * @param target 目标类对象
     * @return 包裹它并赋予其简单对象属性操作能力，可能返回空
     */
    @SuppressWarnings("unchecked")
    public static <T> BeanWrapper<T> wrapper(T target) {
        if (target instanceof Class) {
            return wrapperClass((Class<T>) target);
        }
        return new BeanWrapper<>(target);
    }

    /**
     * 内部类加载器
     */
    public static class InnerClassLoader extends URLClassLoader {

        InnerClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }
    }

    /**
     * 服务提供者加载器
     *
     * @param <T> 目标类型
     */
    public static class ExtensionLoader<T> {

        private final List<Class<T>> classesCache = new ArrayList<>();

        private final Map<String, T> instancesCache = new ConcurrentHashMap<>();

        ExtensionLoader(Class<T> clazz, boolean alwaysInternal) {
            if (clazz == null) {
                throw new NullArgumentException("clazz");
            }
            if (!clazz.isInterface()) {
                throw new IllegalArgumentException(String.format("Class type [%s] is not a interface.", clazz.getName()));
            }
            try {
                loadResources(String.format("META-INF/services/%s", clazz.getName()), clazz);
                if (classesCache.isEmpty() || alwaysInternal) {
                    loadResources(String.format("META-INF/services/internal/%s", clazz.getName()), clazz);
                }
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void loadResources(String resourceName, Class<T> clazz) throws IOException {
            Iterator<URL> resources = ResourceUtils.getResources(resourceName, clazz, true);
            while (resources.hasNext()) {
                try (InputStream inputStream = resources.next().openStream()) {
                    if (inputStream != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                            String lineStr;
                            do {
                                lineStr = reader.readLine();
                                if (StringUtils.isNotBlank(lineStr)) {
                                    lineStr = StringUtils.trim(lineStr);
                                    if (!StringUtils.startsWith(lineStr, "#")) {
                                        try {
                                            Class<T> loadedClass = (Class<T>) loadClass(lineStr, clazz);
                                            if (ClassUtils.isNormalClass(loadedClass) && !loadedClass.isInterface() && !classesCache.contains(loadedClass)) {
                                                classesCache.add(loadedClass);
                                            }
                                        } catch (ClassNotFoundException e) {
                                            if (LOG.isDebugEnabled()) {
                                                LOG.debug(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                                            }
                                        }
                                    }
                                }
                            } while (lineStr != null);
                        }
                    }
                }
            }
        }

        public Class<T> getExtensionClass() {
            return !classesCache.isEmpty() ? classesCache.get(0) : null;
        }

        public List<Class<T>> getExtensionClasses() {
            return Collections.unmodifiableList(classesCache);
        }

        public T getExtension() {
            return getExtension(getExtensionClass());
        }

        private T getExtension(Class<T> clazz) {
            try {
                if (clazz != null) {
                    synchronized (instancesCache) {
                        return ReentrantLockHelper.putIfAbsentAsync(instancesCache, clazz.getName(), () -> {
                            try {
                                return clazz.newInstance();
                            } catch (InstantiationException | IllegalAccessException e) {
                                throw new RuntimeException(e.getMessage(), e);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
            return null;
        }

        public List<T> getExtensions() {
            List<Class<T>> extClasses = getExtensionClasses();
            if (!extClasses.isEmpty()) {
                return extClasses.stream().map(this::getExtension).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }
    }

    /**
     * 类成员属性过滤器接口
     */
    public interface IFieldValueFilter {

        /**
         * 过滤成员属性
         *
         * @param fieldName  成员属性名称
         * @param fieldValue 属性值对象
         * @return 若返回true则该属性将被忽略
         */
        boolean filter(String fieldName, Object fieldValue);
    }

    /**
     * 类对象包裹器，赋予对象简单的属性操作能力
     *
     * @param <T> 对象类型
     * @author 刘镇 (suninformation@163.com) on 2012-12-23 上午12:46:50
     */
    public static class BeanWrapper<T> {

        private final T target;

        private final Map<String, Method> methodMap = new LinkedHashMap<>();

        private final Map<String, Field> fieldMap = new LinkedHashMap<>();

        BeanWrapper(T target) {
            this.target = target;
            //
            ClassUtils.getFields(target.getClass(), true).stream()
                    .filter((field) -> !Modifier.isStatic(field.getModifiers()))
                    .peek((field) -> field.setAccessible(true))
                    .forEachOrdered((field) -> this.fieldMap.put(field.getName(), field));
            ClassUtils.getMethods(target.getClass(), true).stream()
                    .filter(ClassUtils::isNormalMethod)
                    .forEachOrdered(method -> this.methodMap.put(method.getName(), method));
        }

        public T getTargetObject() {
            return target;
        }

        public Map<String, Field> getFieldMap() {
            return Collections.unmodifiableMap(fieldMap);
        }

        public Set<String> getFieldNames() {
            return fieldMap.keySet();
        }

        public Annotation[] getFieldAnnotations(String fieldName) {
            Field field = fieldMap.get(fieldName);
            return field == null ? null : field.getAnnotations();
        }

        public Collection<Field> getFields() {
            return fieldMap.values();
        }

        public Field getField(String fieldName) {
            return fieldMap.get(fieldName);
        }

        public Class<?> getFieldType(String fieldName) {
            return fieldMap.get(fieldName).getType();
        }

        public Collection<Method> getMethods() {
            return methodMap.values();
        }

        public Method getMethod(String methodName) {
            return methodMap.get(methodName);
        }

        public BeanWrapper<T> setValue(String fieldName, Object value) throws IllegalAccessException, InvocationTargetException {
            Field field = fieldMap.get(fieldName);
            if (field != null) {
                field.set(target, BlurObject.bind(value).toObjectValue(field.getType()));
            } else {
                // 尝试通过set方法进行赋值
                Method method = methodMap.get(String.format("set%s", StringUtils.capitalize(fieldName)));
                if (method != null && method.getParameterCount() == 1) {
                    method.invoke(target, BlurObject.bind(value).toObjectValue(method.getParameters()[0].getType()));
                }
            }
            return this;
        }

        public BeanWrapper<T> setValue(Field field, Object value) throws IllegalAccessException {
            field.set(target, BlurObject.bind(value).toObjectValue(field.getType()));
            return this;
        }

        public Object getValue(String fieldName) throws IllegalAccessException, InvocationTargetException {
            Field field = fieldMap.get(fieldName);
            if (field != null) {
                return field.get(target);
            }
            // 尝试通过get或is方法取值
            String capFieldName = StringUtils.capitalize(fieldName);
            Method method = methodMap.get(String.format("get%s", capFieldName));
            if (method == null) {
                method = methodMap.get(String.format("is%s", capFieldName));
            }
            if (method != null && method.getParameterCount() == 0) {
                return method.invoke(target);
            }
            return null;
        }

        public Object getValue(Field field) throws IllegalAccessException {
            return field.get(target);
        }

        public BeanWrapper<T> fromMap(Map<String, Object> map) {
            return fromMap(map, null);
        }

        public BeanWrapper<T> fromMap(Map<String, Object> map, IFieldValueFilter filter) {
            map.forEach((key, value) -> {
                try {
                    if (filter != null && filter.filter(key, value)) {
                        return;
                    }
                    setValue(key, value);
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                    // 当赋值发生异常时，忽略当前值
                }
            });
            return this;
        }

        public Map<String, Object> toMap() {
            return toMap(null);
        }

        public Map<String, Object> toMap(IFieldValueFilter filter) {
            Map<String, Object> returnValues = new HashMap<>(16);
            fieldMap.values().forEach(field -> {
                try {
                    Object fValue = getValue(field.getName());
                    if (filter != null && filter.filter(field.getName(), fValue)) {
                        return;
                    }
                    returnValues.put(field.getName(), fValue);
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                    // 当赋值发生异常时，忽略当前值
                }
            });
            return returnValues;
        }

        /**
         * @param dist 目标对象
         * @param <D>  目标对象类型
         * @return 拷贝当前对象的成员属性值到目标对象
         */
        public <D> D duplicate(D dist) {
            return duplicate(dist, null);
        }

        /**
         * @param dist   目标对象
         * @param filter 类成员属性过滤器
         * @param <D>    目标对象类型
         * @return 拷贝当前对象的成员属性值到目标对象
         */
        public <D> D duplicate(D dist, IFieldValueFilter filter) {
            BeanWrapper<D> wrapDist = wrapper(dist);
            getFieldNames().stream().filter((fieldName) -> (wrapDist.getFieldNames().contains(fieldName))).forEachOrdered((String fieldName) -> {
                Object fValue = null;
                try {
                    fValue = getValue(fieldName);
                    if (filter != null && filter.filter(fieldName, fValue)) {
                        return;
                    }
                    wrapDist.setValue(fieldName, fValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    // 当首次赋值发生异常时，若成员变量值不为NULL则尝试转换一下
                    if (fValue != null) {
                        try {
                            wrapDist.setValue(fieldName, BlurObject.bind(fValue).toObjectValue(wrapDist.getFieldType(fieldName)));
                        } catch (IllegalAccessException | InvocationTargetException ignored) {
                            // 当再次赋值发生异常时，彻底忽略当前值，不中断整个拷贝过程
                        }
                    }
                }
            });
            return wrapDist.getTargetObject();
        }
    }
}