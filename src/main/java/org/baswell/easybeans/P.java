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
 * Metadata for a MBean constructor or operation parameter. Consider using {@link org.baswell.easybeans.EasyBeanConstructor#parameterNames()} or
 * {@link org.baswell.easybeans.EasyBeanOperation#parameterNames} for better readability. This annotation takes precedence over parameter
 * values in {@link org.baswell.easybeans.EasyBeanConstructor} or {@link org.baswell.easybeans.EasyBeanOperation}.
 *
 */
@Target({ElementType.PARAMETER})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface P
{
  /**
   * The name of this parameter.
   */
  String value() default "";

  /**
   * The description of this parameter.
   */
  String description() default "";

  /**
   * The default value for this parameter. Only the first element of the array will be used (can't specify null with annotations).
   */
  String[] defaultValue() default {};

  /**
   * @see javax.management.MBeanFeatureInfo#getDescriptor()
   */
  EasyBeanDescriptor[] descriptor() default {};
}
