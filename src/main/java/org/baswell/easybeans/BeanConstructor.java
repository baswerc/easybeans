package org.baswell.easybeans;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.baswell.easybeans.OpenTypeMappingCreator.createOpenType;
import static org.baswell.easybeans.SharedMethods.hasContent;
import static org.baswell.easybeans.SharedMethods.*;

class BeanConstructor extends BeanMember
{
  final Constructor constructor;

  final String[] parameterNames;

  final String[] parameterDescriptions;

  BeanConstructor(Class clazz, Constructor constructor)
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

  @Override
  OpenTypeMapping getTypeMapping()
  {
    return null;
  }

  void newInstance(Object pojo, Object... parameters) throws IllegalAccessException, InvocationTargetException, InstantiationException
  {
    constructor.newInstance(pojo, parameters);
  }
}
