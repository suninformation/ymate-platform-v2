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
package net.ymate.platform.persistence.jdbc;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.beans.annotation.Inject;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.base.IResultSetHandler;
import net.ymate.platform.persistence.jdbc.entity.PermissionEntity;
import net.ymate.platform.persistence.jdbc.entity.UserEntity;
import net.ymate.platform.persistence.jdbc.entity.UserPermissionEntity;
import net.ymate.platform.persistence.jdbc.query.*;
import net.ymate.platform.persistence.jdbc.support.ResultSetHelper;
import net.ymate.platform.test.YMPJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据库会话测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026/3/10 10:00
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class DatabaseSessionTest {

    @Inject
    private JDBC jdbc;

    @Before
    public void beforeTest() throws Exception {
        // 创建测试表
        createTestTable(UserEntity.class, "sys_user");
        createTestTable(PermissionEntity.class, "sys_permission");
        createTestTable(UserPermissionEntity.class, "sys_user_permission");
    }

    private void createTestTable(Class<? extends IEntity<?>> entityClass, String tableName) throws Exception {
        System.out.println("=== 创建测试表: " + tableName + " ===");
        try {
            if (jdbc == null) {
                System.err.println("JDBC 实例未注入，跳过表创建");
                return;
            }
            EntityMeta entityMeta = EntityMeta.createAndGet(entityClass);
            jdbc.openSession((IDatabaseSessionExecutor<Void>) session -> {
                Table table = new Table(session.getConnectionHolder().getDialect(), entityMeta);
                String dropTableSQL = table.toDropSQL();
                String createTableSQL = table.toCreateSQL();
                System.out.println("生成的删除表 SQL: " + dropTableSQL);
                System.out.println("生成的创建表 SQL: " + createTableSQL);
                List<String> sqls = new ArrayList<>();
                sqls.add(dropTableSQL);
                sqls.add(createTableSQL);
                int effectCount = BatchSQL.execSQL(session.getConnectionHolder().getOwner(), sqls);
                System.out.println("批量执行 SQL 影响行数: " + effectCount);
                System.out.println("创建测试表成功: " + tableName);
                return null;
            });
        } catch (Exception e) {
            System.err.println("创建测试表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testDatabaseSessionAllOperations() throws Exception {
        System.out.println("=== 测试 IDatabaseSession 所有操作 ===");

        jdbc.openSession((IDatabaseSessionExecutor<Void>) session -> {
            // 测试基本操作
            System.out.println("\n1. 测试基本操作");

            // 测试插入操作 - 用户
            System.out.println("\n1.1 测试插入操作 - 用户");
            UserEntity user = new UserEntity();
            user.setId("user_001");
            user.setUsername("admin");
            user.setPassword("123456");
            user.setNickname("管理员");
            user.setEmail("admin@example.com");
            user.setPhone("13800138000");
            user.setStatus(1);
            user.setCreateTime(new Date());

            UserEntity insertedUser = session.insert(user);
            System.out.println("插入用户操作结果: " + insertedUser.getUsername());
            Assert.assertNotNull("插入用户操作应返回实体", insertedUser);
            Assert.assertEquals("插入用户操作应返回正确的用户名", "admin", insertedUser.getUsername());

            // 测试插入操作 - 权限
            System.out.println("\n1.2 测试插入操作 - 权限");
            PermissionEntity permission = new PermissionEntity();
            permission.setPermissionCode("USER_MANAGE");
            permission.setPermissionName("用户管理");
            permission.setPermissionType(1);
            permission.setStatus(1);
            permission.setCreateTime(new Date());

            PermissionEntity insertedPermission = session.insert(permission);
            System.out.println("插入权限操作结果: " + insertedPermission.getPermissionCode() + " (ID: " + insertedPermission.getId() + ")");
            Assert.assertNotNull("插入权限操作应返回实体", insertedPermission);
            Assert.assertEquals("插入权限操作应返回正确的权限编码", "USER_MANAGE", insertedPermission.getPermissionCode());

            // 测试插入操作 - 用户权限关系
            System.out.println("\n1.3 测试插入操作 - 用户权限关系");
            UserPermissionEntity.UserPermissionPK userPermPK = new UserPermissionEntity.UserPermissionPK(
                    user.getId(), insertedPermission.getId());
            UserPermissionEntity userPermission = new UserPermissionEntity(userPermPK);
            userPermission.setGrantType(1);
            userPermission.setCreateTime(new Date());

            UserPermissionEntity insertedUserPerm = session.insert(userPermission);
            System.out.println("插入用户权限关系操作结果: UserID=" + userPermPK.getUserId() + ", PermissionID=" + userPermPK.getPermissionId());
            Assert.assertNotNull("插入用户权限关系操作应返回实体", insertedUserPerm);

            // 测试查询操作
            System.out.println("\n2. 测试查询操作");

            // 测试根据ID查询
            System.out.println("\n2.1 测试根据ID查询");
            UserEntity loadedUser = session.find(EntitySQL.create(UserEntity.class), user.getId());
            System.out.println("根据ID查询用户结果: " + loadedUser.getUsername());
            Assert.assertNotNull("根据ID查询用户应返回实体", loadedUser);

            PermissionEntity loadedPermission = session.find(EntitySQL.create(PermissionEntity.class), insertedPermission.getId());
            System.out.println("根据ID查询权限结果: " + loadedPermission.getPermissionName());
            Assert.assertNotNull("根据ID查询权限应返回实体", loadedPermission);

            // 测试条件查询
            System.out.println("\n2.2 测试条件查询");
            // 使用正确的条件构建方式
            UserEntity foundUser = session.findFirst(EntitySQL.create(UserEntity.class), Where.create(Cond.create().eq("username").param("admin")));
            System.out.println("条件查询用户结果: " + foundUser.getUsername());
            Assert.assertNotNull("条件查询用户应返回实体", foundUser);

            // 测试分页查询
            System.out.println("\n2.3 测试分页查询");
            IResultSet<UserEntity> userResultSet = session.find(EntitySQL.create(UserEntity.class), Page.create(1).pageSize(10));
            System.out.println("分页查询用户结果数量: " + userResultSet.getResultData().size());
            Assert.assertTrue("分页查询用户应返回至少1条记录", userResultSet.isResultsAvailable());

            // 测试更新操作
            System.out.println("\n3. 测试更新操作");
            loadedUser.setNickname("超级管理员");
            loadedUser.setUpdateTime(new Date());
            UserEntity updatedUser = session.update(loadedUser);
            System.out.println("更新用户操作结果: " + updatedUser.getNickname());
            Assert.assertNotNull("更新用户操作应返回实体", updatedUser);
            Assert.assertEquals("更新用户操作应返回正确的昵称", "超级管理员", updatedUser.getNickname());

            // 测试删除操作
            System.out.println("\n4. 测试删除操作");
            int deleteCount = session.delete(UserPermissionEntity.class, userPermPK);
            System.out.println("删除用户权限关系操作影响行数: " + deleteCount);
            Assert.assertEquals("删除用户权限关系操作应影响1行", 1, deleteCount);

            // 测试计数操作
            System.out.println("\n5. 测试计数操作");
            long userCount = session.count(UserEntity.class);
            System.out.println("用户表记录数量: " + userCount);
            Assert.assertTrue("用户表记录数量应大于0", userCount > 0);

            long permissionCount = session.count(PermissionEntity.class);
            System.out.println("权限表记录数量: " + permissionCount);
            Assert.assertTrue("权限表记录数量应大于0", permissionCount > 0);

            long userPermCount = session.count(UserPermissionEntity.class);
            System.out.println("用户权限关系表记录数量: " + userPermCount);
            Assert.assertEquals("用户权限关系表记录数量应等于0", 0, userPermCount);

            // 测试批量操作
            System.out.println("\n6. 测试批量操作");

            // 批量插入权限
            List<PermissionEntity> permissions = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                PermissionEntity perm = new PermissionEntity();
                perm.setPermissionCode("PERM_" + i);
                perm.setPermissionName("权限" + i);
                perm.setPermissionType(1);
                perm.setStatus(1);
                perm.setCreateTime(new Date());
                permissions.add(perm);
            }
            List<PermissionEntity> insertedPermissions = session.insert(permissions);
            System.out.println("批量插入权限操作影响行数: " + insertedPermissions.size());
            Assert.assertEquals("批量插入权限操作应影响3行", 3, insertedPermissions.size());

            // 批量更新权限
            for (PermissionEntity perm : insertedPermissions) {
                perm.setStatus(0);
            }
            List<PermissionEntity> updatedPermissions = session.update(insertedPermissions, null);
            System.out.println("批量更新权限操作影响行数: " + updatedPermissions.size());
            Assert.assertEquals("批量更新权限操作应影响3行", 3, updatedPermissions.size());

            // 批量删除权限
            List<PermissionEntity> deletedPermissions = session.delete(insertedPermissions);
            System.out.println("批量删除权限操作影响行数: " + deletedPermissions.size());
            Assert.assertEquals("批量删除权限操作应影响3行", 3, deletedPermissions.size());

            return null;
        });
    }

    @Test
    public void testDatabaseSessionEventListener() throws Exception {
        System.out.println("=== 测试 IDatabaseSession 事件监听器 ===");

        // 创建事件监听器
        final AtomicInteger queryBeforeCount = new AtomicInteger(0);
        final AtomicInteger queryAfterCount = new AtomicInteger(0);
        final AtomicInteger insertBeforeCount = new AtomicInteger(0);
        final AtomicInteger insertAfterCount = new AtomicInteger(0);
        final AtomicInteger insertIfNotExistBeforeCount = new AtomicInteger(0);
        final AtomicInteger insertIfNotExistAfterCount = new AtomicInteger(0);
        final AtomicInteger updateBeforeCount = new AtomicInteger(0);
        final AtomicInteger updateAfterCount = new AtomicInteger(0);
        final AtomicInteger upsertBeforeCount = new AtomicInteger(0);
        final AtomicInteger upsertAfterCount = new AtomicInteger(0);
        final AtomicInteger deleteBeforeCount = new AtomicInteger(0);
        final AtomicInteger deleteAfterCount = new AtomicInteger(0);

        IDatabaseSessionEventListener eventListener = new IDatabaseSessionEventListener() {
            @Override
            public void onQueryBefore(DatabaseSessionEventContext eventContext) {
                System.out.println("onQueryBefore: " + eventContext.getSql());
                queryBeforeCount.incrementAndGet();
            }

            @Override
            public void onQueryAfter(DatabaseSessionEventContext eventContext) {
                System.out.println("onQueryAfter: " + eventContext.getSql());
                queryAfterCount.incrementAndGet();
            }

            @Override
            public void onInsertBefore(DatabaseSessionEventContext eventContext) {
                System.out.println("onInsertBefore: " + eventContext.getSql());
                insertBeforeCount.incrementAndGet();
            }

            @Override
            public void onInsertAfter(DatabaseSessionEventContext eventContext) {
                System.out.println("onInsertAfter: " + eventContext.getSql());
                insertAfterCount.incrementAndGet();
            }

            @Override
            public void onInsertIfNotExistBefore(DatabaseSessionEventContext eventContext) {
                System.out.println("onInsertIfNotExistBefore: " + eventContext.getSql());
                insertIfNotExistBeforeCount.incrementAndGet();
            }

            @Override
            public void onInsertIfNotExistAfter(DatabaseSessionEventContext eventContext) {
                System.out.println("onInsertIfNotExistAfter: " + eventContext.getSql());
                insertIfNotExistAfterCount.incrementAndGet();
            }

            @Override
            public void onUpdateBefore(DatabaseSessionEventContext eventContext) {
                System.out.println("onUpdateBefore: " + eventContext.getSql());
                updateBeforeCount.incrementAndGet();
            }

            @Override
            public void onUpdateAfter(DatabaseSessionEventContext eventContext) {
                System.out.println("onUpdateAfter: " + eventContext.getSql());
                updateAfterCount.incrementAndGet();
            }

            @Override
            public void onUpsertBefore(DatabaseSessionEventContext eventContext) {
                System.out.println("onUpsertBefore: " + eventContext.getSql());
                upsertBeforeCount.incrementAndGet();
            }

            @Override
            public void onUpsertAfter(DatabaseSessionEventContext eventContext) {
                System.out.println("onUpsertAfter: " + eventContext.getSql());
                upsertAfterCount.incrementAndGet();
            }

            @Override
            public void onRemoveBefore(DatabaseSessionEventContext eventContext) {
                System.out.println("onRemoveBefore: " + eventContext.getSql());
                deleteBeforeCount.incrementAndGet();
            }

            @Override
            public void onRemoveAfter(DatabaseSessionEventContext eventContext) {
                System.out.println("onRemoveAfter: " + eventContext.getSql());
                deleteAfterCount.incrementAndGet();
            }
        };

        jdbc.openSession((IDatabaseSessionExecutor<Void>) session -> {
            // 设置事件监听器
            session.setSessionEventListener(eventListener);

            // 测试插入操作
            System.out.println("\n测试插入操作事件");
            UserEntity user = new UserEntity();
            user.setId("user_002");
            user.setUsername("test");
            user.setPassword("123456");
            user.setStatus(1);
            user.setCreateTime(new Date());
            session.insert(user);

            // 测试查询操作
            System.out.println("\n测试查询操作事件");
            UserEntity loadedUser = session.find(EntitySQL.create(UserEntity.class), user.getId());

            // 测试更新操作
            System.out.println("\n测试更新操作事件");
            loadedUser.setNickname("测试用户");
            session.update(loadedUser);

            // 测试 upsert 操作
            System.out.println("\n测试 upsert 操作事件");
            UserEntity userForUpsert = new UserEntity();
            userForUpsert.setId("user_003");
            userForUpsert.setUsername("upsert_user");
            userForUpsert.setPassword("123456");
            userForUpsert.setNickname("Upsert用户");
            userForUpsert.setStatus(1);
            userForUpsert.setCreateTime(new Date());
            session.upsert(userForUpsert);

            // 测试 insertIfNotExist 操作
            System.out.println("\n测试 insertIfNotExist 操作事件");
            UserEntity userForInsertIfNotExist = new UserEntity();
            userForInsertIfNotExist.setId("user_004");
            userForInsertIfNotExist.setUsername("insert_if_not_exist_user");
            userForInsertIfNotExist.setPassword("123456");
            userForInsertIfNotExist.setNickname("InsertIfNotExist用户");
            userForInsertIfNotExist.setStatus(1);
            userForInsertIfNotExist.setCreateTime(new Date());
            session.insertIfNotExist(userForInsertIfNotExist);

            // 测试删除操作
            System.out.println("\n测试删除操作事件");
            session.delete(UserEntity.class, user.getId());

            // 验证事件监听器被调用
            System.out.println("\n事件监听器调用统计:");
            System.out.println("onQueryBefore: " + queryBeforeCount.get());
            System.out.println("onQueryAfter: " + queryAfterCount.get());
            System.out.println("onInsertBefore: " + insertBeforeCount.get());
            System.out.println("onInsertAfter: " + insertAfterCount.get());
            System.out.println("onInsertIfNotExistBefore: " + insertIfNotExistBeforeCount.get());
            System.out.println("onInsertIfNotExistAfter: " + insertIfNotExistAfterCount.get());
            System.out.println("onUpdateBefore: " + updateBeforeCount.get());
            System.out.println("onUpdateAfter: " + updateAfterCount.get());
            System.out.println("onUpsertBefore: " + upsertBeforeCount.get());
            System.out.println("onUpsertAfter: " + upsertAfterCount.get());
            System.out.println("onRemoveBefore: " + deleteBeforeCount.get());
            System.out.println("onRemoveAfter: " + deleteAfterCount.get());

            Assert.assertTrue("事件监听器应被调用", queryBeforeCount.get() > 0);
            Assert.assertTrue("事件监听器应被调用", insertBeforeCount.get() > 0);
            Assert.assertTrue("事件监听器应被调用", insertIfNotExistBeforeCount.get() > 0);
            Assert.assertTrue("事件监听器应被调用", updateBeforeCount.get() > 0);
            Assert.assertTrue("事件监听器应被调用", upsertBeforeCount.get() > 0);
            Assert.assertTrue("事件监听器应被调用", deleteBeforeCount.get() > 0);

            return null;
        });
    }

    @Test
    public void testThreeTableJoinQuery() throws Exception {
        System.out.println("=== 测试三表关联查询 ===");

        jdbc.openSession((IDatabaseSessionExecutor<Void>) session -> {
            // 准备测试数据
            System.out.println("\n1. 准备测试数据");

            // 插入用户
            UserEntity user1 = new UserEntity();
            user1.setId("user_003");
            user1.setUsername("user1");
            user1.setPassword("123456");
            user1.setNickname("用户1");
            user1.setStatus(1);
            user1.setCreateTime(new Date());
            session.insert(user1);

            UserEntity user2 = new UserEntity();
            user2.setId("user_004");
            user2.setUsername("user2");
            user2.setPassword("123456");
            user2.setNickname("用户2");
            user2.setStatus(1);
            user2.setCreateTime(new Date());
            session.insert(user2);

            // 插入权限
            PermissionEntity perm1 = new PermissionEntity();
            perm1.setPermissionCode("READ");
            perm1.setPermissionName("读取权限");
            perm1.setPermissionType(1);
            perm1.setStatus(1);
            perm1.setCreateTime(new Date());
            PermissionEntity insertedPerm1 = session.insert(perm1);

            PermissionEntity perm2 = new PermissionEntity();
            perm2.setPermissionCode("WRITE");
            perm2.setPermissionName("写入权限");
            perm2.setPermissionType(1);
            perm2.setStatus(1);
            perm2.setCreateTime(new Date());
            PermissionEntity insertedPerm2 = session.insert(perm2);

            // 插入用户权限关系
            UserPermissionEntity.UserPermissionPK pk1 = new UserPermissionEntity.UserPermissionPK(user1.getId(), insertedPerm1.getId());
            UserPermissionEntity userPerm1 = new UserPermissionEntity(pk1);
            userPerm1.setGrantType(1);
            userPerm1.setCreateTime(new Date());
            session.insert(userPerm1);

            UserPermissionEntity.UserPermissionPK pk2 = new UserPermissionEntity.UserPermissionPK(user1.getId(), insertedPerm2.getId());
            UserPermissionEntity userPerm2 = new UserPermissionEntity(pk2);
            userPerm2.setGrantType(1);
            userPerm2.setCreateTime(new Date());
            session.insert(userPerm2);

            UserPermissionEntity.UserPermissionPK pk3 = new UserPermissionEntity.UserPermissionPK(user2.getId(), insertedPerm1.getId());
            UserPermissionEntity userPerm3 = new UserPermissionEntity(pk3);
            userPerm3.setGrantType(1);
            userPerm3.setCreateTime(new Date());
            session.insert(userPerm3);

            // 执行三表关联查询 - 使用 SQL 语句
            System.out.println("\n2. 执行三表关联查询");
            Select select = Select.create(jdbc)
                    .field("u", "id", "user_id")
                    .field("u", "username")
                    .field("u", "nickname")
                    .field("p", "id", "permission_id")
                    .field("p", "permission_code")
                    .field("p", "permission_name")
                    .field("up", "grant_type")
                    .from(UserEntity.class, "u")
                    .innerJoin(UserPermissionEntity.class, "up", Cond.create().eqField(Fields.field("u", "id"), Fields.field("up", "user_id")))
                    .innerJoin(PermissionEntity.class, "p", Cond.create().eqField(Fields.field("up", "permission_id"), Fields.field("p", "id")))
                    .where(Cond.create().eq("u", "status").param(1))
                    .orderByAsc("u", "username").orderByAsc("p", "permission_code");

            System.out.println("三表关联查询 SQL: " + select.toString());

            // 使用原始 SQL 查询
            IResultSet<java.util.Map<String, Object>> resultSet = session.find(select.toSQL(), IResultSetHandler.MAP.create());
            System.out.println("三表关联查询结果数量: " + resultSet.getResultData().size());

            // 输出查询结果
            System.out.println("\n3. 输出查询结果");
            System.out.println(ResultSetHelper.bind(resultSet).toString());

            Assert.assertTrue("三表关联查询应返回至少3条记录", resultSet.getResultData().size() >= 3);

            return null;
        });
    }

    @Test
    public void testSqlIntercept() throws Exception {
        System.out.println("=== 测试 SQL 拦截并增加附加 WHERE 条件 ===");

        jdbc.openSession((IDatabaseSessionExecutor<Void>) session -> {
            // 准备测试数据
            System.out.println("\n1. 准备测试数据");

            // 插入用户
            for (int i = 1; i <= 5; i++) {
                UserEntity user = new UserEntity();
                user.setId("user_00" + i);
                user.setUsername("user" + i);
                user.setPassword("123456");
                user.setNickname("用户" + i);
                user.setStatus(i % 2); // 0或1
                user.setCreateTime(new Date());
                session.insert(user);
            }

            // 测试 SQL 拦截
            System.out.println("\n2. 测试 SQL 拦截");

            // 创建一个自定义的事件监听器，拦截并修改 SQL
            IDatabaseSessionEventListener sqlInterceptor = new IDatabaseSessionEventListener() {
                @Override
                public void onQueryBefore(DatabaseSessionEventContext eventContext) {
                    String originalSql = eventContext.getSql();
                    System.out.println("原始 SQL: " + originalSql);

                    // 使用 JSqlParser 解析并修改 SQL
                    try {
                        net.sf.jsqlparser.statement.Statement statement = new CCJSqlParserManager().parse(new StringReader(originalSql));

                        if (statement instanceof net.sf.jsqlparser.statement.select.Select) {
                            PlainSelect plainSelect = ((net.sf.jsqlparser.statement.select.Select) statement).getPlainSelect();

                            // 创建 status = 1 的条件表达式
                            EqualsTo statusCondition = new net.sf.jsqlparser.expression.operators.relational.EqualsTo();
                            statusCondition.setLeftExpression(new net.sf.jsqlparser.schema.Column("status"));
                            statusCondition.setRightExpression(new net.sf.jsqlparser.expression.LongValue(1));

                            if (plainSelect.getWhere() == null) {
                                // 如果没有 WHERE 子句，直接设置
                                plainSelect.setWhere(statusCondition);
                            } else {
                                // 如果有 WHERE 子句，使用 AND 连接
                                net.sf.jsqlparser.expression.Expression andExpression =
                                        new net.sf.jsqlparser.expression.operators.conditional.AndExpression(
                                                plainSelect.getWhere(),
                                                statusCondition
                                        );
                                plainSelect.setWhere(andExpression);
                            }

                            String modifiedSql = statement.toString();
                            System.out.println("修改后的 SQL: " + modifiedSql);

                            // 更新事件上下文的 SQL
                            eventContext.setSql(modifiedSql);
                        }
                    } catch (JSQLParserException e) {
                        System.err.println("JSqlParser 解析失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            };

            // 设置事件监听器
            session.setSessionEventListener(sqlInterceptor);

            // 执行查询
            System.out.println("\n3. 执行查询");
            IResultSet<UserEntity> resultSet = session.find(EntitySQL.create(UserEntity.class));
            System.out.println("查询结果数量: " + resultSet.getResultData().size());

            // 验证结果
            System.out.println("\n4. 验证结果");
            for (UserEntity user : resultSet.getResultData()) {
                System.out.println("用户: " + user.getUsername() + " (状态: " + user.getStatus() + ")");
                Assert.assertEquals("用户状态应被过滤为1", Integer.valueOf(1), user.getStatus());
            }

            return null;
        });
    }

    @Test
    public void testAtomicOperations() throws Exception {
        System.out.println("=== 测试原子操作 ===");

        jdbc.openSession((IDatabaseSessionExecutor<Void>) session -> {
            // 测试 upsert 操作
            System.out.println("\n1. 测试 upsert 操作");
            UserEntity user = new UserEntity();
            user.setId("user_upsert");
            user.setUsername("upsert_user");
            user.setPassword("123456");
            user.setStatus(1);
            user.setCreateTime(new Date());

            UserEntity upsertResult1 = session.upsert(user);
            System.out.println("upsert操作结果: " + upsertResult1.getUsername());
            Assert.assertNotNull("upsert操作应返回实体", upsertResult1);

            // 再次执行 upsert（更新操作）
            user.setNickname("Upsert 用户");
            user.setUpdateTime(new Date());
            UserEntity upsertResult2 = session.upsert(user);
            System.out.println("upsert更新操作结果: " + upsertResult2.getNickname());
            Assert.assertNotNull("upsert更新操作应返回实体", upsertResult2);
            Assert.assertEquals("upsert更新操作应返回正确的昵称", "Upsert 用户", upsertResult2.getNickname());

            // 测试 insertIfNotExist 操作
            System.out.println("\n2. 测试 insertIfNotExist 操作");
            UserEntity user2 = new UserEntity();
            user2.setId("user_insert_if_not_exist");
            user2.setUsername("insert_if_not_exist");
            user2.setPassword("123456");
            user2.setStatus(1);
            user2.setCreateTime(new Date());

            UserEntity insertIfNotExistResult1 = session.insertIfNotExist(user2);
            System.out.println("insertIfNotExist操作结果: " + (insertIfNotExistResult1 != null));
            Assert.assertNotNull("insertIfNotExist操作应返回实体", insertIfNotExistResult1);

            // 再次执行 insertIfNotExist（应返回 null）
            UserEntity insertIfNotExistResult2 = session.insertIfNotExist(user2);
            System.out.println("insertIfNotExist重复操作结果: " + (insertIfNotExistResult2 == null));
            Assert.assertNull("insertIfNotExist重复操作应返回null", insertIfNotExistResult2);

            return null;
        });
    }

    @Test
    public void testDatabaseSessionEventListenerWithException() throws Exception {
        System.out.println("=== 测试 IDatabaseSession 事件监听器异常打断情况 ===");

        jdbc.openSession((IDatabaseSessionExecutor<Void>) session -> {
            // 创建一个会抛出异常的事件监听器
            IDatabaseSessionEventListener exceptionListener = new IDatabaseSessionEventListener() {
                @Override
                public void onInsertBefore(DatabaseSessionEventContext eventContext) throws Exception {
                    String sql = eventContext.getSql();
                    System.out.println("onInsertBefore: " + sql);
                    System.out.println("检查是否应该抛出异常...");

                    // 检查参数中是否包含特定的用户名，如果是则抛出异常
                    if (eventContext.getParams() != null) {
                        for (Object param : eventContext.getParams().params()) {
                            if ("exception_user".equals(param)) {
                                String errorMsg = "检测到敏感用户，操作被中断: exception_user";
                                System.err.println("异常信息: " + errorMsg);
                                System.err.println("异常时间: " + new Date());
                                System.err.println("异常SQL: " + sql);
                                System.err.println("异常参数: " + param);
                                throw new RuntimeException(errorMsg);
                            }
                        }
                    }
                }

                @Override
                public void onInsertAfter(DatabaseSessionEventContext eventContext) throws Exception {
                    String sql = eventContext.getSql();
                    System.out.println("onInsertAfter: " + sql);
                    System.out.println("插入操作成功完成");
                }
            };

            // 设置事件监听器
            session.setSessionEventListener(exceptionListener);

            // 测试1: 正常插入操作（应该成功）
            System.out.println("\n测试1: 正常插入操作");
            try {
                UserEntity normalUser = new UserEntity();
                normalUser.setId("user_normal_001");
                normalUser.setUsername("normal_user");
                normalUser.setPassword("123456");
                normalUser.setNickname("正常用户");
                normalUser.setStatus(1);
                normalUser.setCreateTime(new Date());
                UserEntity insertedUser = session.insert(normalUser);
                System.out.println("正常用户插入成功: " + insertedUser.getUsername());
            } catch (Exception e) {
                System.err.println("正常用户插入失败（不应该发生）: " + e.getMessage());
                throw e;
            }

            // 测试2: 会触发异常的插入操作（应该失败）
            System.out.println("\n测试2: 会触发异常的插入操作");
            try {
                UserEntity exceptionUser = new UserEntity();
                exceptionUser.setId("user_exception_001");
                exceptionUser.setUsername("exception_user");
                exceptionUser.setPassword("123456");
                exceptionUser.setNickname("异常用户");
                exceptionUser.setStatus(1);
                exceptionUser.setCreateTime(new Date());
                session.insert(exceptionUser);
                System.err.println("错误: 异常用户插入成功（不应该成功）");
                Assert.fail("异常用户插入应该被中断");
            } catch (RuntimeException e) {
                System.out.println("成功捕获预期异常: " + e.getMessage());
                System.out.println("异常类型: " + e.getClass().getName());
                Assert.assertTrue("异常消息应包含敏感用户信息", e.getMessage().contains("exception_user"));
            }

            // 测试3: 验证异常用户确实没有被插入
            System.out.println("\n测试3: 验证异常用户确实没有被插入");
            UserEntity loadedUser = session.find(EntitySQL.create(UserEntity.class), "user_exception_001");
            Assert.assertNull("异常用户应该不存在", loadedUser);
            System.out.println("验证通过: 异常用户确实没有被插入到数据库");

            // 测试4: 在更新操作中触发异常
            System.out.println("\n测试4: 在更新操作中触发异常");
            IDatabaseSessionEventListener updateExceptionListener = new IDatabaseSessionEventListener() {
                @Override
                public void onUpdateBefore(DatabaseSessionEventContext eventContext) throws Exception {
                    String sql = eventContext.getSql();
                    System.out.println("onUpdateBefore: " + sql);
                    System.out.println("检查是否应该抛出异常...");

                    // 检查参数中是否包含特定的昵称，如果是则抛出异常
                    if (eventContext.getParams() != null) {
                        for (Object param : eventContext.getParams().params()) {
                            if ("禁止修改的昵称".equals(param)) {
                                String errorMsg = "检测到禁止修改的内容，更新操作被中断";
                                System.err.println("异常信息: " + errorMsg);
                                System.err.println("异常时间: " + new Date());
                                System.err.println("异常SQL: " + sql);
                                System.err.println("异常参数: " + param);
                                throw new RuntimeException(errorMsg);
                            }
                        }
                    }
                }

                @Override
                public void onUpdateAfter(DatabaseSessionEventContext eventContext) throws Exception {
                    String sql = eventContext.getSql();
                    System.out.println("onUpdateAfter: " + sql);
                    System.out.println("更新操作成功完成");
                }
            };

            session.setSessionEventListener(updateExceptionListener);

            try {
                UserEntity userToUpdate = session.find(EntitySQL.create(UserEntity.class), "user_normal_001");
                userToUpdate.setNickname("禁止修改的昵称");
                userToUpdate.setUpdateTime(new Date());
                session.update(userToUpdate);
                System.err.println("错误: 禁止修改的更新操作成功（不应该成功）");
                Assert.fail("禁止修改的更新操作应该被中断");
            } catch (RuntimeException e) {
                System.out.println("成功捕获预期异常: " + e.getMessage());
                Assert.assertTrue("异常消息应包含禁止修改信息", e.getMessage().contains("禁止修改"));
            }

            // 测试5: 验证数据确实没有被修改
            System.out.println("\n测试5: 验证数据确实没有被修改");
            UserEntity unchangedUser = session.find(EntitySQL.create(UserEntity.class), "user_normal_001");
            Assert.assertEquals("昵称应该保持不变", "正常用户", unchangedUser.getNickname());
            System.out.println("验证通过: 数据确实没有被修改");

            System.out.println("\n所有异常打断测试完成！");

            return null;
        });
    }
}
