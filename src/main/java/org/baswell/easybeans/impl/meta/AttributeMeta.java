package org.baswell.easybeans.impl.meta;

import org.baswell.easybeans.impl.OpenTypeMapping;
import org.baswell.easybeans.impl.OpenTypeMappingCreator;
import org.baswell.easybeans.impl.TypeWrapper;
import org.w3c.dom.Attr;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.baswell.easybeans.impl.OpenTypeMappingCreator.*;

public class AttributeMeta
{
  public final String name;

  public final OpenTypeMapping typeMapping;

  private final Class clazz;

  private final Field field;

  private final Method getter;

  private final Method setter;

  public AttributeMeta(Class clazz, Field field)
  {
    assert clazz != null;
    assert field != null;

    this.clazz = clazz;
    this.field = field;

    getter = null;
    setter = null;
    name = field.getName();
    typeMapping = createOpenType(new TypeWrapper(field));
  }

  public AttributeMeta(Class clazz, PropertyDescriptor propertyDescriptor)
  {
    assert clazz != null;
    assert propertyDescriptor != null;

    this.clazz = clazz;
    this.getter = propertyDescriptor.getReadMethod();
    this.setter = propertyDescriptor.getWriteMethod();

    assert (getter != null) || (setter != null);

    typeMapping = createOpenType(new TypeWrapper(getter == null ? setter : getter));
    field = null;
    name = propertyDescriptor.getName();
  }

  public AttributeMeta(Class clazz, Method getter, Method setter, String name)
  {
    assert clazz != null;
    assert (getter != null) || (setter != null);

    this.clazz = clazz;
    this.getter = getter;
    this.setter = setter;
    this.name = name;

    typeMapping = createOpenType(new TypeWrapper(getter == null ? setter : getter));
    field = null;
  }

  public AccessibleObject[] getAccessibleObjects()
  {
    if (field != null)
    {
      return new AccessibleObject[]{field};
    }
    else if (getter != null && setter != null)
    {
      return new AccessibleObject[]{getter, setter};
    }
    else if (getter != null)
    {
      return new AccessibleObject[]{getter};
    }
    else
    {
      return new AccessibleObject[]{setter};
    }
  }

  public boolean isIs()
  {
    return (getter != null) ? getter.getName().startsWith("is") : false;
  }

  public boolean hasReadAccess()
  {
    return (field != null) || (getter != null);
  }

  public boolean hasWriteAccess()
  {
    return (field != null) || (setter != null);
  }

  public <A extends Annotation> A getReadAnnotation(Class<A> annotationClass)
  {
    if (field != null)
    {
      return field.getAnnotation(annotationClass);
    }
    else if (getter != null)
    {
      return getter.getAnnotation(annotationClass);
    }
    else
    {
      return null;
    }
  }

  public <A extends Annotation> A getWriteAnnotation(Class<A> annotationClass)
  {
    if (field != null)
    {
      return field.getAnnotation(annotationClass);
    }
    else if (setter != null)
    {
      return setter.getAnnotation(annotationClass);
    }
    else
    {
      return null;
    }
  }

  public Object get(Object pojo) throws IllegalAccessException, InvocationTargetException
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

  public void set(Object pojo, Object value) throws IllegalAccessException, InvocationTargetException
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
