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
package net.ymate.platform.persistence.jdbc.query;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.AbstractFunction;
import net.ymate.platform.core.persistence.IFunction;
import net.ymate.platform.core.persistence.Params;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 数据库函数库(尝试!!! 暂未考虑不同数据库间的兼容问题)
 *
 * @author 刘镇 (suninformation@163.com) on 17/6/22 上午10:50
 */
@Ignored
public interface Func {

    Math math = new Math() {
    };

    Strings strings = new Strings() {
    };

    Aggregate aggregate = new Aggregate() {
    };

    DateTime dateTime = new DateTime() {
    };

    ControlFlow controlFlow = new ControlFlow() {
    };

    Operators operators = new Operators() {
    };

    // ------

    /**
     * 构建自定义函数
     *
     * @param funcName 函数名称
     * @return 返回函数接口对象
     */
    static AbstractFunction create(String funcName) {
        return new AbstractFunction(funcName) {
            @Override
            public void onBuild() {
            }
        };
    }

    /**
     * 构建自定义函数(无名)
     *
     * @return 返回函数接口对象
     */
    static AbstractFunction create() {
        return new AbstractFunction() {
            @Override
            public void onBuild() {
            }
        };
    }

    /**
     * 操作符函数
     *
     * @param opt   操作符号
     * @param param 参数
     * @return 返回函数接口对象
     */
    static IFunction operate(String opt, String param) {
        return new AbstractFunction() {
            @Override
            public void onBuild() {
                operate(opt, param);
            }
        };
    }

    /**
     * 操作符函数
     *
     * @param paramOne 参数1
     * @param opt      操作符号
     * @param paramTwo 参数2
     * @return 返回函数接口对象
     */
    static IFunction operate(String paramOne, String opt, String paramTwo) {
        return new AbstractFunction() {
            @Override
            public void onBuild() {
                field(paramOne).operate(opt, paramTwo);
            }
        };
    }

    /**
     * Mathematical Functions
     */
    @Ignored
    interface Math {

        // ------ 返回 X 的绝对值

        default IFunction ABS(String x) {
            return create("ABS").field(x);
        }

        default IFunction ABS(IFunction x) {
            return ABS(x.build()).param(x.params());
        }

        // ------ 返回 X 反余弦, 即, 余弦是X的值

        default IFunction ACOS(String x) {
            return create("ACOS").field(x);
        }

        default IFunction ACOS(IFunction x) {
            return ACOS(x.build()).param(x.params());
        }

        // ------ 返回 X 的反正弦, 即, 正弦为X的值

        default IFunction ASIN(String x) {
            return create("ASIN").field(x);
        }

        default IFunction ASIN(IFunction x) {
            return ASIN(x.build()).param(x.params());
        }

        // ------ 返回 X 的反正切，即，正切为X的值。或两个变量 X 及 Y 的反正切

        default IFunction ATAN(String x) {
            return create("ATAN").field(x);
        }

        default IFunction ATAN(IFunction x) {
            return ATAN(x.build()).param(x.params());
        }

        default IFunction ATAN(String y, String x) {
            return create("ATAN").field(y).separator().field(x);
        }

        default IFunction ATAN(IFunction y, IFunction x) {
            return ATAN(y.build(), x.build()).param(y.params()).param(x.params());
        }

        // ------ 返回不小于 X 的最小整数值

        default IFunction CEILING(String x) {
            return create("CEILING").field(x);
        }

        default IFunction CEILING(IFunction x) {
            return CEILING(x.build()).param(x.params());
        }

        // ------ 进制转换

        default IFunction CONV(String x, int fromBase, int toBase) {
            return create("CONV").field(x).separator().field(fromBase).separator().field(toBase);
        }

        default IFunction CONV(IFunction x, int fromBase, int toBase) {
            return CONV(x.build(), fromBase, toBase).param(x.params());
        }

        // ------ 返回 X 的余弦，其中 X 在弧度上已知

        default IFunction COS(String x) {
            return create("COS").field(x);
        }

        default IFunction COS(IFunction x) {
            return COS(x.build()).param(x.params());
        }

        // ------ 返回 X 的余切

        default IFunction COT(String x) {
            return create("COT").field(x);
        }

        default IFunction COT(IFunction x) {
            return COT(x.build()).param(x.params());
        }

        // ------ 计算循环冗余码校验值并返回一个32比特无符号值

        default IFunction CRC32(String expr) {
            return create("CRC32").field(expr);
        }

        default IFunction CRC32(IFunction expr) {
            return CRC32(expr.build()).param(expr.params());
        }

        // ------ 返回参数 X, 该参数由弧度被转化为度

        default IFunction DEGREES(String x) {
            return create("DEGREES").field(x);
        }

        default IFunction DEGREES(IFunction x) {
            return DEGREES(x.build()).param(x.params());
        }

        // ------ 返回e的X乘方后的值(自然对数的底)

        default IFunction EXP(String x) {
            return create("EXP").field(x);
        }

        default IFunction EXP(IFunction x) {
            return EXP(x.build()).param(x.params());
        }

        // ----- 返回不大于X的最大整数值

        default IFunction FLOOR(String x) {
            return create("FLOOR").field(x);
        }

        default IFunction FLOOR(IFunction x) {
            return FLOOR(x.build()).param(x.params());
        }

        // ------ 返回 X 的自然对数,即, X 相对于基数e 的对数

        default IFunction LN(String x) {
            return create("LN").field(x);
        }

        default IFunction LN(IFunction x) {
            return LN(x.build()).param(x.params());
        }

        // ------ 若用一个参数调用，这个函数就会返回X 的自然对数。若用两个参数进行调用，这个函数会返回X 对于任意基数B 的对数。

        default IFunction LOG(String x) {
            return create("LOG").field(x);
        }

        default IFunction LOG(IFunction x) {
            return LOG(x.build()).param(x.params());
        }

        default IFunction LOG(String b, String x) {
            return create("LOG").field(b).separator().field(x);
        }

        default IFunction LOG(IFunction b, IFunction x) {
            return LOG(b.build(), x.build()).param(b.params()).param(x.params());
        }

        // ------

        default IFunction LOG10(String x) {
            return create("LOG10").field(x);
        }

        default IFunction LOG10(IFunction x) {
            return LOG10(x.build()).param(x.params());
        }

        // ------

        default IFunction LOG2(String x) {
            return create("LOG2").field(x);
        }

        default IFunction LOG2(IFunction x) {
            return LOG2(x.build()).param(x.params());
        }

        // ------ 模操作。返回N 被 M除后的余数。

        default IFunction MOD(String n, String m) {
            return create("MOD").field(n).separator().field(m);
        }

        default IFunction MOD(IFunction n, IFunction m) {
            return MOD(n.build(), m.build()).param(n.params()).param(m.params());
        }

        // ------ 返回 ϖ (pi)的值。默认的显示小数位数是7位

        default IFunction PI() {
            return create("PI");
        }

        // ------ 返回X 的Y乘方的结果值。

        default IFunction POW(String y, String x) {
            return create("POW").field(y).separator().field(x);
        }

        default IFunction POW(IFunction y, IFunction x) {
            return POW(y.build(), x.build()).param(y.params()).param(x.params());
        }

        // ------

        default IFunction POWER(String y, String x) {
            return create("POWER").field(y).separator().field(x);
        }

        default IFunction POWER(IFunction y, IFunction x) {
            return POWER(y.build(), x.build()).param(y.params()).param(x.params());
        }

        // ------ 返回由度转化为弧度的参数 X, (注意 ϖ 弧度等于180度）。

        default IFunction RADIANS(String x) {
            return create("RADIANS").field(x);
        }

        default IFunction RADIANS(IFunction x) {
            return RADIANS(x.build()).param(x.params());
        }

        // ------ 返回一个随机浮点值 v ，范围在 0 到1 之间 (即, 其范围为 0 ≤ v ≤ 1.0)。若已指定一个整数参数 N ，则它被用作种子值，用来产生重复序列。

