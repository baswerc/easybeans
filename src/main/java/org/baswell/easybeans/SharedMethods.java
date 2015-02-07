package org.baswell.easybeans;

import javax.management.Descriptor;
import javax.management.modelmbean.DescriptorSupport;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    List<EasyBeanDescription> descriptions = new ArrayList<EasyBeanDescription>();

    if ((annotations != null) && (annotations.length > 0))
    {
      for (Annotation annotation : annotations)
      {
        if (annotation instanceof EasyBeanDescription)
        {
          descriptions.add((EasyBeanDescription) annotation);
        }
        else if (annotation instanceof EasyBeanDescriptions)
        {
          EasyBeanDescriptions mbeanDescr = (EasyBeanDescriptions) annotation;
          for (EasyBeanDescription mbeanDesc : mbeanDescr.value())
          {
            descriptions.add(mbeanDesc);
          }
        }
      }
    }

    return getDescriptor(descriptions);
  }

  static Descriptor getDescriptor(List<EasyBeanDescription> descriptions)
  {
    if (descriptions.size() > 0)
    {
      String[] names = new String[descriptions.size()];
      String[] values = new String[descriptions.size()];
      for (int i = 0; i < descriptions.size(); i++)
      {
        EasyBeanDescription easyDesc = descriptions.get(i);
        names[i] = easyDesc.name();
        values[i] = easyDesc.value();
      }

      return new DescriptorSupport(names, values);
    }
    else
    {
      return null;
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
