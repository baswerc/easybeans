package org.baswell.easybeans.impl.meta;

import com.sun.corba.se.spi.orb.Operation;
import org.baswell.easybeans.EasyBeanAttribute;
import org.baswell.easybeans.EasyBeanTransient;
import org.baswell.easybeans.impl.OpenTypeMapping;
import org.baswell.easybeans.impl.TypeWrapper;
import org.w3c.dom.Attr;

import javax.management.openmbean.*;
import java.beans.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ClassMeta
{
  public final List<AttributeMeta> attributes;

  public final List<OperationMeta> operations;

  private final Class clazz;

  public ClassMeta(Class clazz) throws IntrospectionException
  {
    this.clazz = clazz;

    Set<String> attributeNames = new HashSet<String>();

    BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
    List<AttributeMeta> attributes = new ArrayList<AttributeMeta>();
    if (propertyDescriptors != null)
    {
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
      {
        String declaringPackage = propertyDescriptor.getPropertyType().getPackage().getName();
        if (!declaringPackage.startsWith("java.") && !declaringPackage.startsWith("javax."))
        {
          attributes.add(new AttributeMeta(clazz, propertyDescriptor));
          attributeNames.add(propertyDescriptor.getName());
        }
      }
    }

    List<Field> publicFields = getPublicFields(clazz);
    for (Field field : publicFields)
    {
      if (!attributeNames.contains(field.getName()))
      {
        attributes.add(new AttributeMeta(clazz, field));
      }
    }

    List<OperationMeta> operations = new ArrayList<OperationMeta>();
    MethodDescriptor[] methodDescriptors = beanInfo.getMethodDescriptors();
    if (methodDescriptors != null)
    {
      for (MethodDescriptor methodDescriptor : methodDescriptors)
      {
        String declaringPackage = methodDescriptor.getMethod().getDeclaringClass().getPackage().getName();
        if (!declaringPackage.startsWith("java.") && !declaringPackage.startsWith("javax."))
        {
          operations.add(new OperationMeta(clazz, methodDescriptor.getMethod()));
        }
      }
    }

    this.attributes = Collections.unmodifiableList(attributes);
    this.operations = Collections.unmodifiableList(operations);
  }

  static List<Field> getPublicFields(Class clazz)
  {
    List<Field> publicFields = new ArrayList<Field>();
    Field[] fields = clazz.getFields();
    if (fields !=  null)
    {
      for (Field field : fields)
      {
        publicFields.add(field);
      }
    }

    Class superClass = clazz.getSuperclass();
    if ((superClass != null) && (superClass != Object.class))
    {
      publicFields.addAll(getPublicFields(superClass));
    }

    return publicFields;
  }
}
