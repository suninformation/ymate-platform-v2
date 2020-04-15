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
package net.ymate.platform.core.persistence.base;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.persistence.IShardingRule;
import net.ymate.platform.core.persistence.annotation.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据实体属性描述对象
 *
 * @author 刘镇 (suninformation@163.com) on 2014年2月16日 下午2:20:48
 */
public final class EntityMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(EntityMeta.class);

    private static final Map<Class<? extends IEntity>, EntityMeta> ENTITY_METAS = new ConcurrentHashMap<>();

    /**
     * 实体名称
     */
    private final String entityName;

    /**
     * 数据分片(表)规则注解
     */
    private final Class<? extends IShardingRule> shardingRuleClass;

    /**
     * 主键类型
     */
    private Class<?> primaryKeyClass;

    /**
     * 主键字段名称集合
     */
    private final List<String> primaryKeys;

    /**
     * 自动增长的字段名称集合
     */
    private final List<String> autoincrementProps;

    /**
     * 只读字段名称集合
     */
    private final List<String> readonlyProps;

    /**
     * 字段 to 属性映射
     */
    private final Map<String, PropertyMeta> properties;

    /**
     * 属性 to 字段映射
     */
    private final Map<String, PropertyMeta> fields;

    /**
     * 索引名称 to 索引映射
     */
    private final Map<String, IndexMeta> indexes;

    /**
     * 是否为复合主键
     */
    private boolean multiplePrimaryKey;

    /**
     * 是否为视图
     */
    private final boolean view;

    /**
     * 实体注释说明信息
     */
    private String comment;

    public static EntityMeta load(Class<? extends IEntity> entityClass) {
        EntityMeta entityMeta = EntityMeta.createAndGet(entityClass);
        if (entityMeta == null) {
            throw new IllegalArgumentException(String.format("Entity class [%s] invalid.", entityClass.getName()));
        }
        return entityMeta;
    }

    /**
     * @param targetClass 目标实体类对象
     * @return 创建数据实体属性描述对象
     */
    public static EntityMeta createAndGet(Class<? extends IEntity> targetClass) {
        // 判断clazz对象是否声明了@Entity注解
        if (ClassUtils.isAnnotationOf(targetClass, Entity.class)) {
            try {
                return ReentrantLockHelper.putIfAbsentAsync(ENTITY_METAS, targetClass, () -> {
                    // 注册数据实体类
                    String entityName = StringUtils.defaultIfBlank(targetClass.getAnnotation(Entity.class).value(), fieldNameToPropertyName(targetClass.getSimpleName(), 0));
                    ShardingRule shardingRule = targetClass.getAnnotation(ShardingRule.class);
                    EntityMeta entityMeta = new EntityMeta(entityName, ClassUtils.isAnnotationOf(targetClass, Readonly.class), shardingRule != null ? shardingRule.value() : null);
                    // 判断clazz对象是否声明了@Comment注解
                    if (ClassUtils.isAnnotationOf(targetClass, Comment.class)) {
                        entityMeta.comment = targetClass.getAnnotation(Comment.class).value();
                    }
                    // 处理字段属性
                    parseProperties(targetClass, entityMeta);
                    // 处理主键(排除视图)
                    if (!entityMeta.isView()) {
                        parsePrimaryKeys(targetClass, entityMeta);
                    }
                    // 处理索引
                    parseIndexes(targetClass, entityMeta);
                    //
                    return entityMeta;
                });
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return null;
    }

    public static Set<Class<? extends IEntity>> getEntityClasses() {
        return Collections.unmodifiableSet(ENTITY_METAS.keySet());
    }

    /**
     * 处理字段名称，使其符合JavaBean属性串格式<br>
     * 例如：属性名称为"user_name"，其处理结果为"UserName"<br>
     *
     * @param propertyName 属性名称
     * @return 符合JavaBean属性格式串
     */
    public static String propertyNameToFieldName(String propertyName) {
        if (StringUtils.contains(propertyName, '_')) {
            String[] words = StringUtils.split(propertyName, '_');
            if (words != null) {
                if (words.length > 1) {
                    StringBuilder returnBuilder = new StringBuilder();
                    for (String word : words) {
                        returnBuilder.append(StringUtils.capitalize(word.toLowerCase()));
                    }
                    return returnBuilder.toString();
                }
                return StringUtils.capitalize(words[0].toLowerCase());
            }
        }
        return StringUtils.capitalize(propertyName.toLowerCase());
    }

    /**
     * 将JavaBean属性串格式转换为下划线小写方式<br>
     * 例如：字段名称为"userName"，其处理结果为"user_name"<br>
     *
     * @param fieldName  字段名称
     * @param capitalize 大小写字母输出方式(小于等于0-全小写，等于1-首字母大写，大于1-全大写)
     * @return 转换以下划线间隔的字符串
     */
    public static String fieldNameToPropertyName(String fieldName, int capitalize) {
        if (StringUtils.isNotBlank(fieldName) && !StringUtils.contains(fieldName, '_')) {
            String currStr = fieldName.substring(0, 1);
            currStr = capitalize <= 0 ? currStr.toLowerCase() : currStr.toUpperCase();
            StringBuilder returnBuilder = new StringBuilder(currStr);
            for (int idx = 1; idx < fieldName.length(); idx++) {
                currStr = fieldName.substring(idx, idx + 1);
                if (currStr.equals(currStr.toUpperCase()) && !Character.isDigit(currStr.charAt(0))) {
                    returnBuilder.append("_");
                    currStr = capitalize > 0 ? currStr.toUpperCase() : currStr.toLowerCase();
                } else {
                    currStr = capitalize > 1 ? currStr.toUpperCase() : currStr.toLowerCase();
                }
                returnBuilder.append(currStr);
            }
            return returnBuilder.toString();
        }
        return fieldName;
    }

    /**
     * 处理@Property注解
     *
     * @param targetClass 目标类型
     * @param targetMeta  实体元数据
     */
    private static void parseProperties(Class<? extends IEntity> targetClass, EntityMeta targetMeta) {
        ClassUtils.getFields(targetClass, true).stream()
                .filter((field) -> ClassUtils.isNormalField(field) && ClassUtils.isAnnotationOf(field, Property.class))
                .map((field) -> getPropertyMeta(field.getAnnotation(Property.class), field, targetMeta))
                .filter(Objects::nonNull).peek((propertyMeta) -> targetMeta.properties.put(propertyMeta.getName(), propertyMeta)).forEachOrdered((propertyMeta) -> {
            targetMeta.fields.put(propertyMeta.getField().getName(), propertyMeta);
        });
    }

    private static PropertyMeta getPropertyMeta(Property property, Field field, EntityMeta targetMeta) {
        PropertyMeta propertyMeta = null;
        // 忽略属性名称已存在的Field对象
        String propName = StringUtils.defaultIfBlank(property.name(), fieldNameToPropertyName(field.getName(), 0));
        if (!targetMeta.containsProperty(propName)) {
            field.setAccessible(true);
            propertyMeta = new PropertyMeta(propName, field, property.autoincrement(),
                    property.sequenceName(), property.nullable(), property.unsigned(), property.length(), property.decimals(), property.type());
            if (ClassUtils.isAnnotationOf(field, Default.class)) {
                propertyMeta.defaultValue(field.getAnnotation(Default.class).value());
            }
            if (ClassUtils.isAnnotationOf(field, Comment.class)) {
                propertyMeta.comment(field.getAnnotation(Comment.class).value());
            }
            if (ClassUtils.isAnnotationOf(field, Conversion.class)) {
                propertyMeta.conversionType(field.getAnnotation(Conversion.class).to());
            }
            if (ClassUtils.isAnnotationOf(field, Readonly.class)) {
                propertyMeta.readonly(true);
                targetMeta.readonlyProps.add(propertyMeta.getName());
            }
            if (propertyMeta.isAutoincrement()) {
                targetMeta.autoincrementProps.add(propertyMeta.getName());
            }
        }
        return propertyMeta;
    }

    /**
     * 处理@Id注解
     *
     * @param targetClass 目标类型
     * @param targetMeta  实体元数据
     */
    private static void parsePrimaryKeys(Class<? extends IEntity> targetClass, EntityMeta targetMeta) {
        PairObject<Field, Id> id = ClassUtils.getFieldAnnotationFirst(targetClass, Id.class);
        if (id == null) {
            throw new IllegalArgumentException("Primary key annotation '@Id' not found.");
        }
        // 首选设置主键类型
        targetMeta.primaryKeyClass = id.getKey().getType();
        // 判断是否为复合型主键
        if (ClassUtils.isAnnotationOf(id.getKey().getType(), PK.class)) {
            if (ClassUtils.isInterfaceOf(id.getKey().getType(), IEntityPK.class)) {
                targetMeta.multiplePrimaryKey = true;
                ClassUtils.getFields(id.getKey().getType(), true).stream()
                        .filter((field) -> ClassUtils.isNormalField(field) && ClassUtils.isAnnotationOf(field, Property.class))
                        .map((field) -> getPropertyMeta(field.getAnnotation(Property.class), field, targetMeta))
                        .filter(Objects::nonNull).peek((propertyMeta) -> targetMeta.properties.put(propertyMeta.getName(), propertyMeta))
                        .peek((propertyMeta) -> targetMeta.fields.put(propertyMeta.getField().getName(), propertyMeta))
                        .forEachOrdered((propertyMeta) -> targetMeta.primaryKeys.add(propertyMeta.getName()));
            } else {
                throw new IllegalArgumentException("PrimaryKey must implement IEntityPK interface.");
            }
        } else {
            targetMeta.primaryKeys.add(targetMeta.fields.get(id.getKey().getName()).getName());
        }
    }

    /**
     * 处理@Indexes和@Index注解
     *
     * @param targetClass 目标类型
     * @param targetMeta  实体元数据
     */
    private static void parseIndexes(Class<? extends IEntity> targetClass, EntityMeta targetMeta) {
        List<Index> indexes = new ArrayList<>();
        if (ClassUtils.isAnnotationOf(targetClass, Indexes.class)) {
            indexes.addAll(Arrays.asList(targetClass.getAnnotation(Indexes.class).value()));
        }
        if (ClassUtils.isAnnotationOf(targetClass, Index.class)) {
            indexes.add(targetClass.getAnnotation(Index.class));
        }
        indexes.stream()
                // 索引名称和索引字段缺一不可
                .filter((index) -> (StringUtils.isNotBlank(index.name()) && ArrayUtils.isNotEmpty(index.fields())))
                // 索引名称不允许重复
                .filter((index) -> (!targetMeta.containsIndex(index.name())))
                .peek((index) -> {
                    // 每个字段名称都必须是有效的
                    Arrays.stream(index.fields()).filter(field -> !targetMeta.containsProperty(field)).forEach(field -> {
                        throw new IllegalArgumentException(String.format("Invalid index field '%s'.", field));
                    });
                }).forEachOrdered((index) -> {
            targetMeta.indexes.put(index.name(), new IndexMeta(index.name(), index.unique(), Arrays.asList(index.fields())));
        });
    }

    /**
     * 私有构造方法
     *
     * @param name         实体名称
     * @param view         是否为视图
     * @param shardingRule 据分片(表)规则
     */
    private EntityMeta(String name, boolean view, Class<? extends IShardingRule> shardingRule) {
        this.primaryKeys = new ArrayList<>();
        this.autoincrementProps = new ArrayList<>();
        this.readonlyProps = new ArrayList<>();
        this.properties = new LinkedHashMap<>();
        this.fields = new LinkedHashMap<>();
        this.indexes = new LinkedHashMap<>();
        //
        this.entityName = name;
        this.view = view;
        this.shardingRuleClass = shardingRule;
    }

    public EntityMeta unsupportedIfView() {
        if (view) {
            throw new UnsupportedOperationException("View does not support this operation.");
        }
        return this;
    }

    /**
     * @param indexName 索引名称
     * @return 返回索引名称是否存在
     */
    public boolean containsIndex(String indexName) {
        return this.indexes.containsKey(indexName);
    }

    /**
     * @param propertyName 字段名称
     * @return 返回字段名称是否存在
     */
    public boolean containsProperty(String propertyName) {
        return this.properties.containsKey(propertyName);
    }

    /**
     * @param fieldName 属性名称
     * @return 返回属性名称是否存在
     */
    public boolean containsField(String fieldName) {
        return this.fields.containsKey(fieldName);
    }

    /**
     * @return 返回是否存在自增长主键
     */
    public boolean hasAutoincrement() {
        return !this.autoincrementProps.isEmpty();
    }

    /**
     * @param propertyName 字段名称
     * @return 返回字段是否为自增长字段
     */
    public boolean isAutoincrement(String propertyName) {
        return this.autoincrementProps.contains(propertyName);
    }

    /**
     * @param propertyName 字段名称
     * @return 返回字段是否为主键
     */
    public boolean isPrimaryKey(String propertyName) {
        return this.primaryKeys.contains(propertyName);
    }

    /**
     * @return 是否为复合主键
     */
    public boolean isMultiplePrimaryKey() {
        return this.multiplePrimaryKey;
    }

    /**
     * @param propertyName 字段名称
     * @return 返回字段是否为只读
     */
    public boolean isReadonly(String propertyName) {
        return this.readonlyProps.contains(propertyName);
    }

    /**
     * @return 返回当前实体是否为视图
     */
    public boolean isView() {
        return view;
    }

    /**
     * @return 返回所有自增长字段名称集合
     */
    public List<String> getAutoincrementKeys() {
        return Collections.unmodifiableList(this.autoincrementProps);
    }

    /**
     * @return 返回实体名称
     */
    public String getEntityName() {
        return this.entityName;
    }

    /**
     * @return 返回实体数据分片(表)规则注解对象
     */
    public Class<? extends IShardingRule> getShardingRule() {
        return shardingRuleClass;
    }

    /**
     * @return 返回主键类型
     */
    public Class<?> getPrimaryKeyClass() {
        return this.primaryKeyClass;
    }

    /**
     * @return 返回主键字段名称集合
     */
    public List<String> getPrimaryKeys() {
        return Collections.unmodifiableList(this.primaryKeys);
    }

    /**
     * @return 返回实体属性描述对象集合
     */
    public Collection<PropertyMeta> getProperties() {
        return Collections.unmodifiableCollection(this.properties.values());
    }

    public Collection<String> getPropertyNames() {
        return Collections.unmodifiableCollection(this.properties.keySet());
    }

    public PropertyMeta getPropertyByName(String propertyName) {
        return this.properties.get(propertyName);
    }

    public PropertyMeta getPropertyByField(String fieldName) {
        return this.fields.get(fieldName);
    }

    /**
     * @return 返回实体索引描述对象集合
     */
    public Collection<IndexMeta> getIndexes() {
        return Collections.unmodifiableCollection(this.indexes.values());
    }

    /**
     * @return 返回实体注释说明信息
     */
    public String getComment() {
        return this.comment;
    }

    @Override
    public String toString() {
        return String.format("Entity [entityName='%s', primaryKeyClass=%s, primaryKeys=%s, autoincrementProps=%s, readonlyProps=%s, properties=%s, fields=%s, indexes=%s, multiplePrimaryKey=%s, comment='%s']", entityName, primaryKeyClass, primaryKeys, autoincrementProps, readonlyProps, properties, fields, indexes, multiplePrimaryKey, comment);
    }
}
