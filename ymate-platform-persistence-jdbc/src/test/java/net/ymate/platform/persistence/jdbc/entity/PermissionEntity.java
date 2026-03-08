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
 * 权限实体类
 *
 * @author 刘镇 (suninformation@163.com) on 2026/3/10 10:00
 * @since 2.1.4
 */
@Entity("sys_permission")
@Comment("权限信息表")
@Indexes({
        @Index(name = "idx_permission_code", fields = {"permission_code"}, unique = true)
})
public class PermissionEntity extends BaseEntity<PermissionEntity, Long> {

    private static final long serialVersionUID = 1L;

    @Id
    @Property(name = "id", type = Type.FIELD.LONG, autoincrement = true, nullable = false)
    @Comment("权限ID")
    private Long id;

    @Property(name = "permission_code", type = Type.FIELD.VARCHAR, length = 50, nullable = false)
    @Comment("权限编码")
    private String permissionCode;

    @Property(name = "permission_name", type = Type.FIELD.VARCHAR, length = 100, nullable = false)
    @Comment("权限名称")
    private String permissionName;

    @Property(name = "parent_id", type = Type.FIELD.VARCHAR, length = 32, nullable = true)
    @Comment("父权限ID")
    private String parentId;

    @Property(name = "permission_type", type = Type.FIELD.INT, length = 1, nullable = false)
    @Default("1")
    @Comment("权限类型：1-菜单，2-按钮，3-接口")
    private Integer permissionType;

    @Property(name = "resource_url", type = Type.FIELD.VARCHAR, length = 255, nullable = true)
    @Comment("资源路径")
    private String resourceUrl;

    @Property(name = "sort_order", type = Type.FIELD.INT, length = 4, nullable = false)
    @Default("0")
    @Comment("排序号")
    private Integer sortOrder;

    @Property(name = "icon", type = Type.FIELD.VARCHAR, length = 100, nullable = true)
    @Comment("图标")
    private String icon;

    @Property(name = "status", type = Type.FIELD.INT, length = 1, nullable = false)
    @Default("1")
    @Comment("状态：0-禁用，1-启用")
    private Integer status;

    @Property(name = "remark", type = Type.FIELD.VARCHAR, length = 500, nullable = true)
    @Comment("备注")
    private String remark;

    @Property(name = "create_time", type = Type.FIELD.TIMESTAMP, nullable = false)
    @Default(value = "CURRENT_TIMESTAMP", ignored = true)
    @Comment("创建时间")
    private Date createTime;

    @Property(name = "update_time", type = Type.FIELD.TIMESTAMP, nullable = true)
    @Comment("更新时间")
    private Date updateTime;

    public PermissionEntity() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(Integer permissionType) {
        this.permissionType = permissionType;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
