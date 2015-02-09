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
