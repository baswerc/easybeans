package org.baswell.easybeans;

import javax.management.*;
import javax.management.openmbean.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.baswell.easybeans.SharedMethods.*;
import static org.baswell.easybeans.OpenTypeMappingCreator.*;

/**
 * Wraps a Java object to expose their attributes and operations as a DynamicMBean. If you want to broadcast JMX notifications
 * use {@link EasyBeanNotificationWrapper}
 *
 */
public class EasyBeanWrapper implements DynamicMBean
{
  Object bean;
  EasyBeanExposure exposure;

  MBeanInfo mBeanInfo;
  ObjectName objectName;

  Map<String, BeanAttribute> attributeReadMap;
  Map<String, BeanAttribute> attributeWriteMap;
  Map<String, List<BeanOperation>> operationMap;
  
  OpenTypeMapper openTypeMapper;

  /**
   *
   * @param bean
   * @throws InvalidEasyBeanNameException
   * @throws InvalidEasyBeanAnnotation
   * @throws InvalidEasyBeanOpenType
   */
  public EasyBeanWrapper(Object bean) throws InvalidEasyBeanNameException, InvalidEasyBeanAnnotation, InvalidEasyBeanOpenType
  {
    this(bean, null);
  }

  /**
   *
   * @param bean
   * @param exposure
   * @throws InvalidEasyBeanNameException
   * @throws InvalidEasyBeanAnnotation
   * @throws InvalidEasyBeanOpenType
   */
  public EasyBeanWrapper(Object bean, EasyBeanExposure exposure) throws InvalidEasyBeanNameException, InvalidEasyBeanAnnotation, InvalidEasyBeanOpenType
  {
    this.bean = bean;
    openTypeMapper = new OpenTypeMapper();
    
    Class clazz = bean.getClass();
    String className = clazz.getCanonicalName();
    String description = null;
    EasyBean easyBeanAnnotation = (EasyBean)clazz.getAnnotation(EasyBean.class);

    String objectNameString = null;
    if ((easyBeanAnnotation != null) && !nullEmpty(easyBeanAnnotation.objectName()))
    {
      objectNameString = easyBeanAnnotation.objectName();
    }
    else
    {
      objectNameString = clazz.getPackage().getName() + ":Name=" + clazz.getSimpleName();
    }

    try
    {
      if (bean instanceof EasyBeanNameProvider)
      {
        objectName = ((EasyBeanNameProvider) bean).getObjectName();
        if (objectName == null)
        {
          throw new InvalidEasyBeanNameException(clazz, "Object name cannot be null.", null);
        }
      }
      else
      {
        objectName = new ObjectName(objectNameString);
      }

      if (exposure != null)
      {
        this.exposure = exposure;
      }
      else if (easyBeanAnnotation != null)
      {
        this.exposure = easyBeanAnnotation.expose();
      }
      else
      {
        this.exposure = EasyBeanExposure.ANNOTATED;
      }

      if(easyBeanAnnotation != null)
      {
        description = easyBeanAnnotation.description();
      }

      BeanDefinition beanDefinition = new BeanDefinition(clazz);
      OpenMBeanConstructorInfo[] constructorInfo = loadConstructorInfo(beanDefinition.constructors);
      OpenMBeanAttributeInfo[] attributeInfo = loadAttributeInfo(beanDefinition.attributes);
      OpenMBeanOperationInfo[] opInfo = loadOperationInfo(beanDefinition.operations);
      MBeanNotificationInfo[] notificationInfo = loadNotificationInfo();

      mBeanInfo = new OpenMBeanInfoSupport(className, description, attributeInfo, constructorInfo, opInfo, notificationInfo, beanDefinition.descriptor);
    }
    catch (MalformedObjectNameException monexc)
    {
      throw new InvalidEasyBeanNameException(clazz, objectNameString, monexc);
    }
  }
  
  @Override
  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
  {
    if (!attributeReadMap.containsKey(attribute))
    {
      throw new AttributeNotFoundException("No readable attribute found with name '" + attribute + "'.");
    }
    try
    {
      BeanAttribute beanAttribute = attributeReadMap.get(attribute);
      Object value = beanAttribute.get(bean);
      return openTypeMapper.map(value, beanAttribute.typeMapping);
    }
    catch (Exception exc)
    {
      String line = exc.getStackTrace()[0].toString();
      throw new ReflectionException(exc, "Unable to execute read attribute on " + attribute + " due to error: " + exc.getMessage() + " at line: " + line);    
    }
  }

  @Override
  public AttributeList getAttributes(String[] attributes)
  {
    AttributeList attList = new AttributeList();
    for (String attribute : attributes)
    {
      try
      {
        attList.add(new Attribute(attribute, getAttribute(attribute)));
      }
      catch (Exception exc)
      {}
    }
    
    return attList;
  }

