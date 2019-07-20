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
package net.ymate.platform.commons;

import net.ymate.platform.commons.lang.BlurObject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 精确的数学计算工具类
 *
 * @author 刘镇 (suninformation@163.com) on 15/8/18 上午10:55
 */
public class MathCalcHelper {

    public static boolean eq(double x, double y) {
        return eq(new BigDecimal(Double.toString(x)), new BigDecimal(Double.toString(y)));
    }

    public static boolean eq(float x, float y) {
        return eq(new BigDecimal(Float.toString(x)), new BigDecimal(Float.toString(y)));
    }

    public static boolean eq(BigDecimal x, BigDecimal y) {
        return Objects.equals(x, y);
    }

    /**
     * 默认除法运算精度
     */
    private static final int DEFAULT_DIV_SCALE = 10;

    private BigDecimal value;

    private int scale = -1;

    private int roundingMode = -1;

    public static MathCalcHelper bind(double value) {
        return new MathCalcHelper(value);
    }

    public static MathCalcHelper bind(String value) {
        return new MathCalcHelper(value);
    }

    public static MathCalcHelper bind(BigDecimal value) {
        return new MathCalcHelper(value);
    }

    private MathCalcHelper(double value) {
        this.value = new BigDecimal(Double.toString(value));
    }

    private MathCalcHelper(String value) {
        this.value = new BigDecimal(value);
    }

    private MathCalcHelper(BigDecimal value) {
        this.value = value;
    }

    //

    public MathCalcHelper scale(int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("the scale must be a positive integer or zero.");
        }
        this.scale = scale;
        return this;
    }

    public MathCalcHelper roundUp() {
        roundingMode = BigDecimal.ROUND_UP;
        return this;
    }

    public MathCalcHelper roundDown() {
        roundingMode = BigDecimal.ROUND_DOWN;
        return this;
    }

    public MathCalcHelper roundCeiling() {
        roundingMode = BigDecimal.ROUND_CEILING;
        return this;
    }

    public MathCalcHelper roundFloor() {
        roundingMode = BigDecimal.ROUND_FLOOR;
        return this;
    }

    public MathCalcHelper roundHalfUp() {
        roundingMode = BigDecimal.ROUND_HALF_UP;
        return this;
    }

    public MathCalcHelper roundHalfDown() {
        roundingMode = BigDecimal.ROUND_HALF_DOWN;
        return this;
    }

    public MathCalcHelper roundHalfEven() {
        roundingMode = BigDecimal.ROUND_HALF_EVEN;
        return this;
    }

    public MathCalcHelper roundUnnecessary() {
        roundingMode = BigDecimal.ROUND_UNNECESSARY;
        return this;
    }

    //

    public MathCalcHelper add(double value) {
        this.value = this.value.add(new BigDecimal(Double.toString(value)));
        return this;
    }

    public MathCalcHelper add(String value) {
        this.value = this.value.add(new BigDecimal(value));
        return this;
    }

    public MathCalcHelper add(BigDecimal value) {
        this.value = this.value.add(value);
        return this;
    }

    //

    public MathCalcHelper subtract(double value) {
        this.value = this.value.subtract(new BigDecimal(Double.toString(value)));
        return this;
    }

    public MathCalcHelper subtract(String value) {
        this.value = this.value.subtract(new BigDecimal(value));
        return this;
    }

    public MathCalcHelper subtract(BigDecimal value) {
        this.value = this.value.subtract(value);
        return this;
    }

    //

    public MathCalcHelper multiply(double value) {
        this.value = this.value.multiply(new BigDecimal(Double.toString(value)));
        return this;
    }

    public MathCalcHelper multiply(String value) {
        this.value = this.value.multiply(new BigDecimal(value));
        return this;
    }

    public MathCalcHelper multiply(BigDecimal value) {
        this.value = this.value.multiply(value);
        return this;
    }

    //

    public MathCalcHelper divide(double value) {
        return divide(new BigDecimal(Double.toString(value)));
    }

    public MathCalcHelper divide(String value) {
        return divide(new BigDecimal(value));
    }

    public MathCalcHelper divide(BigDecimal value) {
        this.value = this.value.divide(value,
                (scale >= 0 ? scale : DEFAULT_DIV_SCALE),
                (roundingMode >= 0 ? roundingMode : BigDecimal.ROUND_HALF_EVEN));
        return this;
    }

    //

    public MathCalcHelper round() {
        value = value.setScale((scale >= 0 ? scale : DEFAULT_DIV_SCALE),
                (roundingMode >= 0 ? roundingMode : BigDecimal.ROUND_HALF_EVEN));
        return this;
    }

    //

    public BlurObject toBlurObject() {
        return BlurObject.bind(value);
    }
}
