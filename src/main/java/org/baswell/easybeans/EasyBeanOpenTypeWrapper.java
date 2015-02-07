package org.baswell.easybeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import static org.baswell.easybeans.SharedMethods.*;

class EasyBeanOpenTypeWrapper
{
  private String name;

  private String description;

  private Type type;

  private Class rawClass;

  private Field field;

  private Method attributeMethod;
  
  EasyBeanOpenTypeWrapper(Type type)
  {
    this.type = type;
    rawClass = findRawClass(type);
    loadAttributes();
  }

  EasyBeanOpenTypeWrapper(Field field)
  {
    this.field = field;
    type = field.getGenericType();
    rawClass = field.getType();
    loadAttributes();
  }
  
  EasyBeanOpenTypeWrapper(Method attributeMethod)
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
    loadAttributes();
  }

  String getName()
  {
    return name;
  }

  String getDescription()
  {
    return description;
  }

  Type getType()
  {
    return type;
  }
  
  Class getRawClass()
  {
    return rawClass;
  }

  boolean isTransient()
  {
    return getAnnotation(EasyBeanTransient.class) != null;
  }

  private void loadAttributes()
  {
    EasyBeanOpenType annotation = getAnnotation(EasyBeanOpenType.class);

    if (annotation != null)
    {
      name = annotation.name();
      description = annotation.description();

      if (annotation.mappedType() != void.class)
      {
        rawClass = annotation.mappedType();
      }
    }

    if (nullEmpty(name))
    {
      name = rawClass.getSimpleName();
    }

    if (nullEmpty(description))
    {
      description = name;
    }
  }

  private <T extends Annotation> T getAnnotation(Class<T> annotationClass)
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
  
  private Class findRawClass(Type type)
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
        throw new InvalidEasyBeanOpenType(type, "Unable to map WildCardType with no lower or upper bounds.");
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
        throw new InvalidEasyBeanOpenType(type, "Unable to map TypeVariable with no bounds.");
      }
    }
    else
    {
      throw new InvalidEasyBeanOpenType(type, "Unsupported type.");
    }
      
  }
}
