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
package net.ymate.platform.core;

/**
 * 版本信息描述类
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/12 下午11:31
 */
public class Version {

    public enum VersionType {

        /**
         * α（alpha） 内部测试版
         */
        Alpha,

        /**
         * β（beta）外部测试版
         */
        Beta,

        /**
         * γ（gamma）版
         */
        GA,

        /**
         * trial（试用版）
         */
        Trial,

        /**
         * demo 演示版
         */
        Demo,

        /**
         * release 最终释放版
         */
        Release
    }

    private int majorVersion;

    private int minorVersion;

    private int revisionNumber;

    private String buildNumber;

    private VersionType versionType;

    public Version(int majorVersion, int minorVersion, int revisionNumber) {
        this(majorVersion, minorVersion, revisionNumber, (String) null, null);
    }

    public Version(int majorVersion, int minorVersion, int revisionNumber, VersionType versionType) {
        this(majorVersion, minorVersion, revisionNumber, (String) null, versionType);
    }

    public Version(int majorVersion, int minorVersion, int revisionNumber, Class<?> targetClass, VersionType versionType) {
        this(majorVersion, minorVersion, revisionNumber, targetClass != null ? targetClass.getPackage().getImplementationVersion() : null, versionType);
    }

    public Version(int majorVersion, int minorVersion, int revisionNumber, String buildNumber, VersionType versionType) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.revisionNumber = revisionNumber;
        this.buildNumber = buildNumber != null ? buildNumber : Version.class.getPackage().getImplementationVersion();
        this.versionType = versionType;
    }

    public Version(Version parent, Class<?> targetClass) {
        this(parent.getMajorVersion(), parent.getMinorVersion(), parent.getRevisionNumber(), (targetClass != null ? targetClass.getPackage().getImplementationVersion() : parent.getBuildNumber()), parent.getVersionType());
    }

    /**
     * @return 返回主版本号
     */
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * @return 返回子版本号
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * @return 返回修正版本号
     */
    public int getRevisionNumber() {
        return revisionNumber;
    }

    /**
     * @return 返回编译版本号
     */
    public String getBuildNumber() {
        return buildNumber;
    }

    public VersionType getVersionType() {
        return versionType;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder()
                .append(majorVersion).append(".")
                .append(minorVersion).append(".")
                .append(revisionNumber);
        if (versionType != null) {
            stringBuilder.append("-").append(versionType);
        }
        if (buildNumber != null) {
            stringBuilder.append(" build-").append(buildNumber);
        }
        return stringBuilder.toString();
    }
}
