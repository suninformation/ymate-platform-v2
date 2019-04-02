package net.ymate.platform.validation.validate;

import java.lang.annotation.*;

/**
 * 金额验证器注解
 *
 * @author Xuanzi An
 *
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VMoney {

    /**
     * @return 自定义验证消息
     */
    String msg() default "";
}