        default IFunction RAND() {
            return create("RAND");
        }

        default IFunction RAND(String n) {
            return create("RAND").field(n);
        }

        default IFunction RAND(IFunction n) {
            return RAND(n.build()).param(n.params());
        }

        // ------ 返回参数X, 其值接近于最近似的整数。在有两个参数的情况下，返回 X ，其值保留到小数点后D位，而第D位的保留方式为四舍五入。若要接保留X值小数点左边的D 位，可将 D 设为负值。

        default IFunction ROUND(String x) {
            return create("ROUND").field(x);
        }

        default IFunction ROUND(IFunction x) {
            return ROUND(x.build()).param(x.params());
        }

        default IFunction ROUND(String x, Number d) {
            return create("ROUND").field(x).separator().field(d);
        }

        default IFunction ROUND(IFunction x, Number d) {
            return ROUND(x.build(), d).param(x.params());
        }

        // ------ 返回参数作为-1、 0或1的符号，该符号取决于X 的值为负、零或正。

        default IFunction SIGN(String n) {
            return create("SIGN").field(n);
        }

        default IFunction SIGN(IFunction n) {
            return SIGN(n.build()).param(n.params());
        }

        // ------ 返回X 正弦，其中 X 在弧度中被给定。

        default IFunction SIN(String x) {
            return create("SIN").field(x);
        }

        default IFunction SIN(IFunction x) {
            return SIN(x.build()).param(x.params());
        }

        // ------ 返回非负数X 的二次方根。

        default IFunction SQRT(String x) {
            return create("SQRT").field(x);
        }

        default IFunction SQRT(IFunction x) {
            return SQRT(x.build()).param(x.params());
        }

        // ------ 返回X 的正切，其中X 在弧度中被给定。

        default IFunction TAN(String x) {
            return create("TAN").field(x);
        }

        default IFunction TAN(IFunction x) {
            return TAN(x.build()).param(x.params());
        }

        // ------ 返回被舍去至小数点后D位的数字X。若D 的值为 0, 则结果不带有小数点或不带有小数部分。可以将D设为负数,若要截去(归零) X小数点左起第D位开始后面所有低位的值

        default IFunction TRUNCATE(String x) {
            return create("TRUNCATE").field(x);
        }

        default IFunction TRUNCATE(IFunction x) {
            return TRUNCATE(x.build()).param(x.params());
        }

        default IFunction TRUNCATE(String x, Number d) {
            return create("TRUNCATE").field(x).separator().field(d);
        }

        default IFunction TRUNCATE(IFunction x, Number d) {
            return TRUNCATE(x.build(), d).param(x.params());
        }
    }

    /**
     * String Functions
     */
    @Ignored
    interface Strings {

        // ------ 返回值为字符串str 的最左字符的数值。假如str为空字符串，则返回值为 0 。假如str 为NULL，则返回值为 NULL。 ASCII()用于带有从 0到255的数值的字符。

        default IFunction ASCII(String str) {
            return create("ASCII").field(str);
        }

        default IFunction ASCII(IFunction str) {
            return ASCII(str.build()).param(str.params());
        }

        // ------ 返回值为N的二进制值的字符串表示，其中  N 为一个longlong (BIGINT) 数字。这等同于 CONV(N,10,2)。假如N 为NULL，则返回值为 NULL。

        default IFunction BIN(String str) {
            return create("BIN").field(str);
        }

        default IFunction BIN(IFunction str) {
            return BIN(str.build()).param(str.params());
        }

        // ------ 返回值为二进制的字符串str 长度。

        default IFunction BIT_LENGTH(String str) {
            return create("BIT_LENGTH").field(str);
        }

        default IFunction BIT_LENGTH(IFunction str) {
            return BIT_LENGTH(str.build()).param(str.params());
        }

        // ------ 将每个参数N理解为一个整数，其返回值为一个包含这些整数的代码值所给出的字符的字符串。NULL值被省略。

        default IFunction CHAR(String... n) {
            return create("CHAR").field(n);
        }

        default IFunction CHAR(IFunction... n) {
            Params params = Params.create();
            List<String> fields = new ArrayList<>();
            Arrays.stream(n).forEach(function -> {
                fields.add(function.build());
                params.add(function.params());
            });
            return CHAR(fields.toArray(new String[0])).param(params);
        }

        // ------ 返回值为字符串str 的长度，长度的单位为字符。一个多字节字符算作一个单字符。对于一个包含五个二字节字符集, LENGTH()返回值为 10, 而CHAR_LENGTH()的返回值为5。

        default IFunction CHAR_LENGTH(String str) {
            return create("CHAR_LENGTH").field(str);
        }

        default IFunction CHAR_LENGTH(IFunction str) {
            return CHAR_LENGTH(str.build()).param(str.params());
        }

        // ------

        default IFunction CHARACTER_LENGTH(String str) {
            return create("CHARACTER_LENGTH").field(str);
        }

        default IFunction CHARACTER_LENGTH(IFunction str) {
            return CHARACTER_LENGTH(str.build()).param(str.params());
        }

        // ------ 返回结果为连接参数产生的字符串。

        default IFunction CONCAT(String... n) {
            return create("CONCAT").field(n);
        }

        default IFunction CONCAT(IFunction... n) {
            Params params = Params.create();
            List<String> fields = new ArrayList<>();
            Arrays.stream(n).forEach(function -> {
                fields.add(function.build());
                params.add(function.params());
            });
            return CONCAT(fields.toArray(new String[0])).param(params);
        }

        // ------

        default IFunction CONCAT_WS(String separator, String... n) {
            return create("CONCAT_WS").field(separator).separator().field(n);
        }

        default IFunction CONCAT_WS(String separator, IFunction... n) {
            Params params = Params.create();
            List<String> fields = new ArrayList<>();
            Arrays.stream(n).forEach(function -> {
                fields.add(function.build());
                params.add(function.params());
            });
            return CONCAT_WS(separator, fields.toArray(new String[0])).param(params);
        }

        // ------ 若N = 1，则返回值为  str1 ，若N = 2，则返回值为 str2 ，以此类推。   若N 小于1或大于参数的数目，则返回值为 NULL 。 ELT() 是  FIELD()的补数。

        default IFunction ELT(String n, String... strs) {
            return create("ELT").field(n).separator().field(strs);
        }

        default IFunction ELT(IFunction n, IFunction... strs) {
            Params params = Params.create();
            List<String> fields = new ArrayList<>();
            Arrays.stream(strs).forEach(function -> {
                fields.add(function.build());
                params.add(function.params());
            });
            return ELT(n.build(), fields.toArray(new String[0])).param(params);
        }

        // ------ 返回值为str1, str2, str3,……列表中的str 指数。在找不到str 的情况下，返回值为 0 。

        default IFunction FIELD(String str, String... n) {
            return create("FIELD").field(str).separator().field(n);
        }

        default IFunction FIELD(IFunction str, IFunction... n) {
            Params params = Params.create();
            List<String> fields = new ArrayList<>();
            Arrays.stream(n).forEach(function -> {
                fields.add(function.build());
                params.add(function.params());
            });
            return FIELD(str.build(), fields.toArray(new String[0])).param(params);
        }

        // ------ 假如字符串str 在由N 子链组成的字符串列表strlist 中， 则返回值的范围在 1 到 N 之间 。一个字符串列表就是一个由一些被‘,’符号分开的自链组成的字符串。

        default IFunction FIND_IN_SET(String x, String strlist) {
            return create("FIND_IN_SET").field(x).separator().field(strlist);
        }

        default IFunction FIND_IN_SET(IFunction x, IFunction strlist) {
            return FIND_IN_SET(x.build(), strlist.build()).param(x.params()).param(strlist.params());
        }

        // ------

        default IFunction FORMAT(String x, Number d) {
            return create("FORMAT").field(x).separator().field(d);
        }

