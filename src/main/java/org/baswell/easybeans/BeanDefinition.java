package org.baswell.easybeans;

import javax.management.Descriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.baswell.easybeans.SharedMethods.*;

class BeanDefinition
{
  final Class clazz;

  final Descriptor descriptor;

  final List<BeanConstructor> constructors;

  final List<BeanAttribute> attributes;

  final List<BeanOperation> operations;

  BeanDefinition(Class clazz) throws InvalidEasyBeanAnnotation
  {
    this.clazz = clazz;

    EasyBean mbeanAnnotation = (EasyBean)clazz.getAnnotation(EasyBean.class);
    descriptor = (mbeanAnnotation == null) ? null : getDescriptor(clazz.getAnnotations());


    List<Constructor> publicConstructors = getPublicNonTransientConstructors(clazz);
    List<BeanConstructor> constructors = new ArrayList<BeanConstructor>();
    for (Constructor publicConstructor : publicConstructors)
    {
      constructors.add(new BeanConstructor(clazz, publicConstructor));
    }
    this.constructors = constructors;

    List<Method> allMethods = getAllMethods(clazz);
    List<BeanOperation> operations = new ArrayList<BeanOperation>();
    List<BeanAttribute> attributes = new ArrayList<BeanAttribute>();

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

              attributes.add(new BeanAttribute(clazz, method, setterMethod, getterAttName));
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

              attributes.add(new BeanAttribute(clazz, getterMethod, method, setterAttName));
            }
          }
          else
          {
            operations.add(new BeanOperation(clazz, method));
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
      if (Modifier.isPublic(field.getModifiers()))
      {
        EasyBeanTransient transientAnnotation = field.getAnnotation(EasyBeanTransient.class);
        if (transientAnnotation == null)
        {
          if (!attributeNamesCreated.contains(field.getName().toLowerCase()))
          {
            attributes.add(new BeanAttribute(clazz, field));
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

  static boolean isStandardGetter(Method method)
  {
    String name = method.getName();
    if (method.getParameterTypes().length == 0)
    {
      if ((name.length() > 3) && name.startsWith("get") && (method.getReturnType() != void.class))
      {
        return true;
      }
      else if ((name.length() > 2) && name.startsWith("is") && ((method.getReturnType() == boolean.class) || (method.getReturnType() == Boolean.class)))
      {
        return true;
      }
      else
      {
        return false;
      }
    }
    else
    {
      return false;
    }
  }

  static boolean isStandardSetter(Method method)
  {
    String name = method.getName();
    return ((name.length() > 3) && name.startsWith("set") && (method.getReturnType() == void.class) && (method.getParameterTypes().length == 1));
  }

  static String getGetterSetterName(String methodName)
  {
    if ((methodName.startsWith("get") || methodName.startsWith("set")) && (methodName.length() > 3))
    {
      return methodName.substring(3, methodName.length());
    }
    else if (methodName.startsWith("is") && methodName.length() > 2)
    {
      return methodName.substring(2, methodName.length());
    }
    else
    {
      return methodName;
    }
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

  static List<Field> getAllFields(Class clazz)
  {
    List<Field> allFields = new ArrayList<Field>();
    Field[] fields = clazz.getDeclaredFields();
    if (fields !=  null)
    {
      for (Field field : fields)
      {
        allFields.add(field);
      }
    }

    Class superClass = clazz.getSuperclass();
    if (superClass != null)
    {
      String declaringPackage = superClass.getPackage().getName();
      if (!declaringPackage.startsWith("java.") && !declaringPackage.startsWith("javax."))
      {
        allFields.addAll(getAllFields(superClass));
      }
    }

    return allFields;
  }

  static List<Method> getAllMethods(Class clazz)
  {
    List<Method> allMethods = new ArrayList<Method>();
    Method[] methods = clazz.getDeclaredMethods();
    if (methods != null)
    {
      for (Method method : methods)
      {
        allMethods.add(method);
      }
    }

    Class superClass = clazz.getSuperclass();
    if (superClass != null)
    {
      String declaringPackage = superClass.getPackage().getName();
      if (!declaringPackage.startsWith("java.") && !declaringPackage.startsWith("javax."))
      {
        allMethods.addAll(getAllMethods(superClass));
      }
    }

    return allMethods;
  }
}
