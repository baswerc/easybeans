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

import javax.management.*;
import javax.management.openmbean.*;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.baswell.easybeans.SharedMethods.*;
import static org.baswell.easybeans.OpenTypeMapper.*;
import static org.baswell.easybeans.OpenTypeConverter.*;

/**
 * Wraps a Java object to exposure their attributes and operations as a DynamicMBean. If you want to broadcast JMX notifications
 * use {@link EasyBeanNotificationWrapper}
 *
 */
public class EasyBeanWrapper implements DynamicMBean
{
  Object bean;
  EasyBeanExposure exposure;

  MBeanInfo mBeanInfo;
  ObjectName objectName;

  Map<String, EasyBeanAttributeStructure> readableAttributeStructures;
  Map<String, EasyBeanAttributeStructure> writableAttributeStructures;
  Map<String, List<EasyBeanOperationStructure>> operationStructures;

  /**
   *
   * @param bean The bean to wrap as a DynamicMBean.
   * @throws InvalidEasyBeanNameException If the ObjectName used for this bean in invalid.
   * @throws InvalidEasyBeanAnnotation If an EasyBean annotation is used incorrectly.
   * @throws InvalidEasyBeanOpenType If the given object (or a descendant of this object) cannot be mapped to an OpenType.
   */
  public EasyBeanWrapper(Object bean) throws InvalidEasyBeanNameException, InvalidEasyBeanAnnotation, InvalidEasyBeanOpenType
  {
    this(bean, null);
  }

  /**
   *
   * @param bean The bean to wrap as a DynamicMBean.
   * @param exposure Overrides the EasyBeanExposure annotated by the given bean object.
   * @throws InvalidEasyBeanNameException If the ObjectName used for this bean in invalid.
   * @throws InvalidEasyBeanAnnotation If an EasyBean annotation is used incorrectly.
   * @throws InvalidEasyBeanOpenType If the given object (or a descendant of this object) cannot be mapped to an OpenType.
   */
  public EasyBeanWrapper(Object bean, EasyBeanExposure exposure) throws InvalidEasyBeanNameException, InvalidEasyBeanAnnotation, InvalidEasyBeanOpenType
  {
    this.bean = bean;
    Class clazz = bean.getClass();
    EasyBeanStructure beanStructure = new EasyBeanStructure(clazz);

    String objectNameString = null;
    if (hasContent(beanStructure.objectName))
    {
      objectNameString = beanStructure.objectName;
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
      else if (beanStructure.exposure != null)
      {
        this.exposure = beanStructure.exposure;
      }
      else
      {
        this.exposure = EasyBeanExposure.ANNOTATED;
      }

      OpenMBeanConstructorInfo[] constructorInfo = loadConstructorInfo(beanStructure.constructors);
      OpenMBeanAttributeInfo[] attributeInfo = loadAttributeInfo(beanStructure.attributes);
      OpenMBeanOperationInfo[] opInfo = loadOperationInfo(beanStructure.operations);
      MBeanNotificationInfo[] notificationInfo = loadNotificationInfo();

      mBeanInfo = new OpenMBeanInfoSupport(beanStructure.className, beanStructure.description, attributeInfo, constructorInfo, opInfo, notificationInfo, beanStructure.descriptor);
    }
    catch (MalformedObjectNameException monexc)
    {
      throw new InvalidEasyBeanNameException(clazz, objectNameString, monexc);
    }
  }

  /**
   * Registers this MBean with the platform MBeanServer
   * 
   * @see java.lang.management.ManagementFactory#getPlatformMBeanServer()
   * @throws ObjectNameAlreadyRegistered If the object name of this MBean has already been registered.
   * @throws UnexpectedEasyBeanException If the registration could not occur from some other unexpected reason.
   */
  public void register() throws ObjectNameAlreadyRegistered, UnexpectedEasyBeanException
  {
    register(ManagementFactory.getPlatformMBeanServer());
  }

  /**
   * Registers this MBean with the given MBeanServer.
   *
   * @param mBeanServer The bogbog to register this MBean with.
   * @throws ObjectNameAlreadyRegistered If the object name of this MBean has already been registered.
   * @throws UnexpectedEasyBeanException If the registration could not occur from some other unexpected reason.
   */
  public void register(MBeanServer mBeanServer) throws ObjectNameAlreadyRegistered, UnexpectedEasyBeanException
  {
    try
    {
      mBeanServer.registerMBean(this, objectName);
    }
    catch (InstanceAlreadyExistsException e)
    {
      throw new ObjectNameAlreadyRegistered(e, bean.getClass(), objectName);
    }
    catch (MBeanRegistrationException e)
    {
      throw new UnexpectedEasyBeanException(e);
    }
    catch (NotCompliantMBeanException e)
    {
      throw new UnexpectedEasyBeanException(e);
    }
  }

