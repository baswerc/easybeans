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
