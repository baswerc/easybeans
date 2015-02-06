package org.baswell.easybeans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class BeanDefinition
{
  public final List<BeanConstructor> constructors;

  public final List<BeanAttribute> attributes;

  public final List<BeanOperation> operations;

  private final Class clazz;

  public BeanDefinition(Class clazz) throws UneasyBeanException
  {
    this.clazz = clazz;

    List<Constructor> publicConstructors = getPublicNonTransientConstructors(clazz);
    List<BeanConstructor> constructors = new ArrayList<BeanConstructor>();
    for (Constructor publicConstructor : publicConstructors)
    {
      constructors.add(new BeanConstructor(clazz, publicConstructor));
    }
    this.constructors = constructors;

    List<Method> publicMethods = getPublicNonTransientMethods(clazz);
    List<BeanOperation> operations = new ArrayList<BeanOperation>();
    List<BeanAttribute> attributes = new ArrayList<BeanAttribute>();

    Set<String> attributeNamesCreated = new HashSet<String>();

    for (Method publicMethod : publicMethods)
    {
      if (isStandardGetter(publicMethod))
      {
        String getterAttName = getGetterSetterName(publicMethod.getName());
        if (!attributeNamesCreated.contains(getterAttName.toLowerCase()))
        {
          attributeNamesCreated.add(getterAttName.toLowerCase());
          Method setterMethod = null;
          for (Method anotherPublicMethod : publicMethods)
          {
            if (isStandardSetter(anotherPublicMethod) && getGetterSetterName(anotherPublicMethod.getName()).equals(getterAttName))
            {
              setterMethod = anotherPublicMethod;
              break;
            }
          }

          attributes.add(new BeanAttribute(clazz, publicMethod, setterMethod, getterAttName));
        }
      }
      else if (isStandardSetter(publicMethod))
      {
        String setterAttName = getGetterSetterName(publicMethod.getName());
        if (!attributeNamesCreated.contains(setterAttName.toLowerCase()))
        {
          attributeNamesCreated.add(setterAttName.toLowerCase());
          Method getterMethod = null;
          for (Method anotherPublicMethod : publicMethods)
          {
            if (isStandardGetter(anotherPublicMethod) && getGetterSetterName(anotherPublicMethod.getName()).equals(setterAttName))
            {
              getterMethod = anotherPublicMethod;
              break;
            }
          }

          attributes.add(new BeanAttribute(clazz, getterMethod, publicMethod, setterAttName));
        }
      }
      else
      {
        operations.add(new BeanOperation(clazz, publicMethod));
      }
    }

    List<Field> publicFields = getPublicNonTransientFields(clazz);
    for (Field field : publicFields)
    {
      if (!attributeNamesCreated.contains(field.getName().toLowerCase()))
      {
        attributes.add(new BeanAttribute(clazz, field));
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

  static List<Field> getPublicNonTransientFields(Class clazz)
  {
    List<Field> publicFields = new ArrayList<Field>();
    Field[] fields = clazz.getFields();
    if (fields !=  null)
    {
      for (Field field : fields)
      {
        if (field.getAnnotation(EasyBeanTransient.class) == null)
        {
        publicFields.add(field);
        }
      }
    }

    Class superClass = clazz.getSuperclass();
    if (superClass != null)
    {
      String declaringPackage = superClass.getPackage().getName();
      if (!declaringPackage.startsWith("java.") && !declaringPackage.startsWith("javax."))
      {
        publicFields.addAll(getPublicNonTransientFields(superClass));
      }
    }

    return publicFields;
  }

  static List<Method> getPublicNonTransientMethods(Class clazz)
  {
    List<Method> publicMethods = new ArrayList<Method>();
    Method[] methods = clazz.getDeclaredMethods();
    if (methods != null)
    {
      for (Method method : methods)
      {
        if (Modifier.isPublic(method.getModifiers()) && (method.getAnnotation(EasyBeanTransient.class) == null))
        {
          publicMethods.add(method);
        }
      }
    }

    Class superClass = clazz.getSuperclass();
    if (superClass != null)
    {
      String declaringPackage = superClass.getPackage().getName();
      if (!declaringPackage.startsWith("java.") && !declaringPackage.startsWith("javax."))
      {
        publicMethods.addAll(getPublicNonTransientMethods(superClass));
      }
    }

    return publicMethods;
  }
}
