package org.baswell.easybeans;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.baswell.easybeans.OpenTypeMapper.mapAttributeToOpenType;
import static org.baswell.easybeans.OpenTypeMapper.mapToOpenType;
import static org.baswell.easybeans.SharedMethods.*;

/*
 * The structure of an attribute. Loads information in EasyBeanAttribute and keeps track if attribute references Field
 * or Method.
 */
class EasyBeanAttributeStructure extends EasyBeanMemberStructure
{
  final boolean wasReadAnnotated;

  final boolean wasWriteAnnotated;

  private final Field field;

  private final Method getter;

  private final Method setter;

  EasyBeanAttributeStructure(Class clazz, Field field)
  {
    super(clazz);

    assert field != null;

    this.field = field;

    getter = null;
    setter = null;

    EasyBeanAttribute attMeta = field.getAnnotation(EasyBeanAttribute.class);
    wasWriteAnnotated = wasReadAnnotated = wasAnnotated = attMeta != null;

    if (wasAnnotated)
    {
      name = hasContent(attMeta.name()) ? attMeta.name() : capatalize(field.getName());
      description = hasContent(attMeta.description()) ? attMeta.description() : name;
    }
    else
    {
      description = name = capatalize(field.getName());
    }

    typeMapping = mapToOpenType(field);
    descriptor = getDescriptor(field);
  }

  EasyBeanAttributeStructure(Class clazz, Method getter, Method setter, String getterSetterName)
  {
    super(clazz);

    assert (getter != null) || (setter != null);

    this.getter = getter;
    this.setter = setter;

    EasyBeanAttribute getterMeta = getter == null ? null : getter.getAnnotation(EasyBeanAttribute.class);
    EasyBeanAttribute setterMeta = setter == null ? null : setter.getAnnotation(EasyBeanAttribute.class);

    wasReadAnnotated = getterMeta != null;
    wasWriteAnnotated = setterMeta != null;
    wasAnnotated = wasReadAnnotated || wasWriteAnnotated;

    if (wasReadAnnotated)
    {
      if (hasContent(getterMeta.name()))
      {
        name = getterMeta.name();
      }
      else
      {
        name = wasWriteAnnotated && hasContent(setterMeta.name()) ? setterMeta.name() : getterSetterName;
      }

      if (hasContent(getterMeta.description()))
      {
        description = getterMeta.description();
      }
      else
      {
        description = wasWriteAnnotated && hasContent(setterMeta.description()) ? setterMeta.description() : getterSetterName;
      }
    }
    else
    {
      name = wasWriteAnnotated && hasContent(setterMeta.name()) ? setterMeta.name() : getterSetterName;
      description = wasWriteAnnotated && hasContent(setterMeta.description()) ? setterMeta.description() : getterSetterName;
    }

    field = null;
    typeMapping = mapAttributeToOpenType(getter != null ? getter : setter);
    descriptor = getDescriptor(getter, setter);
  }

  boolean isIs()
  {
    return (getter != null) ? getter.getName().startsWith("is") : false;
  }

  boolean hasReadAccess()
  {
    return (field != null) || (getter != null);
  }

  boolean hasWriteAccess()
  {
    if (field != null)
    {
      return !Modifier.isFinal(field.getModifiers());
    }
    else
    {
      return setter != null;
    }
  }

  Object get(Object pojo) throws IllegalAccessException, InvocationTargetException
  {
    if (field != null)
    {
      return field.get(pojo);
    }
    else if (getter != null)
    {
      return getter.invoke(pojo);
    }
    else
    {
      return null;
    }
  }

  void set(Object pojo, Object value) throws IllegalAccessException, InvocationTargetException
  {
    if (field != null)
    {
      field.set(pojo, value);
    }
    else if (setter != null)
    {
      setter.invoke(pojo, value);
    }
  }
}
