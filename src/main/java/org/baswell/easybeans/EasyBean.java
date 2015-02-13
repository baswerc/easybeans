/*
 * Copyright 2014 Corey Baswell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.management.ObjectName;

/**
 * Metadata for a JMX MBean.
 * 
 */
@Target({ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBean
{
  /**
   * The optional {@link ObjectName} for this easy bean. If not provided the object name will be generated from
   * the annotated object's class name.
   */
  String objectName() default "";

  /**
   * The optional description of this EasyBean.
   */
  String description() default "";
  
  /**
   * The type of exposure for fields, methods and constructors of this easy bean. Defaults to {@link EasyBeanExposure#ANNOTATED}.
   */
  EasyBeanExposure exposure() default EasyBeanExposure.ANNOTATED;
}