        default IFunction FORMAT(IFunction x, Number d) {
            return FORMAT(x.build(), d).param(x.params());
        }

        default IFunction FORMAT(String x, Number d, String locale) {
            return create("FORMAT").field(x).separator().field(d).separator().field(locale);
        }

        default IFunction FORMAT(IFunction x, Number d, String locale) {
            return FORMAT(x.build(), d, locale).param(x.params());
        }

        // ------

        default IFunction HEX(String str) {
            return create("HEX").field(str);
        }

        default IFunction HEX(IFunction str) {
            return HEX(str.build()).param(str.params());
        }

        // ------

        default IFunction FROM_BASE64(String str) {
            return create("FROM_BASE64").field(str);
        }

        default IFunction FROM_BASE64(IFunction str) {
            return FROM_BASE64(str.build()).param(str.params());
        }

        // ------

        default IFunction TO_BASE64(String str) {
            return create("TO_BASE64").field(str);
        }

        default IFunction TO_BASE64(IFunction str) {
            return TO_BASE64(str.build()).param(str.params());
        }

        // ------ 返回字符串 str, 其子字符串起始于 pos 位置和长期被字符串 newstr取代的len 字符。  如果pos 超过字符串长度，则返回值为原始字符串。 假如len的长度大于其它字符串的长度，则从位置pos开始替换。若任何一个参数为null，则返回值为NULL。

        default IFunction INSERT(String str, Number pos, Number len, String newstr) {
            return create("INSERT").fieldWS(str, pos, len, newstr);
        }

        default IFunction INSERT(IFunction str, Number pos, Number len, IFunction newstr) {
            return INSERT(str.build(), pos, len, newstr.build()).param(str.params()).param(newstr.params());
        }

        // ------ 返回字符串 str 中子字符串的第一个出现位置。

        default IFunction INSTR(String str, String substr) {
            return create("INSTR").fieldWS(str, substr);
        }

        default IFunction INSTR(IFunction str, IFunction substr) {
            return INSTR(str.build(), substr.build()).param(str.params()).param(substr.params());
        }

        // ------ 返回从字符串str 开始的len 最左字符。

        default IFunction LEFT(String str, Number len) {
            return create("LEFT").fieldWS(str, len);
        }

        default IFunction LEFT(IFunction str, Number len) {
            return LEFT(str.build(), len).param(str.params());
        }

        // ------ 返回值为字符串str 的长度，单位为字节。一个多字节字符算作多字节。这意味着 对于一个包含5个2字节字符的字符串， LENGTH() 的返回值为 10, 而 CHAR_LENGTH()的返回值则为5。

        default IFunction LENGTH(String str) {
            return create("LENGTH").field(str);
        }

        default IFunction LENGTH(IFunction str) {
            return LENGTH(str.build()).param(str.params());
        }

        // ------ 读取文件并将这一文件按照字符串的格式返回。 文件的位置必须在服务器上,你必须为文件制定路径全名，而且你还必须拥有FILE 特许权。文件必须可读取，文件容量必须小于 max_allowed_packet字节。

        default IFunction LOAD_FILE(String str) {
            return create("LOAD_FILE").field(str);
        }

        default IFunction LOAD_FILE(IFunction str) {
            return LOAD_FILE(str.build()).param(str.params());
        }

        // ------ 第一个语法返回字符串 str中子字符串substr的第一个出现位置。第二个语法返回字符串 str中子字符串substr的第一个出现位置, 起始位置在pos。如若substr 不在str中，则返回值为0。

        default IFunction LOCATE(String substr, String str) {
            return create("LOCATE").fieldWS(substr, str);
        }

        default IFunction LOCATE(IFunction substr, IFunction str) {
            return LOCATE(substr.build(), str.build()).param(substr.params()).param(str.params());
        }

        default IFunction LOCATE(String substr, String str, String pos) {
            return create("LOCATE").fieldWS(substr, str, pos);
        }

        default IFunction LOCATE(IFunction substr, IFunction str, String pos) {
            return LOCATE(substr.build(), str.build(), pos).param(substr.params()).param(str.params());
        }

        // ------ 返回字符串 str 以及所有根据最新的字符集映射表变为小写字母的字符

        default IFunction LOWER(String str) {
            return create("LOWER").field(str);
        }

        default IFunction LOWER(IFunction str) {
            return LOWER(str.build()).param(str.params());
        }

        // ------ 返回字符串 str, 其左边由字符串padstr 填补到len 字符长度。假如str 的长度大于len, 则返回值被缩短至 len 字符。

        default IFunction LPAD(String str, Number len, String padstr) {
            return create("LPAD").fieldWS(str, len, padstr);
        }

        default IFunction LPAD(IFunction str, Number len, IFunction padstr) {
            return LPAD(str.build(), len, padstr.build()).param(str.params()).param(padstr.params());
        }

        // ------ 返回字符串 str ，其引导空格字符被删除

        default IFunction LTRIM(String str) {
            return create("LTRIM").field(str);
        }

        default IFunction LTRIM(IFunction str) {
            return LTRIM(str.build()).param(str.params());
        }

        // ------ 返回一个 N的八进制值的字符串表示，其中 N 是一个longlong (BIGINT)数。这等同于CONV(N,10,8)。若N 为 NULL ，则返回值为NULL。

        default IFunction OCT(String str) {
            return create("OCT").field(str);
        }

        default IFunction OCT(IFunction str) {
            return OCT(str.build()).param(str.params());
        }

        // ------ 若字符串str 的最左字符是一个多字节字符，则返回该字符的代码

        default IFunction ORD(String str) {
            return create("ORD").field(str);
        }

        default IFunction ORD(IFunction str) {
            return ORD(str.build()).param(str.params());
        }

        // ------ 引证一个字符串，由此产生一个在SQL语句中可用作完全转义数据值的结果。

        default IFunction QUOTE(String str) {
            return create("QUOTE").field(str);
        }

        default IFunction QUOTE(IFunction str) {
            return QUOTE(str.build()).param(str.params());
        }

        // ------ 返回一个由重复的字符串str 组成的字符串，字符串str的数目等于count 。 若 count <= 0,则返回一个空字符串。若str 或 count 为 NULL，则返回 NULL 。

        default IFunction REPEAT(String str, Number count) {
            return create("REPEAT").fieldWS(str, count);
        }

        default IFunction REPEAT(IFunction str, Number count) {
            return REPEAT(str.build(), count).param(str.params());
        }

        // ------ 返回字符串str 以及所有被字符串to_str替代的字符串from_str 。

        default IFunction REPLACE(String str, String fromStr, String toStr) {
            return create("REPLACE").fieldWS(str, fromStr, toStr);
        }

        default IFunction REPLACE(IFunction str, IFunction fromStr, IFunction toStr) {
            return REPLACE(str.build(), fromStr.build(), toStr.build()).param(str.params()).param(fromStr.params()).param(toStr.params());
        }

        // ------ 返回字符串 str ，顺序和字符顺序相反。

        default IFunction REVERSE(String str) {
            return create("REVERSE").field(str);
        }

        default IFunction REVERSE(IFunction str) {
            return REVERSE(str.build()).param(str.params());
        }

        // ------ 从字符串str 开始，返回最右len 字符。

        default IFunction RIGHT(String str, Number len) {
            return create("RIGHT").fieldWS(str, len);
        }

        default IFunction RIGHT(IFunction str, Number len) {
            return RIGHT(str.build(), len).param(str.params());
        }

        // ------ 返回字符串str, 其右边被字符串 padstr填补至len 字符长度。假如字符串str 的长度大于 len,则返回值被缩短到与 len 字符相同长度。

        default IFunction RPAD(String str, Number len, String padstr) {
            return create("RPAD").fieldWS(str, len, padstr);
        }

        default IFunction RPAD(IFunction str, Number len, IFunction padstr) {
            return RPAD(str.build(), len, padstr.build()).param(str.params()).param(padstr.params());
        }

        // ------ 返回字符串 str ，结尾空格字符被删去

