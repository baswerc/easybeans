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

/**
 *
 * The exposure level for the MBean for it's attributes, operations, and constructors.
 * 
 */
public enum EasyBeanExposure
{
  /**
   * Only exposure public fields, methods and constructors annotated with {@link org.baswell.easybeans.EasyBeanAttribute} ,
   * {@link org.baswell.easybeans.EasyBeanOperation} or {@link org.baswell.easybeans.EasyBeanConstructor}.
   */
  ANNOTATED,

  /**
   * Expose any annotated public fields, methods and constructors and any unannotated public fields (read only) and getter methods.
   */
  ANNOTATED_AND_READ_ONLY,

  /**
   * Expose any annotated public fields, methods and constructors and any unannotated public fields (read & write) and methods.
   */
  ALL;
}