  /**
   * 
   * @see javax.management.DynamicMBean#getMBeanInfo()
   */
  @Override
  public MBeanInfo getMBeanInfo()
  {
    return mBeanInfo;
  }

  /**
   * 
   * @see javax.management.DynamicMBean#invoke(String, Object[], String[])
   */
  @Override
  public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException
  {
    List<BeanOperation> operations = operationMap.get(actionName);

    for (BeanOperation operation : operations)
    {
      Method method = operation.method;
      
      Class<?>[] sigClasses = method.getParameterTypes();
      
      if (sigClasses.length == signature.length)
      {
        boolean matched = true;
        for (int i = 0; i < sigClasses.length; i++)
        {
          if (!classesEquivalent(sigClasses[i], signature[i]))
          {
            matched = false;
            break;
          }
        }
        
        if (matched)
        {
          try
          {
            Object value = method.invoke(bean, params);
            return openTypeMapper.map(value, operation.typeMapping);
          }
          catch (Throwable exc)
          {
            if (exc instanceof InvocationTargetException)
            {
              exc = ((InvocationTargetException)exc).getTargetException();
            }
            
            String line = exc.getStackTrace()[0].toString();
            throw new RuntimeException("Unable to execute operation " + actionName + " due to error: " + exc.getMessage() + " at line: " + line);
          }
        }
      }
    }

    throw new NoSuchElementException("No matching operation found with name " + actionName);
  }

  /**
   * 
   * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
   */
  @Override
  public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
  {
    String attName = attribute.getName();
    if (!attributeWriteMap.containsKey(attName))
    {
      throw new AttributeNotFoundException("No writeable attribute found with name '" + attName + "'.");
    }
    else
    {
      try
      {
        attributeWriteMap.get(attName).set(bean, attribute.getValue());
      }
      catch (Exception exc)
      {
        String line = exc.getStackTrace()[0].toString();
        throw new RuntimeException("Unable to execute write attribute on " + attribute + " due to error: " + exc.getMessage() + " at line: " + line);    
      }
    }
  }

  /**
   * 
   * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
   */
  @Override
  public AttributeList setAttributes(AttributeList attributes)
  {
    AttributeList updateAttributes = new AttributeList();
    
    for (int i = 0; i < attributes.size(); i++)
    {
      Attribute attribute = (Attribute)attributes.get(i);
      String attName = attribute.getName();
      
      try
      {
        setAttribute(attribute);
        updateAttributes.add(new Attribute(attName, getAttribute(attName)));
      }
      catch (Exception exc)
      {}
    }
    
    return updateAttributes;
  }


  MBeanNotificationInfo[] loadNotificationInfo()
  {
    return new MBeanNotificationInfo[0];
  }

  OpenMBeanConstructorInfo[] loadConstructorInfo(List<BeanConstructor> beanConstructors)
  {
    List<OpenMBeanConstructorInfo> constructorsInfo = new ArrayList<OpenMBeanConstructorInfo>();
    
    for (BeanConstructor beanConstructor : beanConstructors)
    {
      if (beanConstructor.wasAnnotated || (exposure == EasyBeanExposure.ALL))
      {
        OpenMBeanParameterInfo[] paramsInfo = getParameterInfo(beanConstructor.constructor.getParameterTypes(), beanConstructor.constructor.getParameterAnnotations(), beanConstructor.parameterNames, beanConstructor.parameterDescriptions);
        if (paramsInfo != null)
        {
          constructorsInfo.add(new OpenMBeanConstructorInfoSupport(beanConstructor.name, beanConstructor.description, paramsInfo, beanConstructor.descriptor));
        }
      }
    }
    
    return constructorsInfo.toArray(new OpenMBeanConstructorInfo[constructorsInfo.size()]);
  }
  
