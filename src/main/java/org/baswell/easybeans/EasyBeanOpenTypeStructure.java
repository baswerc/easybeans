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

import javax.management.Descriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.baswell.easybeans.SharedMethods.*;

/*
 * The structure of an object being mapped to an OpenType.
 */
class EasyBeanOpenTypeStructure
{
  private String name;

  private String description;

  private EasyBeanOpenTypeExposure exposure;

  private Descriptor descriptor;

  private Type type;

  private Class rawClass;

  private Field field;

  private Method method;

  private Method getter;

  private Method setter;

  EasyBeanOpenTypeStructure(Type type)
  {
    this.type = type;
    rawClass = findRawClass(type);
    descriptor = SharedMethods.getDescriptor(rawClass);
    loadAttributes();
  }

  EasyBeanOpenTypeStructure(Field field)
  {
    this.field = field;
    type = field.getGenericType();
    rawClass = field.getType();
    descriptor = SharedMethods.getDescriptor(field);
    loadAttributes();
  }

  EasyBeanOpenTypeStructure(Method method, boolean attribute)
  {
    this.method = method;

    if (attribute)
    {
      if (method.getParameterTypes().length == 0)
      {
        type = method.getGenericReturnType();
        rawClass = method.getReturnType();
        getter = method;
      }
      else
      {
        type = method.getGenericParameterTypes()[0];
        rawClass = method.getParameterTypes()[0];
        setter = method;
      }
    }
    else
    {
      type = method.getGenericReturnType();
      rawClass = method.getReturnType();
    }
    descriptor = SharedMethods.getDescriptor(method);
    loadAttributes();
  }


  /*
   * Need to lazily evaludate this so we don't get stock in infinite loop if a class has its own class as a member.
   * OpenTypeMapper#mapCompositeType will take care of self referencing classes.
   */
  List<EasyBeanOpenTypeStructure> getAttributes()
  {
    List<Method> allMethods = getAllMethods(rawClass);
    List<EasyBeanOpenTypeStructure> attributes = new ArrayList<EasyBeanOpenTypeStructure>();
    Set<String> attributeNamesCreated = new HashSet<String>();

    for (Method method : allMethods)
    {
      if (Modifier.isPublic(method.getModifiers()))
      {
        EasyBeanOpenTypeAttribute attributeMeta = method.getAnnotation(EasyBeanOpenTypeAttribute.class);

        if (method.getAnnotation(EasyBeanTransient.class) == null)
        {
          if ((attributeMeta != null) || ((exposure == EasyBeanOpenTypeExposure.ALL) && isStandardGetter(method)))
          {
            if (!isSignatureGetter(method))
            {
              throw new InvalidEasyBeanAnnotation(rawClass, "Method " + method.getName() + " is annotated with EasyBeanOpenTypeAttribute but does not have a getter signature.");
            }

            attributes.add(new EasyBeanOpenTypeStructure(method, true));
          }
        }
        else if (method.getAnnotation(EasyBeanOpenTypeAttribute.class) != null)
        {
          throw new InvalidEasyBeanAnnotation(rawClass, "Method " + method.getName() + " is annotated with EasyBeanTransient and EasyBeanOpenTypeAttribute.");
        }
      }
      else if (method.getAnnotation(EasyBeanOpenTypeAttribute.class) != null)
      {
        throw new InvalidEasyBeanAnnotation(rawClass, "Non-public method " + method.getName() + " is annotated with EasyBeanOpenTypeAttribute.");
      }
    }

    List<Field> allFields = getAllFields(rawClass);
    for (Field field : allFields)
    {
      if (Modifier.isPublic(field.getModifiers()))
      {
        if (field.getAnnotation(EasyBeanTransient.class) == null)
        {
          if (!attributeNamesCreated.contains(field.getName().toLowerCase()))
          {
            if ((exposure == EasyBeanOpenTypeExposure.ALL) || (field.getAnnotation(EasyBeanOpenTypeAttribute.class) != null))
            {
              attributes.add(new EasyBeanOpenTypeStructure(field));
            }
          }
        }
        else if (field.getAnnotation(EasyBeanOpenTypeAttribute.class) != null)
        {
          throw new InvalidEasyBeanAnnotation(rawClass, "Field " + field.getName() + " is annotated with EasyBeanTransient and EasyBeanOpenTypeAttribute.");
        }
      }
      else if (field.getAnnotation(EasyBeanOpenTypeAttribute.class) != null)
      {
        throw new InvalidEasyBeanAnnotation(rawClass, "Non-public field " + field.getName() + " is annotated with EasyBeanOpenTypeAttribute.");
      }
    }

    return attributes;
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

  public Descriptor getDescriptor()
  {
    return descriptor;
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

  private void loadAttributes()
  {
    EasyBeanOpenType annotation = getAnnotation(EasyBeanOpenType.class);

    if (annotation != null)
    {
      name = annotation.name();
      description = annotation.description();
      exposure = annotation.exposure();

      if (annotation.exposeAsString())
      {
        rawClass = String.class;
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
    else if (method != null)
    {
      annotation = method.getAnnotation(annotationClass);
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
        throw new InvalidEasyBeanOpenType(type, "Unable to convertToOpenType WildCardType with no lower or upper bounds.");
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
        throw new InvalidEasyBeanOpenType(type, "Unable to convertToOpenType TypeVariable with no bounds.");
      }
    }
    else
    {
      throw new InvalidEasyBeanOpenType(type, "Unsupported type.");
    }
  }
}
