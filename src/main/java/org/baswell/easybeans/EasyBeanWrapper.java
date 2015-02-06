package org.baswell.easybeans;

import java.beans.IntrospectionException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;

/**
 * Wraps plain Java objects to expose their attributes and operations as a DynamicMBean.
 *
 */
public class EasyBeanWrapper implements DynamicMBean, NotificationEmitter
{
  static Map<String, Class> classEquivalentMap = new ConcurrentHashMap<String, Class>();
  static
  {
    classEquivalentMap.put(Byte.class.getCanonicalName(), Byte.class);
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

  Object pojo;
  EasyBeanExposureLevel exposeLevel;

  MBeanInfo info;
  ObjectName objectName;
  List<NotificationListenerEntry> listenerEntries;

  Map<String, BeanAttribute> attributeReadMap;
  Map<String, BeanAttribute> attributeWriteMap;
  Map<String, List<BeanOperation>> operationMap;
  
  OpenTypeMappingCreator openTypeMappingCreator;
  OpenTypeMapper openTypeMapper;
  
  /**
   * 
   * @param pojo The annotated object to make JMX accessible.
   * @throws EasyBeanDefinitionException If the {@link ObjectName} defined in {@link EasyBean} is invalid.
   */
  public EasyBeanWrapper(Object pojo) throws EasyBeanDefinitionException
  {
    this.pojo = pojo;
    openTypeMappingCreator = new OpenTypeMappingCreator();
    openTypeMapper = new OpenTypeMapper();
    
    Class clazz = pojo.getClass();
    String className = clazz.getCanonicalName();
    String mbeanDescription = null;
    EasyBean mbeanAnnotation = (EasyBean)clazz.getAnnotation(EasyBean.class);
    
    try
    {
      if (pojo instanceof EasyBeanNameProvider)
      {
        objectName = ((EasyBeanNameProvider)pojo).getObjectName();
      }
      else if ((mbeanAnnotation != null) && !nullEmpty(mbeanAnnotation.objectName()))
      {
        objectName = new ObjectName(mbeanAnnotation.objectName());
      }
      else
      {
        objectName = new ObjectName(clazz.getPackage().getName() + ":Name=" + clazz.getSimpleName());
      }
      
      if(mbeanAnnotation != null)
      {
        mbeanDescription = mbeanAnnotation.description();
        exposeLevel = mbeanAnnotation.exposeLevel();
      }
      else
      {
        exposeLevel = EasyBeanExposureLevel.ANNOTATED;
      }

      BeanDefinition beanDefinition = new BeanDefinition(clazz);
      OpenMBeanConstructorInfo[] constructorInfo = loadConstructorInfo(beanDefinition.constructors);
      OpenMBeanAttributeInfo[] attributeInfo = loadAttributeInfo(beanDefinition.attributes);
      OpenMBeanOperationInfo[] opInfo = loadOperationInfo(beanDefinition.operations);
      MBeanNotificationInfo[] notificationInfo = loadNotificationInfo(clazz);
      Descriptor descriptor = (mbeanAnnotation == null) ? null : getDescriptor(clazz.getAnnotations());
      
      info = new OpenMBeanInfoSupport(className, mbeanDescription, attributeInfo, constructorInfo, opInfo, notificationInfo, descriptor);
    }
    catch (MalformedObjectNameException monexc)
    {
      throw new EasyBeanDefinitionException(monexc);
    }
  }
  
  public ObjectName getObjectName()
  {
    return objectName;
  }

  /**
   * 
   * @see javax.management.DynamicMBean#getAttribute(String)
   */
  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
  {
    if (!attributeReadMap.containsKey(attribute))
    {
      throw new AttributeNotFoundException("No readable attribute found with name '" + attribute + "'.");
    }
    try
    {
      BeanAttribute beanAttribute = attributeReadMap.get(attribute);
      Object value = beanAttribute.get(pojo);
      return openTypeMapper.map(value, beanAttribute.typeMapping);
    }
    catch (Exception exc)
    {
      String line = exc.getStackTrace()[0].toString();
      throw new ReflectionException(exc, "Unable to execute read attribute on " + attribute + " due to error: " + exc.getMessage() + " at line: " + line);    
    }
  }

  /**
   * 
   * @see javax.management.DynamicMBean#getAttributes(String[])
   */
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
  public MBeanInfo getMBeanInfo()
  {
    return info;
  }

  /**
   * 
   * @see javax.management.DynamicMBean#invoke(String, Object[], String[])
   */
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
            Object value = method.invoke(pojo, params);
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
        attributeWriteMap.get(attName).set(pojo, attribute.getValue());
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

  /**
   * 
   * @see NotificationEmitter#getNotificationInfo()
   */
  public MBeanNotificationInfo[] getNotificationInfo()
  {
    if (pojo instanceof NotificationBroadcaster)
    {
      return ((NotificationBroadcaster) pojo).getNotificationInfo();
    }
    else
    {
      return info.getNotifications();
    }
  }

  /**
   * 
   * @see NotificationEmitter#addNotificationListener(NotificationListener, NotificationFilter, Object)
   */
  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
  {
    if (pojo instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster) pojo).addNotificationListener(listener, filter, handback);
    }
    else
    {
      listenerEntries.add(new NotificationListenerEntry(listener, filter, handback));
    }
  }

  /**
   * 
   * @see NotificationEmitter#removeNotificationListener(NotificationListener, NotificationFilter, Object)
   */
  public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
  {
    if (pojo instanceof NotificationEmitter)
    {
      ((NotificationEmitter) pojo).removeNotificationListener(listener, filter, handback);
    }
    else if (pojo instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster) pojo).removeNotificationListener(listener);
    }
    else
    {
      for (int i = (listenerEntries.size() - 1); i >= 0; i--)
      {
        NotificationListenerEntry listenerEntry = (NotificationListenerEntry)listenerEntries.get(i);
        if (listenerEntry.equals(listener, filter, handback))
        {
          listenerEntries.remove(i);
        }
      }
    }
  }

  /**
   * 
   * @see NotificationEmitter#removeNotificationListener(NotificationListener)
   */
  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
  {
    if (pojo instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster) pojo).removeNotificationListener(listener);
    }
    else
    {
      for (int i = (listenerEntries.size() - 1); i >= 0; i--)
      {
        NotificationListenerEntry listenerEntry = (NotificationListenerEntry)listenerEntries.get(i);
        if (listenerEntry.equals(listener))
        {
          listenerEntries.remove(i);
        }
      }
    }
  }

  OpenMBeanConstructorInfo[] loadConstructorInfo(List<BeanConstructor> beanConstructors)
  {
    List<OpenMBeanConstructorInfo> constructorsInfo = new ArrayList<OpenMBeanConstructorInfo>();
    
    for (BeanConstructor beanConstructor : beanConstructors)
    {
      if (beanConstructor.wasAnnotated || (exposeLevel == EasyBeanExposureLevel.ALL))
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
        boolean isReadable = beanAttribute.hasReadAccess() && (beanAttribute.wasReadAnnotated || (exposeLevel != EasyBeanExposureLevel.ANNOTATED));
        boolean isWriteable = beanAttribute.hasWriteAccess() && (beanAttribute.wasWriteAnnotated || (exposeLevel == EasyBeanExposureLevel.ALL));

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
        if (beanOperation.wasAnnotated || (exposeLevel == EasyBeanExposureLevel.ALL))
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
  
  MBeanNotificationInfo[] loadNotificationInfo(Class clazz)
  {
    if (pojo instanceof NotificationBroadcaster)
    {
      return ((NotificationBroadcaster) pojo).getNotificationInfo();
    }
    else
    {
      Annotation[] annotations = clazz.getAnnotations();
      List<MBeanNotificationInfo> notificationInfoList = new ArrayList<MBeanNotificationInfo>();
      
      for (Annotation annotation : annotations)
      {
        if (annotation instanceof EasyBeanNotification)
        {
          EasyBeanNotification notificationMeta = (EasyBeanNotification)annotation;
          String name = notificationMeta.name();
          String description = notificationMeta.description();
          String[] notifyTypes = notificationMeta.types();
          notificationInfoList.add(new ModelMBeanNotificationInfo(notifyTypes, name, description, getDescriptor(Arrays.asList(notificationMeta.descriptor()))));
        }
        else if (annotation instanceof EasyBeanNotifications)
        {
          for (EasyBeanNotification notificationMeta : ((EasyBeanNotifications)annotation).value())
          {
            String name = notificationMeta.name();
            String description = notificationMeta.description();
            String[] notifyTypes = notificationMeta.types();
            notificationInfoList.add(new ModelMBeanNotificationInfo(notifyTypes, name, description, getDescriptor(Arrays.asList(notificationMeta.descriptor()))));
          }
        }
      }
      
      listenerEntries = new ArrayList<NotificationListenerEntry>();
      return notificationInfoList.toArray(new ModelMBeanNotificationInfo[notificationInfoList.size()]);
    }
  }
  

  OpenMBeanParameterInfo[] getParameterInfo(Class<?>[] paramTypes, Annotation[][] annotations, String[] parameterNames, String[] parameterDescriptions)
  {
    OpenMBeanParameterInfo[] paramsInfo = new OpenMBeanParameterInfo[paramTypes.length];
    
    for (int i = 0; i < paramTypes.length; i++)
    {
      String name = "arg" + i;
      OpenTypeMapping typeMapping = openTypeMappingCreator.createOpenType(new TypeWrapper(paramTypes[i]));
      
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

  Descriptor getDescriptor(AccessibleObject... accessibleObjects)
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
  
  Descriptor getDescriptor(Annotation[] annotations)
  {
    List<EasyBeanDescription> descriptions = new ArrayList<EasyBeanDescription>();
    
    if ((annotations != null) && (annotations.length > 0))
    {
      for (Annotation annotation : annotations)
      {
        if (annotation instanceof EasyBeanDescription)
        {
          descriptions.add((EasyBeanDescription)annotation);
        }
        else if (annotation instanceof EasyBeanDescriptions)
        {
          EasyBeanDescriptions mbeanDescr = (EasyBeanDescriptions)annotation;
          for (EasyBeanDescription mbeanDesc : mbeanDescr.value())
          {
            descriptions.add(mbeanDesc);
          }
        }
      }
    }
    
    return getDescriptor(descriptions);
  }
  
  Descriptor getDescriptor(List<EasyBeanDescription> descriptions)
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
  
  boolean nullEmpty(String value)
  {
    return ((value == null) || (value.trim().length() == 0));
  }
  
  String capatalize(String text)
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
  
  boolean classesEquivalent(Class<?> clazz, String canonicalName)
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

  private class MethodTypePair
  {
    public Method method;
    
    public OpenTypeMapping typeMapping;
    
    public MethodTypePair(Method method, OpenTypeMapping typeMapping)
    {
      this.method = method;
      this.typeMapping = typeMapping;
    }
  }
  
  private class NotificationListenerEntry
  {
    private NotificationListener listener;
    private NotificationFilter filter;
    private Object handback;
    
    public NotificationListenerEntry(NotificationListener listener, NotificationFilter filter, Object handback)
    {
      this.listener = listener;
      this.filter = filter;
      this.handback = handback;
    }
    
    public void notifyIfNotFiltered(Notification notification)
    {
      if ((filter == null) || (filter.isNotificationEnabled(notification)))
      {
        listener.handleNotification(notification, handback);
      }
    }

    public boolean equals(NotificationListener listener, NotificationFilter filter, Object handback)
    {
      return ((this.listener == listener) && (this.filter == filter) && (this.handback == handback));
    }
    
    public boolean equals(NotificationListener listener)
    {
      return (this.listener == listener);
    }
  }
}
