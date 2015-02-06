package org.baswell.easybeans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.baswell.easybeans.SharedMethods.hasContent;
import static org.baswell.easybeans.OpenTypeMappingCreator.createOpenType;

class BeanOperation extends BeanMember
{
  final Method method;

  final OperationImpact impact;

  final String[] parameterNames;

  final String[] parameterDescriptions;

  BeanOperation(Class clazz, Method method)
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
      impact = oppMeta.impact();
    }
    else
    {
      description = name = method.getName();
      parameterDescriptions = parameterNames = null;
      impact = OperationImpact.UNKNOWN;
    }

    typeMapping = createOpenType(new TypeWrapper(method));
    descriptor = getDescriptor(method);
  }

  void invoke(Object pojo, Object... parameters) throws IllegalAccessException, InvocationTargetException
  {
    method.invoke(pojo, parameters);
  }
}
