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
package net.ymate.platform.validation.test;

import net.ymate.platform.validation.annotation.VCondition;
import net.ymate.platform.validation.annotation.ValidateGroups;
import net.ymate.platform.validation.validate.VEmail;
import net.ymate.platform.validation.validate.VLength;
import net.ymate.platform.validation.validate.VRequired;

/**
 * 验证分组与条件测试DTO
 *
 * @author 刘镇 (suninformation@163.com) on 2026/6/22 01:02
 * @since 2.1.4
 */
public class ValidationTestDTOs {

    /**
     * 创建操作分组
     */
    public interface Create {
    }

    /**
     * 更新操作分组
     */
    public interface Update {
    }

    /**
     * 分组验证DTO - 用户信息
     */
    public static class UserDTO {

        @VRequired(groups = Create.class)
        private String password;

        @VRequired(groups = Update.class)
        private String id;

        @VRequired(groups = {Create.class, Update.class})
        @VLength(min = 2, max = 50, groups = {Create.class, Update.class})
        private String name;

        @VEmail(groups = {Create.class, Update.class})
        private String email;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * 条件验证DTO - 订单信息
     */
    public static class OrderDTO {

        @VRequired
        private String type;

        @VRequired(condition = @VCondition(type = VCondition.Type.FIELD_EQUALS, field = "type", expectedValue = "express"))
        private String address;

        @VRequired(condition = @VCondition(type = VCondition.Type.FIELD_GT, field = "amount", expectedValue = "10000"))
        @VLength(min = 5, max = 200, condition = @VCondition(type = VCondition.Type.FIELD_GT, field = "amount", expectedValue = "10000"))
        private String approvalCode;

        @VEmail(condition = @VCondition(type = VCondition.Type.FIELD_NOT_EMPTY, field = "email"))
        private String email;

        @VRequired(condition = @VCondition(type = VCondition.Type.FIELD_EMPTY, field = "phone"))
        private String backupPhone;

        private String amount;

        private String phone;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getApprovalCode() {
            return approvalCode;
        }

        public void setApprovalCode(String approvalCode) {
            this.approvalCode = approvalCode;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getBackupPhone() {
            return backupPhone;
        }

        public void setBackupPhone(String backupPhone) {
            this.backupPhone = backupPhone;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    /**
     * 分组与条件组合验证DTO
     */
    public static class CombinedDTO {

        @VRequired(groups = Create.class)
        @VLength(min = 6, max = 20, groups = {Create.class, Update.class}, condition = @VCondition(type = VCondition.Type.FIELD_NOT_EMPTY, field = "name"))
        private String password;

        @VRequired(groups = Update.class)
        private String id;

        private String name;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 无分组/条件属性的验证DTO（兼容性测试）
     */
    public static class SimpleDTO {

        @VRequired
        @VLength(min = 1, max = 10)
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 使用@ValidateGroups声明Create分组的DTO
     */
    @ValidateGroups(Create.class)
    public static class CreateUserDTO {

        @VRequired(groups = Create.class)
        private String password;

        @VRequired(groups = Update.class)
        private String id;

        private String name;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
