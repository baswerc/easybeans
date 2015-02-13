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
 * The exposure level for an object's attributes.
 * 
 */
public enum EasyBeanOpenTypeExposure
{
  /**
   * Only exposure the data in public fields and getter methods and constructors annotated with {@link org.baswell.easybeans.EasyBeanOpenTypeAttribute}.
   */
  ANNOTATED,

  /**
   * Expose the data in all public fields and getter methods.
   */
  ALL;
}