        default IFunction RTRIM(String str) {
            return create("RTRIM").field(str);
        }

        default IFunction RTRIM(IFunction str) {
            return RTRIM(str.build()).param(str.params());
        }

        // ------ 从str返回一个soundex字符串。

        default IFunction SOUNDEX(String str) {
            return create("SOUNDEX").field(str);
        }

        default IFunction SOUNDEX(IFunction str) {
            return SOUNDEX(str.build()).param(str.params());
        }

        // ------ 返回一个由N 间隔符号组成的字符串。

        default IFunction SPACE(Number n) {
            return create("SPACE").field(n);
        }

        // ------ 若所有的字符串均相同，则返回STRCMP()，若根据当前分类次序，第一个参数小于第二个，则返回  -1，其它情况返回 1

        default IFunction STRCMP(String expr1, String expr2) {
            return create("STRCMP").fieldWS(expr1, expr2);
        }

        default IFunction STRCMP(IFunction expr1, IFunction expr2) {
            return STRCMP(expr1.build(), expr2.build()).param(expr1.params()).param(expr2.params());
        }

        // ------ 不带有len 参数的格式从字符串str返回一个子字符串，起始于位置 pos。带有len参数的格式从字符串str返回一个长度同len字符相同的子字符串，起始于位置 pos。

        default IFunction SUBSTRING(String str, Number pos) {
            return create("SUBSTRING").fieldWS(str, pos);
        }

        default IFunction SUBSTRING(IFunction str, Number pos) {
            return SUBSTRING(str.build(), pos).param(str.params());
        }

        default IFunction SUBSTRING(String str, Number pos, Number len) {
            return create("SUBSTRING").fieldWS(str, pos, len);
        }

        default IFunction SUBSTRING(IFunction str, Number pos, Number len) {
            return SUBSTRING(str.build(), pos, len).param(str.params());
        }

        // ------ 在定界符 delim 以及count 出现前，从字符串str返回自字符串。若count为正值,则返回最终定界符(从左边开始)左边的一切内容。若count为负值，则返回定界符（从右边开始）右边的一切内容。

        default IFunction SUBSTRING_INDEX(String str, String delim, Number count) {
            return create("SUBSTRING_INDEX").fieldWS(str, delim, count);
        }

        default IFunction SUBSTRING_INDEX(IFunction str, IFunction delim, Number count) {
            return SUBSTRING_INDEX(str.build(), delim.build(), count).param(str.params()).param(delim.params());
        }

        // ------ 返回字符串 str ， 其中所有remstr 前缀和/或后缀都已被删除。若分类符BOTH、LEADIN或TRAILING中没有一个是给定的,则假设为BOTH 。 remstr 为可选项，在未指定情况下，可删除空格。

        default IFunction TRIM(String str) {
            return create("TRIM").field(str);
        }

        default IFunction TRIM(IFunction str) {
            return TRIM(str.build()).param(str.params());
        }

        default IFunction TRIM_BOTH(String remstr, String str) {
            return create("TRIM").field("BOTH " + remstr + " FROM " + str);
        }

        default IFunction TRIM_BOTH(IFunction remstr, IFunction str) {
            return TRIM_BOTH(remstr.build(), str.build()).param(remstr.params()).param(str.params());
        }

        default IFunction TRIM_LEADIN(String remstr, String str) {
            return create("TRIM").field("LEADIN " + remstr + " FROM " + str);
        }

        default IFunction TRIM_LEADIN(IFunction remstr, IFunction str) {
            return TRIM_LEADIN(remstr.build(), str.build()).param(remstr.params()).param(str.params());
        }

        default IFunction TRIM_TRAILING(String remstr, String str) {
            return create("TRIM").field("TRAILING " + remstr + " FROM " + str);
        }

        default IFunction TRIM_TRAILING(IFunction remstr, IFunction str) {
            return TRIM_TRAILING(remstr.build(), str.build()).param(remstr.params()).param(str.params());
        }

        // ------ 执行从HEX(str)的反向操作。就是说，它将参数中的每一对十六进制数字理解为一个数字，并将其转化为该数字代表的字符。结果字符以二进制字符串的形式返回。

        default IFunction UNHEX(String str) {
            return create("UNHEX").field(str);
        }

        default IFunction UNHEX(IFunction str) {
            return UNHEX(str.build()).param(str.params());
        }

        // ------ 返回字符串str， 以及根据最新字符集映射转化为大写字母的字符

        default IFunction UPPER(String str) {
            return create("UPPER").field(str);
        }

        default IFunction UPPER(IFunction str) {
            return UPPER(str.build()).param(str.params());
        }
    }

    /**
     * Date and Time Functions
     */
    @Ignored
    interface DateTime {

        // ------ 若 days 参数只是整数值，将其作为天数值添加至 expr。

        default IFunction ADDDATE(String expr, Number days) {
            return create("ADDDATE").fieldWS(expr, days);
        }

        default IFunction ADDDATE(IFunction expr, Number days) {
            return ADDDATE(expr.build(), days).param(expr.params());
        }

        // ------ 将 expr2添加至expr 然后返回结果。 expr 是一个时间或时间日期表达式，而expr2 是一个时间表达式。

        default IFunction ADDTIME(String expr, String expr2) {
            return create("ADDTIME").fieldWS(expr, expr2);
        }

        default IFunction ADDTIME(IFunction expr, IFunction expr2) {
            return ADDTIME(expr.build(), expr2.build()).param(expr.params()).param(expr2.params());
        }

        // ------ 将时间日期值dt 从from_tz 给出的时区转到to_tz给出的时区，然后返回结果值。

        default IFunction CONVERT_TZ(String dt, String fromTz, String toTz) {
            return create("CONVERT_TZ").fieldWS(dt, fromTz, toTz);
        }

        default IFunction CONVERT_TZ(IFunction dt, String fromTz, String toTz) {
            return CONVERT_TZ(dt.build(), fromTz, toTz).param(dt.params());
        }

        // ------ 将当前日期按照'YYYY-MM-DD' 或YYYYMMDD 格式的值返回，具体格式根据函数用在字符串或是数字语境中而定。

        default IFunction CURDATE() {
            return create("CURDATE");
        }

        // ------ 将当前时间以'HH:MM:SS'或 HHMMSS 的格式返回， 具体格式根据函数用在字符串或是数字语境中而定。

        default IFunction CURTIME() {
            return create("CURTIME");
        }

        // ------ 提取日期或时间日期表达式expr中的日期部分

        default IFunction DATE(String expr) {
            return create("DATE").field(expr);
        }

        default IFunction DATE(IFunction expr) {
            return DATE(expr.build()).param(expr.params());
        }

        // ------ 根据format 字符串安排date 值的格式。

        default IFunction DATE_FORMAT(String date, String format) {
            return create("DATE_FORMAT").fieldWS(date, format);
        }

        default IFunction DATE_FORMAT(IFunction date, String format) {
            return DATE_FORMAT(date.build(), format).param(date.params());
        }

        // ------ 返回起始时间 expr和结束时间expr2之间的天数。Expr和expr2 为日期或 date-and-time 表达式。计算中只用到这些值的日期部分。

        default IFunction DATEDIFF(String expr, String expr2) {
            return create("DATEDIFF").fieldWS(expr, expr2);
        }

        default IFunction DATEDIFF(IFunction expr, IFunction expr2) {
            return DATEDIFF(expr.build(), expr2.build()).param(expr.params()).param(expr2.params());
        }

        // ------ 返回date 对应的工作日名称。

        default IFunction DAYNAME(String date) {
            return create("DAYNAME").field(date);
        }

        default IFunction DAYNAME(IFunction date) {
            return DAYNAME(date.build()).param(date.params());
        }

        // ------ 返回date 对应的该月日期，范围是从 1到31。

        default IFunction DAYOFMONTH(String date) {
            return create("DAYOFMONTH").field(date);
        }

