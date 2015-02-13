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
import javax.management.modelmbean.DescriptorSupport;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.baswell.easybeans.Pair.*;

class SharedMethods
{
  static boolean nullEmpty(String string)
  {
    return string == null || string.trim().isEmpty();
  }

  static boolean hasContent(String string)
  {
    return string != null && !string.trim().isEmpty();
  }

  static String capatalize(String text)
  {
    if (text.length() > 2)
    {
      return text.substring(0, 1).toUpperCase() + text.substring(1, text.length());
    }
    else
    {
      return text.toUpperCase();
    }
  }

  static Descriptor getDescriptor(Class clazz)
  {
    return getDescriptor(clazz.getClass().getAnnotations());
  }


  static Descriptor getDescriptor(AccessibleObject... accessibleObjects)
  {
    List<Annotation> annotations = new ArrayList<Annotation>();
    for (AccessibleObject accessibleObject : accessibleObjects)
    {
      if (accessibleObject != null)
      {
        Annotation[] annons = accessibleObject.getAnnotations();
        if (annons != null)
        {
          annotations.addAll(Arrays.asList(annons));
        }
      }
    }

    return getDescriptor(annotations.toArray(new Annotation[annotations.size()]));
  }

  static Descriptor getDescriptor(Annotation[] annotations)
  {
    List<EasyBeanDescriptor> descriptors = new ArrayList<EasyBeanDescriptor>();

    if ((annotations != null) && (annotations.length > 0))
    {
      for (Annotation annotation : annotations)
      {
        if (annotation instanceof EasyBeanDescriptor)
        {
          descriptors.add((EasyBeanDescriptor) annotation);
        }
      }
    }

    return getDescriptor(descriptors);
  }

  static Descriptor getDescriptor(List<EasyBeanDescriptor> descriptors)
  {
    List<Pair<String, Object>> descriptions = new ArrayList<Pair<String, Object>>();
    for (EasyBeanDescriptor descriptor : descriptors)
    {
      int numberFields = Math.min(descriptor.names().length, descriptor.values().length);
      for (int i = 0; i < numberFields; i++)
      {
        descriptions.add(pair(descriptor.names()[i], (Object)descriptor.values()[i]));
      }
    }

    return getDescriptorFromPairs(descriptions);
  }

  static Descriptor getDescriptorFromPairs(List<Pair<String, Object>> descriptions)
  {
    if (descriptions.size() > 0)
    {
      String[] names = new String[descriptions.size()];
      Object[] values = new Object[descriptions.size()];
      for (int i = 0; i < descriptions.size(); i++)
      {
        names[i] = descriptions.get(i).x;
        values[i] = descriptions.get(i).y;
      }

      return new DescriptorSupport(names, values);
    }
    else
    {
      return null;
    }
  }

  static boolean isSignatureGetter(Method method)
  {
    return (method.getParameterTypes().length == 0) && (method.getReturnType() != void.class);
  }

  static boolean isStandardGetter(Method method)
  {
    if (isSignatureGetter(method))
    {
      String name = method.getName();
      if ((name.length() > 3) && name.startsWith("get"))
      {
        return true;
      }
      else if ((name.length() > 2) && name.startsWith("is") && ((method.getReturnType() == boolean.class)))
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

  static boolean isSignatureSetter(Method method)
  {
    return (method.getReturnType() == void.class) && (method.getParameterTypes().length == 1);
  }

  static boolean isStandardSetter(Method method)
  {
    String name = method.getName();
    return (name.length() > 3) && name.startsWith("set") && isSignatureSetter(method);
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

  static Object mapSimpleType(String value, Class toType) throws NumberFormatException
  {
    if ((toType == Byte.class) || (toType == byte.class))
    {
      return Byte.valueOf(value);
    }
    else if ((toType == Boolean.class) || (toType == boolean.class))
    {
      return Boolean.valueOf(value);
    }
    else if ((toType == Short.class) || (toType == short.class))
    {
      return Short.valueOf(value);
    }
    else if ((toType == Integer.class) || (toType == int.class))
    {
      return Integer.valueOf(value);
    }
    else if ((toType == Long.class) || (toType == long.class))
    {
      return Long.valueOf(value);
    }
    else if ((toType == Float.class) || (toType == float.class))
    {
      return Float.valueOf(value);
    }
    else if ((toType == Double.class) || (toType == double.class))
    {
      return Double.valueOf(value);
    }
    else
    {
      return value;
    }
  }

  static boolean classesEquivalent(Class<?> clazz, String canonicalName)
  {
    if (clazz.getCanonicalName().equals(canonicalName))
    {
      return true;
    }
    else if (classEquivalentMap.containsKey(clazz.getCanonicalName()) && classEquivalentMap.containsKey(canonicalName))
    {
      return classEquivalentMap.get(clazz.getCanonicalName()) == classEquivalentMap.get(canonicalName);
    }
    else
    {
      return false;
    }
  }

  static private Map<String, Class> classEquivalentMap = new ConcurrentHashMap<String, Class>();
  static
  {
    classEquivalentMap.put(Byte.class.getCanonicalName(), Byte.class);
    classEquivalentMap.put(byte.class.getCanonicalName(), Byte.class);
    classEquivalentMap.put(boolean.class.getCanonicalName(), Boolean.class);
    classEquivalentMap.put(Boolean.class.getCanonicalName(), Boolean.class);
    classEquivalentMap.put(short.class.getCanonicalName(), Short.class);
    classEquivalentMap.put(Short.class.getCanonicalName(), Short.class);
    classEquivalentMap.put(int.class.getCanonicalName(), Integer.class);
    classEquivalentMap.put(Integer.class.getCanonicalName(), Integer.class);
    classEquivalentMap.put(long.class.getCanonicalName(), Long.class);
    classEquivalentMap.put(Long.class.getCanonicalName(), Long.class);
    classEquivalentMap.put(float.class.getCanonicalName(), Float.class);
    classEquivalentMap.put(Float.class.getCanonicalName(), Float.class);
    classEquivalentMap.put(double.class.getCanonicalName(), Double.class);
    classEquivalentMap.put(Double.class.getCanonicalName(), Double.class);
  }
}
