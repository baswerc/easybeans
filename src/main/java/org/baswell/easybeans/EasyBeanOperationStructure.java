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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.baswell.easybeans.SharedMethods.*;
import static org.baswell.easybeans.OpenTypeMapper.*;

/*
 * The structure of an operation. Loads information in EasyBeanOperation.
 */
class EasyBeanOperationStructure extends EasyBeanMemberStructure
{
  final Method method;

  final OperationImpact impact;

  final String[] parameterNames;

  final String[] parameterDescriptions;

  final String[] parameterDefaultValues;

  EasyBeanOperationStructure(Class clazz, Method method)
  {
    super(clazz);

    assert method != null;

    this.method = method;

    EasyBeanOperation oppMeta = method.getAnnotation(EasyBeanOperation.class);
    wasAnnotated = oppMeta != null;

    if (oppMeta != null)
    {
      name = hasContent(oppMeta.name()) ? oppMeta.name() : method.getName();
      description = hasContent(oppMeta.description()) ? oppMeta.description() : name;
      parameterNames = oppMeta.parameterNames();
      parameterDescriptions = oppMeta.parameterDescriptions();
      parameterDefaultValues = oppMeta.parameterDefaultValues();
      impact = oppMeta.impact();
    }
    else
    {
      description = name = method.getName();
      parameterDefaultValues = parameterDescriptions = parameterNames = null;
      impact = OperationImpact.UNKNOWN;
    }

    typeMapping = mapOperationToOpenType(method);
    descriptor = getDescriptor(method);
  }

  boolean signatureMatches(String[] parameterClassNames)
  {
    Class<?>[] sigClasses = method.getParameterTypes();
    if (sigClasses.length == parameterClassNames.length)
    {
      for (int i = 0; i < sigClasses.length; i++)
      {
        if (!classesEquivalent(sigClasses[i], parameterClassNames[i]))
        {
          return false;
        }
      }
      return true;
    }
    else
    {
      return false;
    }
  }

  Object invoke(Object pojo, Object... parameters) throws IllegalAccessException, InvocationTargetException
  {
    return method.invoke(pojo, parameters);
  }
}
