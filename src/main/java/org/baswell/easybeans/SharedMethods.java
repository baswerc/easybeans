package org.baswell.easybeans;

import javax.management.Descriptor;
import javax.management.modelmbean.DescriptorSupport;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

}
