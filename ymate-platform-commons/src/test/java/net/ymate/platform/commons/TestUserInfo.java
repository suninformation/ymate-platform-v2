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

import net.ymate.platform.commons.annotation.ExportColumn;

import java.util.Date;

/**
 * 测试用的用户信息Bean类
 * <p>
 * 用于测试Excel导入导出功能，包含各种数据类型的字段。
 * </p>
 */
public class TestUserInfo {

    @ExportColumn(value = "用户名", order = 1)
    private String username;

    @ExportColumn(value = "年龄", order = 2)
    private Integer age;

    @ExportColumn(value = "邮箱", order = 3)
    private String email;

    @ExportColumn(value = "创建时间", dateTime = true, pattern = "yyyy-MM-dd HH:mm:ss", order = 4)
    private Date createTime;

    @ExportColumn(value = "性别", dataRange = {"男", "女"}, order = 5)
    private Integer gender;

    @ExportColumn(value = "金额", currency = true, decimals = 2, order = 6)
    private Double amount;

    @ExportColumn(value = "是否激活", order = 7)
    private Boolean active;

    @ExportColumn(value = "备注", excluded = true)
    private String remark;

    public TestUserInfo() {
    }

    public TestUserInfo(String username, Integer age, String email, Date createTime, Integer gender, Double amount, Boolean active) {
        this.username = username;
        this.age = age;
        this.email = email;
        this.createTime = createTime;
        this.gender = gender;
        this.amount = amount;
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "TestUserInfo{" +
                "username='" + username + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", createTime=" + createTime +
                ", gender=" + gender +
                ", amount=" + amount +
                ", active=" + active +
                ", remark='" + remark + '\'' +
                '}';
    }
}