        default IFunction DAYOFMONTH(IFunction date) {
            return DAYOFMONTH(date.build()).param(date.params());
        }

        // ------ 返回date (1 = 周日, 2 = 周一, ..., 7 = 周六)对应的工作日索引。这些索引值符合 ODBC标准。

        default IFunction DAYOFWEEK(String date) {
            return create("DAYOFWEEK").field(date);
        }

        default IFunction DAYOFWEEK(IFunction date) {
            return DAYOFWEEK(date.build()).param(date.params());
        }

        // ------ 返回date 对应的一年中的天数，范围是从 1到366。

        default IFunction DAYOFYEAR(String date) {
            return create("DAYOFYEAR").field(date);
        }

        default IFunction DAYOFYEAR(IFunction date) {
            return DAYOFYEAR(date.build()).param(date.params());
        }

        // ------ 返回'YYYY-MM-DD HH:MM:SS'或YYYYMMDDHHMMSS 格式值的unix_timestamp参数表示，具体格式取决于该函数是否用在字符串中或是数字语境中。

        default IFunction FROM_UNIXTIME(String timestamp) {
            return create("FROM_UNIXTIME").field(timestamp);
        }

        default IFunction FROM_UNIXTIME(IFunction timestamp) {
            return FROM_UNIXTIME(timestamp.build()).param(timestamp.params());
        }


        default IFunction FROM_UNIXTIME(String timestamp, String format) {
            return create("FROM_UNIXTIME").fieldWS(timestamp, format);
        }

        default IFunction FROM_UNIXTIME(IFunction timestamp, String format) {
            return FROM_UNIXTIME(timestamp.build(), format).param(timestamp.params());
        }

        // ------ 若无参数调用，则返回一个Unix timestamp ('1970-01-01 00:00:00' GMT 之后的秒数) 作为无符号整数。
        // ------ 若用date 来调用UNIX_TIMESTAMP()，它会将参数值以'1970-01-01 00:00:00' GMT后的秒数的形式返回。date 可以是一个DATE 字符串、一个 DATETIME字符串、一个 TIMESTAMP或一个当地时间的YYMMDD 或YYYMMDD格式的数字。

        default IFunction UNIX_TIMESTAMP() {
            return create("UNIX_TIMESTAMP");
        }

        default IFunction UNIX_TIMESTAMP(String date) {
            return create("UNIX_TIMESTAMP").field(date);
        }

        default IFunction UNIX_TIMESTAMP(IFunction date) {
            return UNIX_TIMESTAMP(date.build()).param(date.params());
        }

        // ------ 返回一个格式字符串。

        default IFunction GET_FORMAT(String date, String type) {
            return create("GET_FORMAT").fieldWS(date, type);
        }

        default IFunction GET_FORMAT(IFunction date, String type) {
            return GET_FORMAT(date.build(), type).param(date.params());
        }

        // ------ 返回time 对应的小时数。对于日时值的返回值范围是从 0 到 23 。

        default IFunction HOUR(String time) {
            return create("HOUR").field(time);
        }

        default IFunction HOUR(IFunction time) {
            return HOUR(time.build()).param(time.params());
        }

        // ------ 获取一个日期或日期时间值，返回该月最后一天对应的值。若参数无效，则返回NULL。

        default IFunction LAST_DAY(String date) {
            return create("LAST_DAY").field(date);
        }

        default IFunction LAST_DAY(IFunction date) {
            return LAST_DAY(date.build()).param(date.params());
        }

        // ------ 给出年份值和一年中的天数值，返回一个日期。dayofyear 必须大于 0 ，否则结果为 NULL。

        default IFunction MAKEDATE(String year, String dayOfYear) {
            return create("MAKEDATE").fieldWS(year, dayOfYear);
        }

        default IFunction MAKEDATE(IFunction year, IFunction dayOfYear) {
            return MAKEDATE(year.build(), dayOfYear.build()).param(year.params()).param(dayOfYear.params());
        }

        // ------ 返回由hour、 minute和second 参数计算得出的时间值

        default IFunction MAKETIME(String hour, String minute, String second) {
            return create("MAKETIME").fieldWS(hour, minute, second);
        }

        default IFunction MAKETIME(IFunction hour, IFunction minute, IFunction second) {
            return MAKETIME(hour.build(), minute.build(), second.build()).param(hour.params()).param(minute.params()).param(second.params());
        }

        // ------ 从时间或日期时间表达式expr返回微秒值，其数字范围从 0到 999999。

        default IFunction MICROSECOND(String expr) {
            return create("MICROSECOND").field(expr);
        }

        default IFunction MICROSECOND(IFunction expr) {
            return MICROSECOND(expr.build()).param(expr.params());
        }

        // ------ 返回 time 对应的分钟数,范围是从 0 到 59。

        default IFunction MINUTE(String time) {
            return create("MINUTE").field(time);
        }

        default IFunction MINUTE(IFunction time) {
            return MINUTE(time.build()).param(time.params());
        }

        // ------ 返回date 对应的月份，范围时从 1 到 12。

        default IFunction MONTH(String date) {
            return create("MONTH").field(date);
        }

        default IFunction MONTH(IFunction date) {
            return MONTH(date.build()).param(date.params());
        }

        // ------ 返回date 对应月份的全名。

        default IFunction MONTHNAME(String date) {
            return create("MONTHNAME").field(date);
        }

        default IFunction MONTHNAME(IFunction date) {
            return MONTHNAME(date.build()).param(date.params());
        }

        // ------ 返回当前日期和时间值，其格式为 'YYYY-MM-DD HH:MM:SS' 或YYYYMMDDHHMMSS ， 具体格式取决于该函数是否用在字符串中或数字语境中。

        default IFunction NOW() {
            return create("NOW");
        }

        // ------ 添加 N 个月至周期P (格式为YYMM 或YYYYMM)，返回值的格式为 YYYYMM。注意周期参数 P 不是日期值。

        default IFunction PERIOD_ADD(String p, String n) {
            return create("PERIOD_ADD").fieldWS(p, n);
        }

        default IFunction PERIOD_ADD(IFunction p, IFunction n) {
            return PERIOD_ADD(p.build(), n.build()).param(p.params()).param(n.params());
        }

        // ------ 返回周期P1和 P2 之间的月份数。P1 和P2 的格式应该为YYMM或YYYYMM。注意周期参数 P1和P2 不是日期值。

        default IFunction PERIOD_DIFF(String p1, String p2) {
            return create("PERIOD_DIFF").fieldWS(p1, p2);
        }

        default IFunction PERIOD_DIFF(IFunction p1, IFunction p2) {
            return PERIOD_DIFF(p1.build(), p2.build()).param(p1.params()).param(p2.params());
        }

        // ------ 返回date 对应的一年中的季度值，范围是从 1到 4。

        default IFunction QUARTER(String date) {
            return create("QUARTER").field(date);
        }

        default IFunction QUARTER(IFunction date) {
            return QUARTER(date.build()).param(date.params());
        }

        // ------ 返回被转化为小时、 分钟和秒数的seconds参数值, 其格式为 'HH:MM:SS' 或HHMMSS，具体格式根据该函数是否用在字符串或数字语境中而定。

        default IFunction SEC_TO_TIME(String seconds) {
            return create("SEC_TO_TIME").field(seconds);
        }

        default IFunction SEC_TO_TIME(IFunction seconds) {
            return SEC_TO_TIME(seconds.build()).param(seconds.params());
        }

        // ------ 返回time 对应的秒数, 范围是从 0到59。

        default IFunction SECOND(String date) {
            return create("SECOND").field(date);
        }

        default IFunction SECOND(IFunction date) {
            return SECOND(date.build()).param(date.params());
        }

        // ------ 这是DATE_FORMAT() 函数的倒转。它获取一个字符串 str 和一个格式字符串format。若格式字符串包含日期和时间部分，则 STR_TO_DATE()返回一个 DATETIME 值， 若该字符串只包含日期部分或时间部分，则返回一个 DATE 或TIME值。