  OpenMBeanAttributeInfo[] loadAttributeInfo(List<BeanAttribute> beanAttributes)
  {
    attributeReadMap = new HashMap<String, BeanAttribute>();
    attributeWriteMap = new HashMap<String, BeanAttribute>();
    
    List<OpenMBeanAttributeInfo> attributesInfo = new ArrayList<OpenMBeanAttributeInfo>();
    
    for (BeanAttribute beanAttribute : beanAttributes)
    {
      if (beanAttribute.typeMapping != null)
      {
        boolean isReadable = beanAttribute.hasReadAccess() && (beanAttribute.wasReadAnnotated || (exposure != EasyBeanExposure.ANNOTATED));
        boolean isWriteable = beanAttribute.hasWriteAccess() && (beanAttribute.wasWriteAnnotated || (exposure == EasyBeanExposure.ALL));

        if (isReadable || isWriteable)
        {
          attributesInfo.add(new OpenMBeanAttributeInfoSupport(beanAttribute.name, beanAttribute.description, (OpenType<Long>) beanAttribute.typeMapping.getOpenType(), isReadable, isWriteable, beanAttribute.isIs(), beanAttribute.descriptor));

          if (isReadable)
          {
            attributeReadMap.put(beanAttribute.name, beanAttribute);
          }

          if (isWriteable)
          {
            attributeWriteMap.put(beanAttribute.name, beanAttribute);
          }
        }
      }
    }

    Collections.sort(attributesInfo, new Comparator<OpenMBeanAttributeInfo>()
    {
      public int compare(OpenMBeanAttributeInfo a1, OpenMBeanAttributeInfo a2)
      {
        return a1.getName().compareTo(a2.getName());
      }
    });
    return attributesInfo.toArray(new OpenMBeanAttributeInfoSupport[attributesInfo.size()]);
  }

  OpenMBeanOperationInfo[] loadOperationInfo(List<BeanOperation> beanOperations)
  {
    operationMap = new HashMap<String, List<BeanOperation>>();
    List<OpenMBeanOperationInfo> operationsInfo = new ArrayList<OpenMBeanOperationInfo>();
    
    for (BeanOperation beanOperation : beanOperations)
    {
      if (beanOperation.typeMapping != null)
      {
        if (beanOperation.wasAnnotated || (exposure == EasyBeanExposure.ALL))
        {
          OpenMBeanParameterInfo[] paramsInfo = getParameterInfo(beanOperation.method.getParameterTypes(), beanOperation.method.getParameterAnnotations(), beanOperation.parameterNames, beanOperation.parameterDescriptions);
          if (paramsInfo != null)
          {
            operationsInfo.add(new OpenMBeanOperationInfoSupport(beanOperation.name, beanOperation.description, paramsInfo, beanOperation.typeMapping.getOpenType(), beanOperation.impact.getMBeanImpact(), beanOperation.descriptor));

            List<BeanOperation> operationsWithSameName;
            if (operationMap.containsKey(beanOperation.name))
            {
              operationsWithSameName = operationMap.get(beanOperation.name);
            }
            else
            {
              operationsWithSameName = new ArrayList<BeanOperation>();
              operationMap.put(beanOperation.name, operationsWithSameName);
            }

            operationsWithSameName.add(beanOperation);
          }
        }
      }
    }
    
    Collections.sort(operationsInfo, new Comparator<OpenMBeanOperationInfo>()
    {
      public int compare(OpenMBeanOperationInfo o1, OpenMBeanOperationInfo o2)
      {
        int compare = o1.getName().compareTo(o2.getName());
        if (compare == 0)
        {
          int numParams1 = o1.getSignature().length;
          int numParams2 = o2.getSignature().length;
          
          if (numParams1 < numParams2)
          {
            return -1;
          }
          else if (numParams1 == numParams2)
          {
            return 0;
          }
          else
          {
            return 1;
          }
        }
        else
        {
          return compare;
        }
      }
    });
    
    return operationsInfo.toArray(new OpenMBeanOperationInfo[operationsInfo.size()]);
  }
  
  static OpenMBeanParameterInfo[] getParameterInfo(Class<?>[] paramTypes, Annotation[][] annotations, String[] parameterNames, String[] parameterDescriptions)
  {
    OpenMBeanParameterInfo[] paramsInfo = new OpenMBeanParameterInfo[paramTypes.length];
    
    for (int i = 0; i < paramTypes.length; i++)
    {
      String name = "arg" + i;
      OpenTypeMapping typeMapping = createOpenType(new EasyBeanOpenTypeWrapper(paramTypes[i]));
      
      if (typeMapping == null)
      {
        return null;
      }

      if ((parameterNames != null) && (parameterNames.length > i))
      {
        name = parameterNames[i];
      }

      String description;
      if ((parameterDescriptions != null) && (parameterDescriptions.length > i))
      {
        description = parameterDescriptions[i];
      }
      else
      {
        description = name;
      }

      for (Annotation annotation : annotations[i])
      {
        if (annotation instanceof P)
        {
          P jmxParameter = (P)annotation;
          if (jmxParameter.value().trim().length() > 0) name = jmxParameter.value();
          if (jmxParameter.description().trim().length() > 0) description = jmxParameter.description();
          
          break;
        }
      }
      
      paramsInfo[i] = new OpenMBeanParameterInfoSupport(name, description, typeMapping.getOpenType());
    }
    
    return paramsInfo;
  }
}
