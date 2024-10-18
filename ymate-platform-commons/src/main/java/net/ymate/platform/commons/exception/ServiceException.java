 /*
  * Copyright 2007-2021 the original author or authors.
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
 package net.ymate.platform.commons.exception;

 import net.ymate.platform.commons.util.ParamUtils;
 import org.apache.commons.lang3.StringUtils;

 import java.util.LinkedHashMap;
 import java.util.Map;

 /**
  * @author 刘镇 (suninformation@163.com) on 2021/01/20 19:48
  * @since 2.1.0
  */
 public class ServiceException extends Exception {

     private final String errorCode;

     /**
      * @since 2.1.3
      */
     private final Map<String, Object> attributes = new LinkedHashMap<>();

     public ServiceException(int errorCode, String message) {
         this(String.valueOf(errorCode), message);
     }

     /**
      * @since 2.1.3
      */
     public ServiceException(String errorCode, String message) {
         super(message);
         this.errorCode = errorCode;
     }

     /**
      * @since 2.1.3
      */
     public ServiceException(int errorCode, Throwable cause) {
         this(String.valueOf(errorCode), cause);
     }

     /**
      * @since 2.1.3
      */
     public ServiceException(String errorCode, Throwable cause) {
         super(cause);
         this.errorCode = errorCode;
     }

     public String getErrorCode() {
         return errorCode;
     }

     /**
      * @since 2.1.3
      */
     public Map<String, Object> getAttributes() {
         return attributes;
     }

     /**
      * @since 2.1.3
      */
     public ServiceException addAttribute(String attrKey, Object attrValue) {
         if (StringUtils.isNotBlank(attrKey) && !ParamUtils.isInvalid(attrValue)) {
             attributes.put(attrKey, attrValue);
         }
         return this;
     }

     /**
      * @since 2.1.3
      */
     public ServiceException addAttributes(Map<String, Object> attributes) {
         this.attributes.putAll(attributes);
         return this;
     }

     @Override
     public String getMessage() {
         return String.format("[%s] %s", errorCode, StringUtils.defaultIfBlank(super.getMessage(), "Service exception occurred."));
     }
 }
