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

  private final boolean writeable;

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

    if (Modifier.isFinal(field.getModifiers()))
    {
      writeable = false;
    }
    else if (attMeta != null)
    {
      writeable = !attMeta.readOnly();
    }
    else
    {
      writeable = true;
    }

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

    if (setter == null)
    {
      writeable = false;
    }
    else
    {
      boolean readOnly = getterMeta != null && getterMeta.readOnly();
      readOnly = readOnly || (setterMeta != null && setterMeta.readOnly());
      writeable = !readOnly;
    }
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
    return writeable;
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
