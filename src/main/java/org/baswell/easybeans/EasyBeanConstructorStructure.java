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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.baswell.easybeans.SharedMethods.hasContent;
import static org.baswell.easybeans.SharedMethods.*;

/*
 * The structure of a constructor. Loads information in EasyBeanConstructor.
 */
class EasyBeanConstructorStructure extends EasyBeanMemberStructure
{
  final Constructor constructor;

  final String[] parameterNames;

  final String[] parameterDescriptions;

  EasyBeanConstructorStructure(Class clazz, Constructor constructor)
  {
    super(clazz);

    assert constructor != null;

    this.constructor = constructor;

    EasyBeanConstructor oppMeta = (EasyBeanConstructor)constructor.getAnnotation(EasyBeanConstructor.class);
    wasAnnotated = oppMeta != null;

    if (oppMeta != null)
    {
      name = hasContent(oppMeta.name()) ? oppMeta.name() : constructor.getName();
      description = hasContent(oppMeta.description()) ? oppMeta.description() : name;
      parameterNames = oppMeta.parameterNames();
      parameterDescriptions = oppMeta.parameterDescriptions();
    }
    else
    {
      description = name = constructor.getName();
      parameterDescriptions = parameterNames = null;
    }

    descriptor = getDescriptor(constructor);
  }

  void newInstance(Object pojo, Object... parameters) throws IllegalAccessException, InvocationTargetException, InstantiationException
  {
    constructor.newInstance(pojo, parameters);
  }
}
