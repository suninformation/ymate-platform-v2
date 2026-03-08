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
package net.ymate.platform.persistence.jdbc.entity;

import net.ymate.platform.core.persistence.annotation.*;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.IEntityPK;
import net.ymate.platform.core.persistence.base.Type;

import java.util.Date;

/**
 * 用户权限关系实体类（多主键）
 *
 * @author 刘镇 (suninformation@163.com) on 2026/3/10 10:00
 * @since 2.1.4
 */
@Entity("sys_user_permission")
@Comment("用户权限关系表")
@Indexes({
        @Index(name = "idx_user_permission", fields = {"user_id", "permission_id"}, unique = true)
})
public class UserPermissionEntity implements IEntity<UserPermissionEntity.UserPermissionPK> {

    private static final long serialVersionUID = 1L;

    @PK
    public static class UserPermissionPK implements IEntityPK {

        private static final long serialVersionUID = 1L;

        @Property(name = "user_id", type = Type.FIELD.VARCHAR, length = 32, nullable = false)
        @Comment("用户ID")
        private String userId;

        @Property(name = "permission_id", type = Type.FIELD.LONG, nullable = false)
        @Comment("权限ID")
        private Long permissionId;

        public UserPermissionPK() {
        }

        public UserPermissionPK(String userId, Long permissionId) {
            this.userId = userId;
            this.permissionId = permissionId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Long getPermissionId() {
            return permissionId;
        }

        public void setPermissionId(Long permissionId) {
            this.permissionId = permissionId;
        }
    }

    @Id
    private UserPermissionPK id;

    @Property(name = "grant_type", type = Type.FIELD.INT, length = 1, nullable = false)
    @Default("1")
    @Comment("授权类型：1-直接授权，2-角色继承")
    private Integer grantType;

    @Property(name = "create_time", type = Type.FIELD.TIMESTAMP, nullable = false)
    @Default(value = "CURRENT_TIMESTAMP", ignored = true)
    @Comment("创建时间")
    private Date createTime;

    public UserPermissionEntity() {
    }

    public UserPermissionEntity(UserPermissionPK id) {
        this.id = id;
    }

    @Override
    public UserPermissionPK getId() {
        return id;
    }

    @Override
    public void setId(UserPermissionPK id) {
        this.id = id;
    }

    public Integer getGrantType() {
        return grantType;
    }

    public void setGrantType(Integer grantType) {
        this.grantType = grantType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
