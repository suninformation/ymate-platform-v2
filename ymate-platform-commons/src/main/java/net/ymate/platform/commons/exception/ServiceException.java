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

 import org.apache.commons.lang3.StringUtils;

 /**
  * @author 刘镇 (suninformation@163.com) on 2021/01/20 19:48
  * @since 2.1.0
  */
 public class ServiceException extends Exception {

     private final int errorCode;

     public ServiceException(int errorCode, String message) {
         super(message);
         this.errorCode = errorCode;
     }

     public int getErrorCode() {
         return errorCode;
     }

     @Override
     public String getMessage() {
         return "Service error: [" + errorCode + "] " + StringUtils.trimToEmpty(super.getMessage());
     }
 }
