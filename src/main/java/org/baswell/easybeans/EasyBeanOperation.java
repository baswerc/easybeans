/*
 * Copyright 2015 Corey Baswell
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

/**
 * Metadata for a JMX MBean operation.
 *
 * @see javax.management.openmbean.OpenMBeanOperationInfo
 */
@Target({ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanOperation
{
  /**
   * The JMX operation name. If not provided the name will be created from the {@link java.lang.reflect.Method}.
   *
   * @see javax.management.openmbean.OpenMBeanOperationInfo#getName()
   */
  String name() default "";

  /**
   * Optional description of this operation.
   *
   * @see javax.management.openmbean.OpenMBeanOperationInfo#getDescription()
   */
  String description() default "";

  /**
   * The names of the parameters (in order) for this operation.
   *
   * @see javax.management.openmbean.OpenMBeanParameterInfo#getName()
   */
  String[] parameterNames() default {};

  /**
   * The descriptions of the parameters (in order) for this operation.
   *
   * @see javax.management.openmbean.OpenMBeanParameterInfo#getDescription()
   */
  String[] parameterDescriptions() default {};

  /**
   * The default values of the parameters (in order) for this operation.
   *
   * @see javax.management.openmbean.OpenMBeanParameterInfo#getDefaultValue()
   */
  String[] parameterDefaultValues() default {};

  /**
   * The impact of this operation.
   *
   * @see javax.management.openmbean.OpenMBeanOperationInfo#getImpact()
   */
  OperationImpact impact() default OperationImpact.UNKNOWN;
}
