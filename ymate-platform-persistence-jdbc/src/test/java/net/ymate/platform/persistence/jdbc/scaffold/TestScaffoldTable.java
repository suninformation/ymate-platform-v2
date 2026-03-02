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
package net.ymate.platform.persistence.jdbc.scaffold;

import net.ymate.platform.core.persistence.annotation.*;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.support.BaseEntity;

import java.util.Date;

/**
 * 测试脚手架表实体类
 *
 * @author 刘镇 (suninformation@163.com) on 2026/3/1 03:14
 * @since 2.1.4
 */
@Entity("test_scaffold_table")
@Comment("测试脚手架表")
public class TestScaffoldTable extends BaseEntity<TestScaffoldTable, Long> {

    @Id
    @Property(name = "id", autoincrement = true, nullable = false, unsigned = true)
    @Comment("主键")
    private Long id;

    @Property(name = "name", nullable = false, length = 100)
    @Comment("名称")
    private String name;

    @Property(name = "age", nullable = false, unsigned = true, type = Type.FIELD.TINYINT)
    @Default("18")
    @Comment("年龄")
    private Integer age;

    @Property(name = "salary", nullable = false, length = 10, decimals = 2, type = Type.FIELD.NUMBER)
    @Default("0.00")
    @Comment("薪资")
    private java.math.BigDecimal salary;

    @Property(name = "active", nullable = false, type = Type.FIELD.BOOLEAN)
    @Default("TRUE")
    @Comment("是否活跃")
    private Boolean active;

    @Property(name = "create_time", nullable = false, type = Type.FIELD.TIMESTAMP)
    @Default(value = "CURRENT_TIMESTAMP", ignored = true)
    @Comment("创建时间")
    private Date createTime;

    @Property(name = "update_time", type = Type.FIELD.TIMESTAMP)
    @Comment("更新时间")
    private Date updateTime;

    @Property(name = "remark", type = Type.FIELD.TEXT)
    @Comment("备注")
    private String remark;

    @Property(name = "data", type = Type.FIELD.BLOB)
    @Comment("二进制数据")
    private byte[] data;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public java.math.BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(java.math.BigDecimal salary) {
        this.salary = salary;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