        default IFunction STR_TO_DATE(String str, String format) {
            return create("STR_TO_DATE").fieldWS(str, format);
        }

        default IFunction STR_TO_DATE(IFunction str, String format) {
            return STR_TO_DATE(str.build(), format).param(str.params());
        }

        // ------ 返回当前日期和时间值，格式为'YYYY-MM-DD HH:MM:SS' 或YYYYMMDDHHMMSS， 具体格式根据函数是否用在字符串或数字语境而定。

        default IFunction SYSDATE() {
            return create("SYSDATE");
        }

        // ------ 提取一个时间或日期时间表达式的时间部分，并将其以字符串形式返回。

        default IFunction TIME(String expr) {
            return create("TIME").field(expr);
        }

        default IFunction TIME(IFunction expr) {
            return TIME(expr.build()).param(expr.params());
        }

        // ------ 其使用和 DATE_FORMAT()函数相同, 然而format 字符串可能仅会包含处理小时、分钟和秒的格式说明符。其它说明符产生一个NULL值或0。

        default IFunction TIME_FORMAT(String time, String format) {
            return create("TIME_FORMAT").fieldWS(time, format);
        }

        default IFunction TIME_FORMAT(IFunction time, String format) {
            return TIME_FORMAT(time.build(), format).param(time.params());
        }

        // ------ 返回已转化为秒的time参数。

        default IFunction TIME_TO_SEC(String time) {
            return create("TIME_TO_SEC").field(time);
        }

        default IFunction TIME_TO_SEC(IFunction time) {
            return TIME_TO_SEC(time.build()).param(time.params());
        }

        // ------ 返回起始时间 expr 和结束时间expr2 之间的时间。 expr 和expr2 为时间或 date-and-time 表达式,两个的类型必须一样。

        default IFunction TIMEDIFF(String expr, String expr2) {
            return create("TIMEDIFF").fieldWS(expr, expr2);
        }

        default IFunction TIMEDIFF(IFunction expr, IFunction expr2) {
            return TIMEDIFF(expr.build(), expr2.build()).param(expr.params()).param(expr2.params());
        }

        // ------ 对于一个单参数,该函数将日期或日期时间表达式 expr 作为日期时间值返回.对于两个参数, 它将时间表达式 expr2 添加到日期或日期时间表达式 expr 中，将theresult作为日期时间值返回。

        default IFunction TIMESTAMP(String expr) {
            return create("TIMESTAMP").field(expr);
        }

        default IFunction TIMESTAMP(IFunction expr) {
            return TIMESTAMP(expr.build()).param(expr.params());
        }

        default IFunction TIMESTAMP(String expr, String expr2) {
            return create("TIMESTAMP").fieldWS(expr, expr2);
        }

        default IFunction TIMESTAMP(IFunction expr, IFunction expr2) {
            return TIMESTAMP(expr.build(), expr2.build()).param(expr.params()).param(expr2.params());
        }

        // ------ 返回日期或日期时间表达式datetime_expr1 和datetime_expr2the 之间的整数差。其结果的单位由unit 参数给出。

        default IFunction TIMESTAMPDIFF(String unit, String datetimeExpr1, String datetimeExpr2) {
            return create("TIMESTAMPDIFF").fieldWS(unit, datetimeExpr1, datetimeExpr2);
        }

        default IFunction TIMESTAMPDIFF(String unit, IFunction datetimeExpr1, IFunction datetimeExpr2) {
            return TIMESTAMPDIFF(unit, datetimeExpr1.build(), datetimeExpr2.build()).param(datetimeExpr1.params()).param(datetimeExpr2.params());
        }

        // ------ 给定一个日期date, 返回一个天数 (从年份0开始的天数 )。

        default IFunction TO_DAYS(String date) {
            return create("TO_DAYS").field(date);
        }

        default IFunction TO_DAYS(IFunction date) {
            return TO_DAYS(date.build()).param(date.params());
        }

        // ------ 返回当前 UTC日期值，其格式为 'YYYY-MM-DD' 或 YYYYMMDD，具体格式取决于函数是否用在字符串或数字语境中。

        default IFunction UTC_DATE() {
            return create("UTC_DATE");
        }

        // ------ 返回当前 UTC 值，其格式为  'HH:MM:SS' 或HHMMSS，具体格式根据该函数是否用在字符串或数字语境而定。

        default IFunction UTC_TIME() {
            return create("UTC_TIME");
        }

        // ------ 返回当前UTC日期及时间值，格式为 'YYYY-MM-DD HH:MM:SS' 或YYYYMMDDHHMMSS，具体格式根据该函数是否用在字符串或数字语境而定。

        default IFunction UTC_TIMESTAMP() {
            return create("UTC_TIMESTAMP");
        }

        // ------ 该函数返回date 对应的星期数。WEEK() 的双参数形式允许你指定该星期是否起始于周日或周一， 以及返回值的范围是否为从0 到53 或从1 到53。若 mode参数被省略，则使用default_week_format系统自变量的值。

        default IFunction WEEK(String date) {
            return create("WEEK").field(date);
        }

        default IFunction WEEK(IFunction date) {
            return WEEK(date.build()).param(date.params());
        }

        default IFunction WEEK(String date, Number mode) {
            return create("WEEK").fieldWS(date, mode);
        }

        default IFunction WEEK(IFunction date, Number mode) {
            return WEEK(date.build(), mode).param(date.params());
        }

        // ------ 返回date (0 = 周一, 1 = 周二, ... 6 = 周日)对应的工作日索引

        default IFunction WEEKDAY(String date) {
            return create("WEEKDAY").field(date);
        }

        default IFunction WEEKDAY(IFunction date) {
            return WEEKDAY(date.build()).param(date.params());
        }

        // ------ 将该日期的阳历周以数字形式返回，范围是从1到53。它是一个兼容度函数，相当于WEEK(date,3)。

        default IFunction WEEKOFYEAR(String date) {
            return create("WEEKOFYEAR").field(date);
        }

        default IFunction WEEKOFYEAR(IFunction date) {
            return WEEKOFYEAR(date.build()).param(date.params());
        }

        // ------ 返回date 对应的年份,范围是从1000到9999。

        default IFunction YEAR(String date) {
            return create("YEAR").field(date);
        }

        default IFunction YEAR(IFunction date) {
            return YEAR(date.build()).param(date.params());
        }

        // ------ 返回一个日期对应的年或周。start参数的工作同 start参数对 WEEK()的工作相同。结果中的年份可以和该年的第一周和最后一周对应的日期参数有所不同。

        default IFunction YEARWEEK(String date) {
            return create("YEARWEEK").field(date);
        }

        default IFunction YEARWEEK(IFunction date) {
            return YEARWEEK(date.build()).param(date.params());
        }

        default IFunction YEARWEEK(String date, Number mode) {
            return create("YEARWEEK").fieldWS(date, mode);
        }

        default IFunction YEARWEEK(IFunction date, Number mode) {
            return YEARWEEK(date.build(), mode).param(date.params());
        }
    }

    /**
     * Aggregate (GROUP BY) Function Descriptions
     */
    @Ignored
    interface Aggregate {

        // ------ 返回expr 的平均值。 DISTINCT 选项可用于返回 expr的不同值的平均值。

        default IFunction AVG(String expr) {
            return AVG(false, expr);
        }

        default IFunction AVG(IFunction expr) {
            return AVG(false, expr.build()).param(expr.params());
        }

        default IFunction AVG(boolean distinct, String expr) {
            return create("AVG").field(distinct ? "DISTINCT " : "").field(expr);
        }

        default IFunction AVG(IFunction expr, boolean distinct) {
            return AVG(distinct, expr.build()).param(expr.params());
        }

        // ------ 返回expr中所有比特的 bitwise AND 。计算执行的精确度为64比特(BIGINT) 。

        default IFunction BIT_AND(String expr) {
            return create("BIT_AND").field(expr);
        }

