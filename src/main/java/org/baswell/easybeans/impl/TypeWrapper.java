package org.baswell.easybeans.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

public class TypeWrapper
{
  private Type type;
  private Class rawClass;
  private Field field;
  private Method attributeMethod;
  
  public TypeWrapper(Type type)
  {
    this.type = type;
    rawClass = findRawClass(type);
  }

  public TypeWrapper(Field field)
  {
    this.field = field;
    type = field.getGenericType();
    rawClass = field.getType();
  }
  
  public TypeWrapper(Method attributeMethod)
  {
    this.attributeMethod = attributeMethod;
    
    if (attributeMethod.getReturnType() != null)
    {
      type = attributeMethod.getGenericReturnType();
      rawClass = attributeMethod.getReturnType();
    }
    else
    {
      type = attributeMethod.getGenericParameterTypes()[0];
      rawClass = attributeMethod.getParameterTypes()[0];
    }
  }

  public Type getType()
  {
    return type;
  }
  
  public Class getRawClass()
  {
    return rawClass;
  }

  public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
  {
    T annotation = null;
    
    if (field != null)
    {
      annotation = field.getAnnotation(annotationClass);
    }
    else if (attributeMethod != null)
    {
      annotation = attributeMethod.getAnnotation(annotationClass);
    }
    
    if ((annotation == null) && (rawClass != null))
    {
      annotation = (T)rawClass.getAnnotation(annotationClass);
    }
    
    return annotation;
  }
  
  Class findRawClass(Type type)
  {
    if (type instanceof Class)
    {
      return (Class)type;
    }
    else if (type instanceof GenericArrayType)
    {
      return findRawClass(((GenericArrayType)type).getGenericComponentType());
    }
    else if (type instanceof ParameterizedType)
    {
      return findRawClass(((ParameterizedType)type).getRawType());
    }
    else if (type instanceof WildcardType)
    {
      WildcardType wildCardType = (WildcardType)type;
      if (wildCardType.getLowerBounds().length > 0)
      {
        return findRawClass(wildCardType.getLowerBounds()[0]);
      }
      else if (wildCardType.getUpperBounds().length > 0)
      {
        return findRawClass(wildCardType.getUpperBounds()[0]);
      }
      else
      {
        return null;
      }
    }
    else if (type instanceof TypeVariable)
    {
      TypeVariable typeVar = (TypeVariable)type;
      if (typeVar.getBounds().length > 0)
      {
        return findRawClass(typeVar.getBounds()[0]);
      }
      else
      {
        return null;
      }
    }
    else
    {
      return null;
    }
      
  }
}