  /**
   * Unregisters this MBean from the platform MBeanServer
   *
   * @see java.lang.management.ManagementFactory#getPlatformMBeanServer()
   * @throws UnexpectedEasyBeanException If the unregister action could not occur from some other unexpected reason.
   */
  public void unregister() throws UnexpectedEasyBeanException
  {
    unregister(ManagementFactory.getPlatformMBeanServer());
  }

  /**
   * Unregisters this MBean from the given MBeanServer
   * *
   * @param mBeanServer The bogbog to unregister this MBean from.
   * @throws UnexpectedEasyBeanException If the unregister action could not occur from some other unexpected reason.
   */
  public void unregister(MBeanServer mBeanServer) throws UnexpectedEasyBeanException
  {
    try
    {
      mBeanServer.unregisterMBean(objectName);
    }
    catch (InstanceNotFoundException e)
    {}
    catch (MBeanRegistrationException e)
    {
      throw new UnexpectedEasyBeanException(e);
    }
  }

  @Override
  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
  {
    if (!readableAttributeStructures.containsKey(attribute))
    {
      throw new AttributeNotFoundException("No readable attribute found with name '" + attribute + "'.");
    }
    try
    {
      EasyBeanAttributeStructure beanAttribute = readableAttributeStructures.get(attribute);
      Object value = beanAttribute.get(bean);
      return convertToOpenType(value, beanAttribute.typeMapping);
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
    if (operationStructures.containsKey(actionName))
    {
      List<EasyBeanOperationStructure> operations = operationStructures.get(actionName);

      for (EasyBeanOperationStructure operation : operations)
      {
        if (operation.signatureMatches(signature))
        {
          try
          {
            return convertToOpenType(operation.invoke(bean, params), operation.typeMapping);
          }
          catch (Throwable exc)
          {
            if (exc instanceof InvocationTargetException)
            {
              exc = ((InvocationTargetException) exc).getTargetException();
            }

            String line = exc.getStackTrace()[0].toString();
            throw new RuntimeException("Unable to execute operation " + actionName + " due to error: " + exc.getMessage() + " at line: " + line);
          }
        }
      }
    }

    throw new NoSuchElementException("No matching operation found with name " + actionName + " and signature " + signature);
  }

  /**
   * 
   * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
   */
  @Override
  public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
  {
    String attName = attribute.getName();
    if (!writableAttributeStructures.containsKey(attName))
    {
      throw new AttributeNotFoundException("No writeable attribute found with name '" + attName + "'.");
    }
    else
    {
      try
      {
        writableAttributeStructures.get(attName).set(bean, attribute.getValue());
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

  OpenMBeanConstructorInfo[] loadConstructorInfo(List<EasyBeanConstructorStructure> beanConstructors)
  {
    List<OpenMBeanConstructorInfo> constructorsInfo = new ArrayList<OpenMBeanConstructorInfo>();
    
    for (EasyBeanConstructorStructure beanConstructor : beanConstructors)
    {
      if (beanConstructor.wasAnnotated || (exposure == EasyBeanExposure.ALL))
      {
        OpenMBeanParameterInfo[] paramsInfo = getParameterInfo(bean.getClass(), beanConstructor.constructor.getParameterTypes(), beanConstructor.constructor.getParameterAnnotations(), beanConstructor.parameterNames, beanConstructor.parameterDescriptions, null);
        if (paramsInfo != null)
        {
          constructorsInfo.add(new OpenMBeanConstructorInfoSupport(beanConstructor.name, beanConstructor.description, paramsInfo, beanConstructor.descriptor));
        }
      }
    }
    
    return constructorsInfo.toArray(new OpenMBeanConstructorInfo[constructorsInfo.size()]);
  }
  
  OpenMBeanAttributeInfo[] loadAttributeInfo(List<EasyBeanAttributeStructure> beanAttributes)
  {
    readableAttributeStructures = new HashMap<String, EasyBeanAttributeStructure>();
    writableAttributeStructures = new HashMap<String, EasyBeanAttributeStructure>();
    
    List<OpenMBeanAttributeInfo> attributesInfo = new ArrayList<OpenMBeanAttributeInfo>();
    
    for (EasyBeanAttributeStructure beanAttribute : beanAttributes)
    {
      if (beanAttribute.typeMapping != null)
      {
        boolean isReadable = beanAttribute.hasReadAccess() && (beanAttribute.wasReadAnnotated || (exposure != EasyBeanExposure.ANNOTATED));
        boolean isWritable = beanAttribute.hasWriteAccess() && (beanAttribute.wasWriteAnnotated || (exposure == EasyBeanExposure.ALL));

        if (isReadable || isWritable)
        {
          attributesInfo.add(new OpenMBeanAttributeInfoSupport(beanAttribute.name, beanAttribute.description, (OpenType<Long>) beanAttribute.typeMapping.getOpenType(), isReadable, isWritable, beanAttribute.isIs(), beanAttribute.descriptor));

          if (isReadable)
          {
            readableAttributeStructures.put(beanAttribute.name, beanAttribute);
          }

          if (isWritable)
          {
            writableAttributeStructures.put(beanAttribute.name, beanAttribute);
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

  OpenMBeanOperationInfo[] loadOperationInfo(List<EasyBeanOperationStructure> beanOperations)
  {
    operationStructures = new HashMap<String, List<EasyBeanOperationStructure>>();
    List<OpenMBeanOperationInfo> operationsInfo = new ArrayList<OpenMBeanOperationInfo>();
    
    for (EasyBeanOperationStructure beanOperation : beanOperations)
    {
      if (beanOperation.typeMapping != null)
      {
        if (beanOperation.wasAnnotated || (exposure == EasyBeanExposure.ALL))
        {
          OpenMBeanParameterInfo[] paramsInfo = getParameterInfo(bean.getClass(), beanOperation.method.getParameterTypes(), beanOperation.method.getParameterAnnotations(), beanOperation.parameterNames, beanOperation.parameterDescriptions, beanOperation.parameterDefaultValues);
          if (paramsInfo != null)
          {
            operationsInfo.add(new OpenMBeanOperationInfoSupport(beanOperation.name, beanOperation.description, paramsInfo, beanOperation.typeMapping.getOpenType(), beanOperation.impact.getMBeanImpact(), beanOperation.descriptor));

            List<EasyBeanOperationStructure> operationsWithSameName;
            if (operationStructures.containsKey(beanOperation.name))
            {
              operationsWithSameName = operationStructures.get(beanOperation.name);
            }
            else
            {
              operationsWithSameName = new ArrayList<EasyBeanOperationStructure>();
              operationStructures.put(beanOperation.name, operationsWithSameName);
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
  
  static OpenMBeanParameterInfo[] getParameterInfo(Class beanClass, Class<?>[] paramTypes, Annotation[][] annotations, String[] parameterNames, String[] parameterDescriptions, String[] parameterDefaultValues)
  {
    OpenMBeanParameterInfo[] paramsInfo = new OpenMBeanParameterInfo[paramTypes.length];
    
    for (int i = 0; i < paramTypes.length; i++)
    {
      String name = "arg" + i;
      OpenTypeMapping typeMapping = mapToOpenType(paramTypes[i]);
      
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


      String defaultValueString = null;
      if ((parameterDefaultValues != null) && (parameterDefaultValues.length > i))
      {
        defaultValueString = parameterDefaultValues[i];
      }

      List<Pair<String, Object>> descriptorPairs = new ArrayList<Pair<String, Object>>();
      for (Annotation annotation : annotations[i])
      {
        if (annotation instanceof P)
        {
          P jmxParameter = (P)annotation;
          if (jmxParameter.value().trim().length() > 0) name = jmxParameter.value();
          if (jmxParameter.description().trim().length() > 0) description = jmxParameter.description();
          if (jmxParameter.defaultValue().length > 0) defaultValueString = jmxParameter.defaultValue()[0];

          for (EasyBeanDescriptor easyBeanDescriptor : jmxParameter.descriptor())
          {
            int count = Math.min(easyBeanDescriptor.names().length, easyBeanDescriptor.values().length);
            for (int j = 0; j < count; j++)
            {
              descriptorPairs.add(Pair.pair(easyBeanDescriptor.names()[i], (Object)easyBeanDescriptor.values()[i]));
            }
          }
          break;
        }
      }

      if ((defaultValueString != null) && typeMapping.isSimpleType())
      {
        /*
         * We've got no way to specify null with annotations, so if the default parameter value is an empty string
         * and this isn't a String parameter then treat that as null.
         */
        Class simpleClass = typeMapping.getSimpleClass();
        if (hasContent(defaultValueString) || (simpleClass == String.class))
        {
          descriptorPairs.add(Pair.pair("defaultValue", mapSimpleType(defaultValueString, simpleClass)));
        }
      }
      else if (defaultValueString != null)
      {
        throw new InvalidEasyBeanAnnotation(beanClass, "Default value " + defaultValueString + " for parameter " + paramTypes[i].getSimpleName() + " must be a simple type.");
      }
      Descriptor descriptor = descriptorPairs.isEmpty() ? null : getDescriptorFromPairs(descriptorPairs);
      /*
       * The (OpenType<Integer>) cast here is BS to get the compiler not to complain about the constructor being ambiguous.
       */
      paramsInfo[i] = new OpenMBeanParameterInfoSupport(name, description, (OpenType<Integer>)typeMapping.getOpenType(), descriptor);
    }
    
    return paramsInfo;
  }
}
