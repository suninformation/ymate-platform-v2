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

import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.beans.annotation.Inject;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.validation.Validations;
import net.ymate.platform.validation.validate.DefaultGroup;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 验证分组与条件测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026/6/22 01:02
 * @since 2.1.4
 */
@RunWith(net.ymate.platform.test.YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class ValidationGroupConditionTest {

    @Inject
    private Validations validationsOwner;

    // ==================== 分组验证测试 ====================

    /**
     * 测试Create分组 - password必填
     */
    @Test
    public void testCreateGroupPasswordRequired() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "test");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.UserDTO.class, params, ValidationTestDTOs.Create.class);
        Assert.assertTrue("Create分组下password必填", results.containsKey("password"));
    }

    /**
     * 测试Create分组 - id不需要验证
     */
    @Test
    public void testCreateGroupIdNotRequired() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("password", "123456");
        params.put("name", "test");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.UserDTO.class, params, ValidationTestDTOs.Create.class);
        Assert.assertFalse("Create分组下id不需要验证", results.containsKey("id"));
    }

    /**
     * 测试Update分组 - id必填
     */
    @Test
    public void testUpdateGroupIdRequired() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "test");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.UserDTO.class, params, ValidationTestDTOs.Update.class);
        Assert.assertTrue("Update分组下id必填", results.containsKey("id"));
    }

    /**
     * 测试Update分组 - password不需要验证
     */
    @Test
    public void testUpdateGroupPasswordNotRequired() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("id", "123");
        params.put("name", "test");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.UserDTO.class, params, ValidationTestDTOs.Update.class);
        Assert.assertFalse("Update分组下password不需要验证", results.containsKey("password"));
    }

    /**
     * 测试Create和Update分组 - name在两个分组中都必填
     */
    @Test
    public void testNameRequiredInBothGroups() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("password", "123456");

        Map<String, ValidateResult> createResults = validationsOwner.validate(
                ValidationTestDTOs.UserDTO.class, params, ValidationTestDTOs.Create.class);
        Assert.assertTrue("Create分组下name必填", createResults.containsKey("name"));

        Map<String, Object> params2 = new LinkedHashMap<>();
        params2.put("id", "123");
        Map<String, ValidateResult> updateResults = validationsOwner.validate(
                ValidationTestDTOs.UserDTO.class, params2, ValidationTestDTOs.Update.class);
        Assert.assertTrue("Update分组下name必填", updateResults.containsKey("name"));
    }

    /**
     * 测试Default分组 - 未指定分组的注解属于Default分组
     */
    @Test
    public void testDefaultGroup() {
        Map<String, Object> params = new LinkedHashMap<>();

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.SimpleDTO.class, params, DefaultGroup.class);
        Assert.assertTrue("Default分组下name必填", results.containsKey("name"));
    }

    /**
     * 测试不传groups参数 - 默认使用Default分组
     */
    @Test
    public void testNoGroupsParam() {
        Map<String, Object> params = new LinkedHashMap<>();

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.SimpleDTO.class, params);
        Assert.assertTrue("不传groups时默认Default分组", results.containsKey("name"));
    }

    /**
     * 测试Create分组下SimpleDTO的name不验证（name属于Default分组，不属于Create分组）
     */
    @Test
    public void testSimpleDTOWithCreateGroup() {
        Map<String, Object> params = new LinkedHashMap<>();

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.SimpleDTO.class, params, ValidationTestDTOs.Create.class);
        Assert.assertFalse("Create分组下SimpleDTO的name不验证", results.containsKey("name"));
    }

    /**
     * 测试Create分组全部通过
     */
    @Test
    public void testCreateGroupAllPass() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("password", "123456");
        params.put("name", "test");
        params.put("email", "test@example.com");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.UserDTO.class, params, ValidationTestDTOs.Create.class);
        Assert.assertTrue("Create分组全部通过", results.isEmpty());
    }

    /**
     * 测试Update分组全部通过
     */
    @Test
    public void testUpdateGroupAllPass() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("id", "123");
        params.put("name", "test");
        params.put("email", "test@example.com");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.UserDTO.class, params, ValidationTestDTOs.Update.class);
        Assert.assertTrue("Update分组全部通过", results.isEmpty());
    }

    // ==================== 条件验证测试 ====================

    /**
     * 测试FIELD_EQUALS条件 - type为express时address必填
     */
    @Test
    public void testFieldEqualsConditionAddressRequired() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "express");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.OrderDTO.class, params);
        Assert.assertTrue("type为express时address必填", results.containsKey("address"));
    }

    /**
     * 测试FIELD_EQUALS条件 - type不为express时address不需要验证
     */
    @Test
    public void testFieldEqualsConditionAddressNotRequired() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "pickup");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.OrderDTO.class, params);
        Assert.assertFalse("type不为express时address不需要验证", results.containsKey("address"));
    }

    /**
     * 测试FIELD_GT条件 - amount大于10000时approvalCode必填
     */
    @Test
    public void testFieldGtConditionApprovalCodeRequired() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "express");
        params.put("address", "test address");
        params.put("amount", "20000");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.OrderDTO.class, params);
        Assert.assertTrue("amount大于10000时approvalCode必填", results.containsKey("approvalCode"));
    }

    /**
     * 测试FIELD_GT条件 - amount不大于10000时approvalCode不需要验证
     */
    @Test
    public void testFieldGtConditionApprovalCodeNotRequired() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "pickup");
        params.put("amount", "5000");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.OrderDTO.class, params);
        Assert.assertFalse("amount不大于10000时approvalCode不需要验证", results.containsKey("approvalCode"));
    }

    /**
     * 测试FIELD_NOT_EMPTY条件 - email字段存在时验证格式
     */
    @Test
    public void testFieldNotEmptyConditionEmailValidated() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "pickup");
        params.put("email", "invalid-email");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.OrderDTO.class, params);
        Assert.assertTrue("email字段存在时验证格式", results.containsKey("email"));
    }

    /**
     * 测试FIELD_NOT_EMPTY条件 - email字段不存在时不验证
     */
    @Test
    public void testFieldNotEmptyConditionEmailNotValidated() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "pickup");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.OrderDTO.class, params);
        Assert.assertFalse("email字段不存在时不验证", results.containsKey("email"));
    }

    /**
     * 测试FIELD_EMPTY条件 - phone为空时backupPhone必填
     */
    @Test
    public void testFieldEmptyConditionBackupPhoneRequired() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "pickup");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.OrderDTO.class, params);
        Assert.assertTrue("phone为空时backupPhone必填", results.containsKey("backupPhone"));
    }

    /**
     * 测试FIELD_EMPTY条件 - phone不为空时backupPhone不需要验证
     */
    @Test
    public void testFieldEmptyConditionBackupPhoneNotRequired() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "pickup");
        params.put("phone", "13800138000");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.OrderDTO.class, params);
        Assert.assertFalse("phone不为空时backupPhone不需要验证", results.containsKey("backupPhone"));
    }

    // ==================== 分组与条件组合测试 ====================

    /**
     * 测试分组与条件组合 - Create分组下password必填（不受条件影响）
     */
    @Test
    public void testCombinedCreateGroupPasswordRequired() {
        Map<String, Object> params = new LinkedHashMap<>();

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.CombinedDTO.class, params, ValidationTestDTOs.Create.class);
        Assert.assertTrue("Create分组下password必填", results.containsKey("password"));
    }

    /**
     * 测试分组与条件组合 - Update分组下password不需要验证
     */
    @Test
    public void testCombinedUpdateGroupPasswordNotRequired() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("id", "123");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.CombinedDTO.class, params, ValidationTestDTOs.Update.class);
        Assert.assertFalse("Update分组下password不需要验证", results.containsKey("password"));
    }

    /**
     * 测试分组与条件组合 - Create分组下name存在时VLength验证password长度
     */
    @Test
    public void testCombinedCreateGroupLengthCondition() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "test");
        params.put("password", "123");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.CombinedDTO.class, params, ValidationTestDTOs.Create.class);
        Assert.assertTrue("Create分组下name存在时VLength验证password长度", results.containsKey("password"));
    }

    /**
     * 测试分组与条件组合 - Create分组下name不存在时VLength不验证password长度
     */
    @Test
    public void testCombinedCreateGroupLengthNoCondition() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("password", "123");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.CombinedDTO.class, params, ValidationTestDTOs.Create.class);
        // password有值，VRequired通过；name不存在，VLength条件不满足不验证
        Assert.assertTrue("Create分组下name不存在时VLength不验证password长度", results.isEmpty());
    }

    // ==================== 兼容性测试 ====================

    /**
     * 测试兼容性 - 无groups/condition属性的注解在Default分组下正常工作
     */
    @Test
    public void testCompatibilityDefaultGroup() {
        Map<String, Object> params = new LinkedHashMap<>();

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.SimpleDTO.class, params, DefaultGroup.class);
        Assert.assertTrue("无groups/condition的注解在Default分组下正常工作", results.containsKey("name"));
    }

    /**
     * 测试兼容性 - 不传groups参数时原有行为不变
     */
    @Test
    public void testCompatibilityNoGroupsParam() {
        Map<String, Object> params = new LinkedHashMap<>();

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.SimpleDTO.class, params);
        Assert.assertTrue("不传groups参数时原有行为不变", results.containsKey("name"));
    }

    /**
     * 测试兼容性 - name长度超出限制
     */
    @Test
    public void testCompatibilityLengthValidation() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "a_very_long_name_that_exceeds_max_length");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.SimpleDTO.class, params);
        Assert.assertTrue("name长度超出限制时验证失败", results.containsKey("name"));
    }

    /**
     * 测试兼容性 - 全部通过
     */
    @Test
    public void testCompatibilityAllPass() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "test");

        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.SimpleDTO.class, params);
        Assert.assertTrue("全部通过时结果为空", results.isEmpty());
    }

    // ==================== @ValidateGroups 注解测试 ====================

    /**
     * 测试@ValidateGroups注解 - DTO类声明Create分组，不传groups参数时自动使用Create分组
     */
    @Test
    public void testValidateGroupsOnClass() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "test");

        // 不传groups参数，应从CreateUserDTO类上的@ValidateGroups(Create.class)自动获取Create分组
        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.CreateUserDTO.class, params);
        // Create分组下password必填
        Assert.assertTrue("@ValidateGroups注解声明Create分组，password必填", results.containsKey("password"));
        // Update分组下id不需要验证
        Assert.assertFalse("@ValidateGroups注解声明Create分组，id不需要验证", results.containsKey("id"));
    }

    /**
     * 测试@ValidateGroups注解 - 显式传入groups参数优先于类上的@ValidateGroups
     */
    @Test
    public void testValidateGroupsExplicitOverride() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "test");

        // 显式传入Update分组，应覆盖类上的@ValidateGroups(Create.class)
        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.CreateUserDTO.class, params, ValidationTestDTOs.Update.class);
        // Update分组下id必填
        Assert.assertTrue("显式传入Update分组，id必填", results.containsKey("id"));
        // Update分组下password不需要验证
        Assert.assertFalse("显式传入Update分组，password不需要验证", results.containsKey("password"));
    }

    /**
     * 测试@ValidateGroups注解 - 无@ValidateGroups注解的类默认使用DefaultGroup
     */
    @Test
    public void testValidateGroupsDefaultWhenNoAnnotation() {
        Map<String, Object> params = new LinkedHashMap<>();

        // UserDTO没有@ValidateGroups注解，不传groups参数时默认使用DefaultGroup
        Map<String, ValidateResult> results = validationsOwner.validate(
                ValidationTestDTOs.UserDTO.class, params);
        // DefaultGroup分组下，无分组注解的字段不会被验证
        // UserDTO的所有字段都指定了Create或Update分组，没有DefaultGroup分组的字段
        Assert.assertTrue("无@ValidateGroups注解的类默认DefaultGroup，无匹配字段", results.isEmpty());
    }
}
