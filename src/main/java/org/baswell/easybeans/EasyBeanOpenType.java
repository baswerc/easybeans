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
 * Metadata for how objects are mapped to {@link javax.management.openmbean.OpenType}.
 */
@Target({ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanOpenType
{
  /**
   * @see javax.management.openmbean.OpenType#getTypeName()
   */
  String name() default "";

  /**
   * @see javax.management.openmbean.OpenType#getDescription()
   */
  String description() default "";

  /**
   * If true the toString method of this annotated object will be used for it's JMX representation (SimpleType<String>).
   */
  boolean exposeAsString() default false;

  /**
   * The type of exposure for this object's (attribute) fields and methods. Defaults to {@link EasyBeanOpenTypeExposure#ALL}.
   */
  EasyBeanOpenTypeExposure exposure() default EasyBeanOpenTypeExposure.ALL;
}