        default IFunction BIT_AND(IFunction expr) {
            return BIT_AND(expr.build()).param(expr.params());
        }

        // ------ 返回expr 中所有比特的bitwise OR。计算执行的精确度为64比特(BIGINT) 。

        default IFunction BIT_OR(String expr) {
            return create("BIT_OR").field(expr);
        }

        default IFunction BIT_OR(IFunction expr) {
            return BIT_OR(expr.build()).param(expr.params());
        }

        // ------ 返回expr 中所有比特的bitwise XOR。计算执行的精确度为64比特(BIGINT) 。

        default IFunction BIT_XOR(String expr) {
            return create("BIT_XOR").field(expr);
        }

        default IFunction BIT_XOR(IFunction expr) {
            return BIT_XOR(expr.build()).param(expr.params());
        }

        // ------ 返回SELECT语句检索到的行中非NULL值的数目。

        default IFunction COUNT(String expr) {
            return COUNT(false, expr);
        }

        default IFunction COUNT(IFunction expr) {
            return COUNT(false, expr.build()).param(expr.params());
        }

        default IFunction COUNT(boolean distinct, String expr) {
            return create("COUNT").field(distinct ? "DISTINCT " : "").field(expr);
        }

        default IFunction COUNT(IFunction expr, boolean distinct) {
            return COUNT(distinct, expr.build()).param(expr.params());
        }

        // ------

        default IFunction GROUP_CONCAT(boolean distinct, OrderBy orderBy, String separator, String... expr) {
            AbstractFunction func = create("GROUP_CONCAT").field(distinct ? "DISTINCT " : "").field(expr);
            if (orderBy != null && !orderBy.isEmpty()) {
                func.space().field(orderBy.toSQL()).param(orderBy.params());
            }
            if (StringUtils.isNotEmpty(separator)) {
                func.space().field("SEPARATOR ").field(separator);
            }
            return func;
        }

        default IFunction GROUP_CONCAT(boolean distinct, String... expr) {
            return GROUP_CONCAT(distinct, null, null, expr);
        }

        default IFunction GROUP_CONCAT(boolean distinct, OrderBy orderBy, String separator, IFunction... expr) {
            Params params = Params.create();
            List<String> fields = new ArrayList<>();
            Arrays.stream(expr).forEach(function -> {
                fields.add(function.build());
                params.add(function.params());
            });
            return GROUP_CONCAT(distinct, orderBy, separator, fields.toArray(new String[0])).param(params);
        }

        default IFunction GROUP_CONCAT(boolean distinct, IFunction... expr) {
            return GROUP_CONCAT(distinct, null, null, expr);
        }

        default IFunction GROUP_CONCAT(String... expr) {
            return GROUP_CONCAT(false, null, null, expr);
        }

        default IFunction GROUP_CONCAT(IFunction... expr) {
            return GROUP_CONCAT(false, null, null, expr);
        }

        // ------ 返回expr 的最小值和最大值。

        default IFunction MAX(boolean distinct, String expr) {
            return create("MAX").field(distinct ? "DISTINCT " : "").field(expr);
        }

        default IFunction MAX(boolean distinct, IFunction expr) {
            return MAX(distinct, expr.build()).param(expr.params());
        }

        default IFunction MAX(String expr) {
            return MAX(false, expr);
        }

        default IFunction MAX(IFunction expr) {
            return MAX(false, expr.build()).param(expr.params());
        }

        // ---

        default IFunction MIN(boolean distinct, String expr) {
            return create("MIN").field(distinct ? "DISTINCT " : "").field(expr);
        }

        default IFunction MIN(boolean distinct, IFunction expr) {
            return MIN(distinct, expr.build()).param(expr.params());
        }

        default IFunction MIN(String expr) {
            return MIN(false, expr);
        }

        default IFunction MIN(IFunction expr) {
            return MIN(false, expr.build()).param(expr.params());
        }

        // ------ 返回expr 的总数。 若返回集合中无任何行，则 SUM() 返回NULL。

        default IFunction SUM(boolean distinct, String expr) {
            return create("SUM").field(distinct ? "DISTINCT " : "").field(expr);
        }

        default IFunction SUM(boolean distinct, IFunction expr) {
            return SUM(distinct, expr.build()).param(expr.params());
        }

        default IFunction SUM(String expr) {
            return SUM(false, expr);
        }

        default IFunction SUM(IFunction expr) {
            return SUM(false, expr.build()).param(expr.params());
        }
    }

    /**
     * Control Flow Functions
     */
    @Ignored
    interface ControlFlow {

        default IFunction CASE(Cond value, IFunction[] whenFn, String elseFn) {
            return new AbstractFunction() {
                @Override
                public void onBuild() {
                    field("CASE ");
                    if (value != null) {
                        field(value.toString()).space().param(value.params());
                    }
                    Arrays.stream(whenFn).forEach(func -> field(func).space());
                    if (elseFn != null) {
                        field(elseFn).space();
                    }
                    field("END");
                }
            };
        }

        default IFunction CASE(Cond value, IFunction[] whenFn, IFunction elseFn) {
            IFunction function = CASE(value, whenFn, elseFn != null ? elseFn.build() : null);
            if (elseFn != null) {
                function.param(elseFn.params());
            }
            return function;
        }

        default IFunction CASE(IFunction value, IFunction[] when) {
            return CASE(value, when, null);
        }

        default IFunction CASE(String value, IFunction[] whenFn) {
            return CASE(value, whenFn, (String) null);
        }

        default IFunction CASE(IFunction[] when) {
            return CASE((String) null, when);
        }

        default IFunction CASE(IFunction[] when, IFunction elseFn) {
            return CASE((IFunction) null, when, elseFn);
        }

        default IFunction CASE(IFunction value, IFunction[] whenFn, IFunction elseFn) {
            return new AbstractFunction() {
                @Override
                public void onBuild() {
                    field("CASE ");
                    if (value != null) {
                        field(value).space();
                    }
                    Arrays.stream(whenFn).forEach(func -> field(func).space());
                    if (elseFn != null) {
                        field(elseFn).space();
                    }
                    field("END");
                }
            };
        }

        default IFunction CASE(String value, IFunction[] whenFn, IFunction elseFn) {
            IFunction function = CASE(value, whenFn, elseFn != null ? elseFn.build() : null);
            if (elseFn != null) {
                function.param(elseFn.params());
            }
            return function;
        }

        default IFunction CASE(String value, IFunction[] whenFn, String elseFn) {
            return new AbstractFunction() {
                @Override
                public void onBuild() {
                    field("CASE ");
                    if (StringUtils.isNotBlank(value)) {
                        field(value).space();
                    }
                    Arrays.stream(whenFn).forEach(func -> field(func).space());
                    if (StringUtils.isNotBlank(elseFn)) {
                        field(elseFn).space();
                    }
                    field("END");
                }
            };
        }

        // ---

        default IFunction WHEN(Cond expr) {
            return WHEN(expr.toString(), "?").param(expr.params());
        }

        default IFunction WHEN(IFunction expr) {
            return WHEN(expr.build(), "?").param(expr.params());
        }

        default IFunction WHEN(Cond expr, IFunction result) {
            return WHEN(expr.toString(), result.build()).param(expr.params()).param(result.params());
        }

        default IFunction WHEN(IFunction expr, IFunction result) {
            return WHEN(expr.build(), result.build()).param(expr.params()).param(result.params());
        }

        default IFunction WHEN(String expr) {
            return WHEN(expr, "?");
        }

        default IFunction WHEN(String expr, String result) {
            return new AbstractFunction() {
                @Override
                public void onBuild() {
                    field("WHEN ").field(expr).space().field("THEN ").field(result).space();
                }
            };
        }

        // ---

        default IFunction ELSE() {
            return ELSE("?");
        }

        default IFunction ELSE(IFunction result) {
            return ELSE(result.build()).param(result.params());
        }

        default IFunction ELSE(String result) {
            return new AbstractFunction() {
                @Override
                public void onBuild() {
                    field("ELSE ").field(result).space();
                }
            };
        }

