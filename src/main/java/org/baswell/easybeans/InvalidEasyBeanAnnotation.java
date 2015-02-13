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
 * Thrown when an EasyBeans annotation is used incorrectly. For example you can't public a {@link org.baswell.easybeans.EasyBeanAttribute} on
 * a non-public field.
 */
public class InvalidEasyBeanAnnotation extends EasyBeanException
{
  /**
   * The annotated class that caused this exception.
   */
  public final Class annotatedClass;

  InvalidEasyBeanAnnotation(Class annotateClass, String message)
  {
    super(message + " (" + annotateClass.getCanonicalName() + ")");
    this.annotatedClass = annotateClass;
  }
}
