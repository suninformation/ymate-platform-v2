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
package net.ymate.platform.webmvc.validate;

import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.core.support.IContext;
import net.ymate.platform.validation.AbstractValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.IWebMvcConfig;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 16/3/20 上午3:48
 */
@CleanProxy
public class UploadFileValidator extends AbstractValidator {

    private static final String I18N_MESSAGE_BETWEEN_KEY = "ymp.validation.upload_file_between";

    private static final String I18N_MESSAGE_BETWEEN_DEFAULT_VALUE = "{0} size must be between {1} and {2}.";

    private static final String I18N_MESSAGE_TOTAL_MAX_KEY = "ymp.validation.upload_file_total_max";

    private static final String I18N_MESSAGE_TOTAL_MAX_DEFAULT_VALUE = "{0} total size must be lt {1}.";

    private static final String I18N_MESSAGE_MAX_KEY = "ymp.validation.upload_file_max";

    private static final String I18N_MESSAGE_MAX_DEFAULT_VALUE = "{0} size must be lt {1}.";

    private static final String I18N_MESSAGE_MIN_KEY = "ymp.validation.upload_file_min";

    private static final String I18N_MESSAGE_MIN_DEFAULT_VALUE = "{0} size must be gt {1}.";

    private static final String I18N_MESSAGE_CONTENT_TYPE_KEY = "ymp.validation.upload_file_content_type";

    private static final String I18N_MESSAGE_CONTENT_TYPE_DEFAULT_VALUE = "{0} content type must be match {1}.";

    @Override
    public ValidateResult validate(ValidateContext context) {
        // 待验证的参数必须是IUploadFileWrapper类型
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            VUploadFile uploadFileAnn = (VUploadFile) context.getAnnotation();
            //
            List<IUploadFileWrapper> fileWrappers = new ArrayList<>();
            if (paramValue.getClass().isArray() && paramValue instanceof IUploadFileWrapper[]) {
                IUploadFileWrapper[] values = (IUploadFileWrapper[]) paramValue;
                fileWrappers.addAll(Arrays.asList(values));
            } else if (paramValue instanceof IUploadFileWrapper) {
                fileWrappers.add((IUploadFileWrapper) paramValue);
            }
            if (!fileWrappers.isEmpty()) {
                Set<String> allowedContentTypes = getAllowedContentTypes(context, uploadFileAnn.contentTypes());
                long totalSize = 0;
                ValidateResult validateResult = null;
                for (IUploadFileWrapper fileWrapper : fileWrappers) {
                    totalSize += fileWrapper.getSize();
                    if (uploadFileAnn.totalMax() > 0 && totalSize > uploadFileAnn.totalMax()) {
                        ValidateResult.Builder builder = ValidateResult.builder(context).matched(true);
                        if (StringUtils.isNotBlank(uploadFileAnn.msg())) {
                            validateResult = builder.msg(uploadFileAnn.msg()).build();
                        } else {
                            validateResult = builder.msg(I18N_MESSAGE_TOTAL_MAX_KEY, I18N_MESSAGE_TOTAL_MAX_DEFAULT_VALUE, uploadFileAnn.totalMax()).build();
                        }
                        break;
                    } else {
                        int result = validate(fileWrapper, allowedContentTypes, uploadFileAnn.min(), uploadFileAnn.max());
                        if (result > 0) {
                            ValidateResult.Builder builder = ValidateResult.builder(context).matched(true);
                            if (StringUtils.isNotBlank(uploadFileAnn.msg())) {
                                validateResult = builder.msg(uploadFileAnn.msg()).build();
                            } else if (result == 3) {
                                validateResult = builder.msg(I18N_MESSAGE_CONTENT_TYPE_KEY, I18N_MESSAGE_CONTENT_TYPE_DEFAULT_VALUE, StringUtils.join(allowedContentTypes, ",")).build();
                            } else {
                                if (uploadFileAnn.min() > 0 && uploadFileAnn.max() > 0) {
                                    validateResult = builder.msg(I18N_MESSAGE_BETWEEN_KEY, I18N_MESSAGE_BETWEEN_DEFAULT_VALUE, uploadFileAnn.min(), uploadFileAnn.max()).build();
                                } else if (result == 2) {
                                    validateResult = builder.msg(I18N_MESSAGE_MAX_KEY, I18N_MESSAGE_MAX_DEFAULT_VALUE, uploadFileAnn.max()).build();
                                } else if (result == 1) {
                                    validateResult = builder.msg(I18N_MESSAGE_MIN_KEY, I18N_MESSAGE_MIN_DEFAULT_VALUE, uploadFileAnn.min()).build();
                                }
                            }
                            break;
                        }
                    }
                }
                return validateResult;
            }
        }
        return null;
    }

    /**
     * 验证上传的文件是否合法
     *
     * @param value               上传文件包装器
     * @param allowedContentTypes 允许的ContentType集合
     * @param minSize             文件size最小值
     * @param maxSize             文件size最大值
     * @return 返回结果为0表示合法，为1表示文件size小于minSize，为2表示文件size大于maxSize，为3表示文件ContentType不在允许范围
     */
    public static int validate(IUploadFileWrapper value, Set<String> allowedContentTypes, int minSize, int maxSize) {
        if (minSize > 0 && value.getSize() < minSize) {
            return 1;
        } else if (maxSize > 0 && value.getSize() > maxSize) {
            return 2;
        } else if (allowedContentTypes.size() > 0) {
            if (allowedContentTypes.stream().noneMatch(contentType -> StringUtils.contains(value.getContentType(), contentType))) {
                return 3;
            }
        }
        return 0;
    }

    public static Set<String> getAllowedContentTypes(IContext context, String... allowContentTypes) {
        Set<String> contentTypes = new HashSet<>();
        if (ArrayUtils.isNotEmpty(allowContentTypes)) {
            contentTypes.addAll(Arrays.asList(allowContentTypes));
        }
        String[] types = StringUtils.split(StringUtils.trimToEmpty(context.getContextParams().get(IWebMvcConfig.PARAMS_ALLOWED_UPLOAD_CONTENT_TYPES)), "|");
        if (ArrayUtils.isNotEmpty(types)) {
            contentTypes.addAll(Arrays.asList(types));
        }
        types = StringUtils.split(StringUtils.trimToEmpty(context.getOwner().getParam(IWebMvcConfig.PARAMS_ALLOWED_UPLOAD_CONTENT_TYPES)), "|");
        if (ArrayUtils.isNotEmpty(types)) {
            contentTypes.addAll(Arrays.asList(types));
        }
        return contentTypes;
    }
}