        // ---

        default IFunction IF(Cond expr) {
            return create("IF").fieldWS(expr.toString(), "?", "?").param(expr.params());
        }

        default IFunction IF(Cond expr1, IFunction expr2, IFunction expr3) {
            return create("IF").fieldWS(expr1.toString(), expr2.build(), expr3.build())
                    .param(expr1.params())
                    .param(expr2.params())
                    .param(expr3.params());
        }

        default IFunction IF(Cond expr1, IFunction expr2, String expr3) {
            return create("IF").fieldWS(expr1.toString(), expr2.build(), expr3)
                    .param(expr1.params())
                    .param(expr2.params());
        }

        default IFunction IF(Cond expr1, String expr2, IFunction expr3) {
            return create("IF").fieldWS(expr1.toString(), expr2, expr3)
                    .param(expr1.params())
                    .param(expr3.params());
        }

        default IFunction IF(Cond expr1, String expr2, String expr3) {
            return create("IF").fieldWS(expr1.toString(), expr2, expr3).param(expr1.params());
        }

        default IFunction IF(String expr1, String expr2, String expr3) {
            return create("IF").fieldWS(expr1, expr2, expr3);
        }

        // ---

        default IFunction IFNULL() {
            return create("IFNULL").fieldWS("?", "?");
        }

        default IFunction IFNULL(IFunction expr1, IFunction expr2) {
            return create("IFNULL").fieldWS(expr1, expr2);
        }

        default IFunction IFNULL(String expr1, String expr2) {
            return create("IFNULL").fieldWS(expr1, expr2);
        }

        // ---

        default IFunction NULLIF() {
            return create("NULLIF").fieldWS("?", "?");
        }

        default IFunction NULLIF(IFunction expr1, IFunction expr2) {
            return create("NULLIF").fieldWS(expr1, expr2);
        }

        default IFunction NULLIF(String expr1, String expr2) {
            return create("NULLIF").fieldWS(expr1, expr2);
        }
    }

    /**
     * Operators
     */
    @Ignored
    interface Operators {

        /**
         * 括号
         *
         * @param content 内容
         * @return 返回加括号的内容
         */
        default IFunction brackets(IFunction content) {
            return create().bracketBegin().field(content).bracketEnd();
        }

        default IFunction brackets(String content) {
            return create().bracketBegin().field(content).bracketEnd();
        }

        /**
         * 引号
         *
         * @param content 内容
         * @return 返回加引号的内容
         */
        default IFunction quotes(IFunction content) {
            return create().quotes().field(content).quotes();
        }

        default IFunction quotes(String content) {
            return create().quotes().field(content).quotes();
        }

        /**
         * 加法
         *
         * @param param 数值型被加数
         * @return 返回函数接口对象
         */
        default IFunction addition(Number param) {
            return addition(param.toString());
        }

        default IFunction addition(String paramOne, Number paramTwo) {
            return addition(paramOne, paramTwo.toString());
        }

        /**
         * 加法
         *
         * @param param 函数型被加数
         * @return 返回函数接口对象
         */
        default IFunction addition(IFunction param) {
            return addition(param.toString()).param(param.params());
        }

        default IFunction addition(String paramOne, IFunction paramTwo) {
            return addition(paramOne, paramTwo.toString()).param(paramTwo.params());
        }

        default IFunction addition(IFunction paramOne, IFunction paramTwo) {
            return addition(paramOne.toString(), paramTwo.toString())
                    .param(paramOne.params())
                    .param(paramTwo.params());
        }

        /**
         * 加法
         *
         * @param param 字符串型被加数
         * @return 返回函数接口对象
         */
        default IFunction addition(String param) {
            return operate("+", param);
        }

        default IFunction addition(String paramOne, String paramTwo) {
            return operate(paramOne, "+", paramTwo);
        }

        // ---

        /**
         * 减法
         *
         * @param param 数值型被加数
         * @return 返回函数接口对象
         */
        default IFunction subtract(Number param) {
            return subtract(param.toString());
        }

        default IFunction subtract(String paramOne, Number paramTwo) {
            return subtract(paramOne, paramTwo.toString());
        }

        default IFunction subtract(Number paramOne, String paramTwo) {
            return subtract(paramOne.toString(), paramTwo);
        }

        /**
         * 减法
         *
         * @param param 函数型被加数
         * @return 返回函数接口对象
         */
        default IFunction subtract(IFunction param) {
            return subtract(param.build()).param(param.params());
        }

        default IFunction subtract(String paramOne, IFunction paramTwo) {
            return subtract(paramOne, paramTwo.build()).param(paramTwo.params());
        }

        default IFunction subtract(IFunction paramOne, String paramTwo) {
            return subtract(paramOne.build(), paramTwo).param(paramOne.params());
        }

        default IFunction subtract(IFunction paramOne, IFunction paramTwo) {
            return subtract(paramOne.toString(), paramTwo.build())
                    .param(paramOne.params())
                    .param(paramTwo.params());
        }

        /**
         * 减法
         *
         * @param param 字符串被加数
         * @return 返回函数接口对象
         */
        default IFunction subtract(String param) {
            return operate("-", param);
        }

        default IFunction subtract(String paramOne, String paramTwo) {
            return operate(paramOne, "-", paramTwo);
        }

        // ---

        /**
         * 乘法
         *
         * @param param 数值型被加数
         * @return 返回函数接口对象
         */
        default IFunction multiply(Number param) {
            return multiply(param.toString());
        }

        default IFunction multiply(String paramOne, Number paramTwo) {
            return multiply(paramOne, paramTwo.toString());
        }

        /**
         * 乘法
         *
         * @param param 函数型被加数
         * @return 返回函数接口对象
         */
        default IFunction multiply(IFunction param) {
            return multiply(param.build()).param(param.params());
        }

        default IFunction multiply(String paramOne, IFunction paramTwo) {
            return multiply(paramOne, paramTwo.build()).param(paramTwo.params());
        }

        default IFunction multiply(IFunction paramOne, IFunction paramTwo) {
            return multiply(paramOne.toString(), paramTwo.build())
                    .param(paramOne.params())
                    .param(paramTwo.params());
        }

        /**
         * 乘法
         *
         * @param param 字符串被加数
         * @return 返回函数接口对象
         */
        default IFunction multiply(String param) {
            return operate("*", param);
        }

        default IFunction multiply(String paramOne, String paramTwo) {
            return operate(paramOne, "*", paramTwo);
        }

        // ---

        /**
         * 除法
         *
         * @param param 数值型被加数
         * @return 返回函数接口对象
         */
        default IFunction divide(Number param) {
            return divide(param.toString());
        }

        default IFunction divide(String paramOne, Number paramTwo) {
            return divide(paramOne, paramTwo.toString());
        }

        default IFunction divide(Number paramOne, String paramTwo) {
            return divide(paramOne.toString(), paramTwo);
        }

        /**
         * 除法
         *
         * @param param 函数型被加数
         * @return 返回函数接口对象
         */
        default IFunction divide(IFunction param) {
            return divide(param.build()).param(param.params());
        }

        default IFunction divide(String paramOne, IFunction paramTwo) {
            return divide(paramOne, paramTwo.build()).param(paramTwo.params());
        }

        default IFunction divide(IFunction paramOne, String paramTwo) {
            return divide(paramOne.build(), paramTwo).param(paramOne.params());
        }

        default IFunction divide(IFunction paramOne, IFunction paramTwo) {
            return divide(paramOne.toString(), paramTwo.build())
                    .param(paramOne.params())
                    .param(paramTwo.params());
        }

        /**
         * 除法
         *
         * @param param 字符串被加数
         * @return 返回函数接口对象
         */
        default IFunction divide(String param) {
            return operate("/", param);
        }

        default IFunction divide(String paramOne, String paramTwo) {
            return operate(paramOne, "/", paramTwo);
        }
    }
}
