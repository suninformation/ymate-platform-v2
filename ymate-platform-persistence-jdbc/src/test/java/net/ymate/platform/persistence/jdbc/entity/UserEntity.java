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
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.support.BaseEntity;

import java.util.Date;

/**
 * 用户实体类
 *
 * @author 刘镇 (suninformation@163.com) on 2026/3/10 10:00
 * @since 2.1.4
 */
@Entity("sys_user")
@Comment("用户信息表")
@Indexes({
        @Index(name = "idx_username", fields = {"username"}, unique = true),
        @Index(name = "idx_email", fields = {"email"}, unique = true)
})
public class UserEntity extends BaseEntity<UserEntity, String> {

    private static final long serialVersionUID = 1L;

    @Id
    @Property(name = "id", type = Type.FIELD.VARCHAR, length = 32, nullable = false)
    @Comment("用户ID")
    private String id;

    @Property(name = "username", type = Type.FIELD.VARCHAR, length = 50, nullable = false)
    @Comment("用户名")
    private String username;

    @Property(name = "password", type = Type.FIELD.VARCHAR, length = 64, nullable = false)
    @Comment("密码")
    private String password;

    @Property(name = "nickname", type = Type.FIELD.VARCHAR, length = 50, nullable = true)
    @Comment("昵称")
    private String nickname;

    @Property(name = "email", type = Type.FIELD.VARCHAR, length = 100, nullable = true)
    @Comment("邮箱")
    private String email;

    @Property(name = "phone", type = Type.FIELD.VARCHAR, length = 20, nullable = true)
    @Comment("手机号")
    private String phone;

    @Property(name = "avatar", type = Type.FIELD.VARCHAR, length = 255, nullable = true)
    @Comment("头像URL")
    private String avatar;

    @Property(name = "status", type = Type.FIELD.INT, length = 1, nullable = false)
    @Default("1")
    @Comment("状态：0-禁用，1-启用")
    private Integer status;

    @Property(name = "create_time", type = Type.FIELD.TIMESTAMP, nullable = false)
    @Default(value = "CURRENT_TIMESTAMP", ignored = true)
    @Comment("创建时间")
    private Date createTime;

    @Property(name = "update_time", type = Type.FIELD.TIMESTAMP, nullable = true)
    @Comment("更新时间")
    private Date updateTime;

    public UserEntity() {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
