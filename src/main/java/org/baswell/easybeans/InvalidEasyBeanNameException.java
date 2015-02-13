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

import javax.management.MalformedObjectNameException;

/**
 * Thrown when the {@link #objectName} generated (or provided) for the {@link #beanClass} is invalid.
 */
public class InvalidEasyBeanNameException extends EasyBeanException
{
  public final Class beanClass;

  public final String objectName;

  public InvalidEasyBeanNameException(Class beanClass, String objectName, MalformedObjectNameException cause)
  {
    super(cause);
    this.beanClass = beanClass;
    this.objectName = objectName;
  }
}
