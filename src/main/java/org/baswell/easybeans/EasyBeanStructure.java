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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.baswell.easybeans.SharedMethods.*;

/*
 * The structure of a class bean. Loads information in EasyBean and keeps track of all it's operations, attributes, and
 * operations.
 */
class EasyBeanStructure
{
  final Class clazz;

  final String className;

  final String objectName;

  final String description;

  final EasyBeanExposure exposure;

  final Descriptor descriptor;

  final List<EasyBeanConstructorStructure> constructors;

  final List<EasyBeanAttributeStructure> attributes;

  final List<EasyBeanOperationStructure> operations;

  EasyBeanStructure(Class clazz) throws InvalidEasyBeanAnnotation
  {
    this.clazz = clazz;

    className = clazz.getCanonicalName();

    EasyBean easyBeanAnnotation = (EasyBean)clazz.getAnnotation(EasyBean.class);
    objectName = (easyBeanAnnotation == null) ? null : easyBeanAnnotation.objectName();
    description = (easyBeanAnnotation == null) ? null : easyBeanAnnotation.description();
    exposure = (easyBeanAnnotation == null) ? null : easyBeanAnnotation.exposure();
    descriptor = (easyBeanAnnotation == null) ? null : getDescriptor(clazz.getAnnotations());


    List<Constructor> publicConstructors = getPublicNonTransientConstructors(clazz);
    List<EasyBeanConstructorStructure> constructors = new ArrayList<EasyBeanConstructorStructure>();
    for (Constructor publicConstructor : publicConstructors)
    {
      constructors.add(new EasyBeanConstructorStructure(clazz, publicConstructor));
    }
    this.constructors = constructors;

    List<Method> allMethods = getAllMethods(clazz);
    List<EasyBeanOperationStructure> operations = new ArrayList<EasyBeanOperationStructure>();
    List<EasyBeanAttributeStructure> attributes = new ArrayList<EasyBeanAttributeStructure>();

    Set<String> attributeNamesCreated = new HashSet<String>();

    for (Method method : allMethods)
    {
      if (Modifier.isPublic(method.getModifiers()))
      {
        if (method.getAnnotation(EasyBeanTransient.class) == null)
        {
          EasyBeanOperation operationMeta = method.getAnnotation(EasyBeanOperation.class);
          EasyBeanAttribute attributeMeta = method.getAnnotation(EasyBeanAttribute.class);

          if ((operationMeta != null) & (attributeMeta != null))
          {
            throw new InvalidEasyBeanAnnotation(clazz, "Method " + method.getName() + " is annotated with EasyBeanAttribute and EasyBeanOperation.");
          }
          else if ((operationMeta == null) && isStandardGetter(method))
          {
            String getterAttName = getGetterSetterName(method.getName());
            if (!attributeNamesCreated.contains(getterAttName.toLowerCase()))
            {
              attributeNamesCreated.add(getterAttName.toLowerCase());
              Method setterMethod = null;
              for (Method anotherPublicMethod : allMethods)
              {
                if (isStandardSetter(anotherPublicMethod) && getGetterSetterName(anotherPublicMethod.getName()).equals(getterAttName))
                {
                  setterMethod = anotherPublicMethod;
                  break;
                }
              }

              attributes.add(new EasyBeanAttributeStructure(clazz, method, setterMethod, getterAttName));
            }
          }
          else if ((operationMeta == null) && isStandardSetter(method))
          {
            String setterAttName = getGetterSetterName(method.getName());
            if (!attributeNamesCreated.contains(setterAttName.toLowerCase()))
            {
              attributeNamesCreated.add(setterAttName.toLowerCase());
              Method getterMethod = null;
              for (Method anotherPublicMethod : allMethods)
              {
                if (isStandardGetter(anotherPublicMethod) && getGetterSetterName(anotherPublicMethod.getName()).equals(setterAttName))
                {
                  getterMethod = anotherPublicMethod;
                  break;
                }
              }

              attributes.add(new EasyBeanAttributeStructure(clazz, getterMethod, method, setterAttName));
            }
          }
          else
          {
            operations.add(new EasyBeanOperationStructure(clazz, method));
          }
        }
        else if (method.getAnnotation(EasyBeanAttribute.class) != null)
        {
          throw new InvalidEasyBeanAnnotation(clazz, "Method " + method.getName() + " is annotated with EasyBeanTransient and EasyBeanAttribute.");
        }
        else if (method.getAnnotation(EasyBeanOperation.class) != null)
        {
          throw new InvalidEasyBeanAnnotation(clazz, "Method " + method.getName() + " is annotated with EasyBeanTransient and EasyBeanOperation.");
        }
      }
      else if (method.getAnnotation(EasyBeanAttribute.class) != null)
      {
        throw new InvalidEasyBeanAnnotation(clazz, "Non-public method " + method.getName() + " is annotated with EasyBeanAttribute.");
      }
      else if (method.getAnnotation(EasyBeanOperation.class) != null)
      {
        throw new InvalidEasyBeanAnnotation(clazz, "Non-public method " + method.getName() + " is annotated with EasyBeanOperation.");
      }
    }

    List<Field> allFields = getAllFields(clazz);
    for (Field field : allFields)
    {
      if (Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()))
      {
        EasyBeanTransient transientAnnotation = field.getAnnotation(EasyBeanTransient.class);
        if (transientAnnotation == null)
        {
          if (!attributeNamesCreated.contains(field.getName().toLowerCase()))
          {
            attributes.add(new EasyBeanAttributeStructure(clazz, field));
          }
        }
        else if (field.getAnnotation(EasyBeanAttribute.class) != null)
        {
          throw new InvalidEasyBeanAnnotation(clazz, "Field " + field.getName() + " is annotated with EasyBeanTransient and EasyBeanAttribute.");
        }
      }
      else if (field.getAnnotation(EasyBeanAttribute.class) != null)
      {
        throw new InvalidEasyBeanAnnotation(clazz, "Non-public field " + field.getName() + " is annotated with EasyBeanAttribute.");
      }
    }

    this.attributes = Collections.unmodifiableList(attributes);
    this.operations = Collections.unmodifiableList(operations);
  }

  static List<Constructor> getPublicNonTransientConstructors(Class clazz)
  {
    List<Constructor> publicConstructors = new ArrayList<Constructor>();
    Constructor[] constructors = clazz.getConstructors();
    if (constructors != null)
    {
      for (Constructor constructor : constructors)
      {
        if (Modifier.isPublic(constructor.getModifiers()) && (constructor.getAnnotation(EasyBeanTransient.class) == null))
        {
          publicConstructors.add(constructor);
        }
      }
    }

    return publicConstructors;
  }
}
