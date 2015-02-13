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

import java.lang.reflect.Type;

/**
 * Thrown when a {@link java.lang.reflect.Type} cannot be mapped to an {@link javax.management.openmbean.OpenType}.
 */
public class InvalidEasyBeanOpenType extends EasyBeanException
{
  /**
   * The type that caused this exception.
   */
  public final Type openTypeClass;

  InvalidEasyBeanOpenType(Type openTypeClass, String message)
  {
    super(message + " (" + openTypeClass.getClass().getName() + ")");
    this.openTypeClass = openTypeClass;
  }
}
