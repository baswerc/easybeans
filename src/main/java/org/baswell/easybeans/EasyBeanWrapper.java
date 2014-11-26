package org.baswell.easybeans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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

import org.baswell.easybeans.impl.OpenTypeMapper;
import org.baswell.easybeans.impl.OpenTypeMapping;
import org.baswell.easybeans.impl.OpenTypeMappingCreator;
import org.baswell.easybeans.impl.TypeWrapper;

/**
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

  Object proxiedObject;
  EasyBeanExposureLevel exposeLevel;

  MBeanInfo info;
  ObjectName objectName;
  List<NotificationListenerEntry> listenerEntries;

  Map<String, MethodTypePair> attributeReadMap;
  Map<String, MethodTypePair> attributeWriteMap;
  Map<String, List<MethodTypePair>> operationMap;
  
  OpenTypeMappingCreator openTypeMappingCreator;
  OpenTypeMapper openTypeMapper;
  
  /**
   * 
   * @param proxiedObject The annotated object to make JMX accessible.
   * @throws EasyBeanDefinitionException If the {@link ObjectName} defined in {@link EasyBean} is invalid.
   */
  public EasyBeanWrapper(Object proxiedObject) throws EasyBeanDefinitionException
  {
    this.proxiedObject = proxiedObject;
    openTypeMappingCreator = new OpenTypeMappingCreator();
    openTypeMapper = new OpenTypeMapper();
    
    Class clazz = proxiedObject.getClass();
    String className = clazz.getCanonicalName();
    String mbeanDescription = null;
    EasyBean mbeanAnnotation = (EasyBean)clazz.getAnnotation(EasyBean.class);
    
    try
    {
      if (proxiedObject instanceof EasyBeanNameProvider)
      {
        objectName = ((EasyBeanNameProvider)proxiedObject).getObjectName();
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

      BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
      OpenMBeanConstructorInfo[] constructorInfo = loadConstructorInfo(clazz);
      OpenMBeanAttributeInfo[] attributeInfo = loadAttributeInfo(beanInfo, clazz);
      OpenMBeanOperationInfo[] opInfo = loadOperationInfo(beanInfo);
      MBeanNotificationInfo[] notificationInfo = loadNotificationInfo(clazz);
      Descriptor descriptor = (mbeanAnnotation == null) ? null : getDescriptor(clazz.getAnnotations());
      
      info = new OpenMBeanInfoSupport(className, mbeanDescription, attributeInfo, constructorInfo, opInfo, notificationInfo, descriptor);
    }
    catch (MalformedObjectNameException monexc)
    {
      throw new EasyBeanDefinitionException(monexc);
    }
    catch (IntrospectionException iexc)
    {
      throw new RuntimeException("Unable to get BeanInfo from class '" + clazz.getCanonicalName() + "' due to introspection excetion: " + iexc.getMessage());
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
      MethodTypePair methodTypePair = attributeReadMap.get(attribute);
      Object value = methodTypePair.method.invoke(proxiedObject);
      return openTypeMapper.map(value, methodTypePair.typeMapping); 
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
    List<MethodTypePair> methods = operationMap.get(actionName);

    for (MethodTypePair methodTypePair : methods)
    {
      Method method = methodTypePair.method;
      
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
            Object value = method.invoke(proxiedObject, params);
            return openTypeMapper.map(value, methodTypePair.typeMapping);
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
      MethodTypePair methodTypePair = attributeWriteMap.get(attName);
      
      try
      {
        methodTypePair.method.invoke(proxiedObject, attribute.getValue());
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
    if (proxiedObject instanceof NotificationBroadcaster)
    {
      return ((NotificationBroadcaster)proxiedObject).getNotificationInfo();
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
    if (proxiedObject instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster)proxiedObject).addNotificationListener(listener, filter, handback);
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
    if (proxiedObject instanceof NotificationEmitter)
    {
      ((NotificationEmitter)proxiedObject).removeNotificationListener(listener, filter, handback);
    }
    else if (proxiedObject instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster)proxiedObject).removeNotificationListener(listener);
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
    if (proxiedObject instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster)proxiedObject).removeNotificationListener(listener);
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

  OpenMBeanConstructorInfo[] loadConstructorInfo(Class clazz)
  {
    Constructor[] constructors = clazz.getConstructors();
    List<OpenMBeanConstructorInfo> constructorsInfo = new ArrayList<OpenMBeanConstructorInfo>();
    
    for (Constructor constructor : constructors)
    {
      EasyBeanConstructor easyConstructorAnnotation = (EasyBeanConstructor)constructor.getAnnotation(EasyBeanConstructor.class);
      EasyBeanTransient transientAnnotation = (EasyBeanTransient)constructor.getAnnotation(EasyBeanTransient.class);

      if ((transientAnnotation == null) && ((easyConstructorAnnotation != null) || (exposeLevel == EasyBeanExposureLevel.ALL)))
      {
        OpenMBeanParameterInfo[] paramsInfo = getParameterInfo(constructor.getParameterTypes(), constructor.getParameterAnnotations());
        if (paramsInfo != null)
        {
          String name = (easyConstructorAnnotation != null) ? easyConstructorAnnotation.name() : null;
          if (nullEmpty(name)) name = constructor.getName();
          
          String description = (easyConstructorAnnotation != null) ? easyConstructorAnnotation.description() : null;
          if (nullEmpty(description)) description = name;
          
          constructorsInfo.add(new OpenMBeanConstructorInfoSupport(name, description, paramsInfo, getDescriptor(constructor)));
        }
      }
    }
    
    return constructorsInfo.toArray(new OpenMBeanConstructorInfo[constructorsInfo.size()]);
  }
  
  OpenMBeanAttributeInfo[] loadAttributeInfo(BeanInfo beanInfo, Class beanClass)
  {
    attributeReadMap = new HashMap<String, MethodTypePair>();
    attributeWriteMap = new HashMap<String, MethodTypePair>();
    
    PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
    List<OpenMBeanAttributeInfo> attributesInfo = new ArrayList<OpenMBeanAttributeInfo>();
    
    List<String> methodsAdded = new ArrayList<String>();
    
    for (PropertyDescriptor property : properties)
    {
      Method readMethod = property.getReadMethod();
      EasyBeanAttribute readMeta = (readMethod == null) ? null : readMethod.getAnnotation(EasyBeanAttribute.class);
      EasyBeanTransient readTransient = (readMethod == null) ? null : readMethod.getAnnotation(EasyBeanTransient.class);

      Method writeMethod = property.getWriteMethod();
      EasyBeanAttribute writeMeta = (writeMethod == null) ? null : writeMethod.getAnnotation(EasyBeanAttribute.class);
      EasyBeanTransient writeTransient = (writeMethod == null) ? null : writeMethod.getAnnotation(EasyBeanTransient.class);

      if ((readMethod == null) && (writeMethod == null)) continue;
      
      Class declaringClass = (readMethod != null) ? readMethod.getDeclaringClass() : writeMethod.getDeclaringClass();
      String declaringPackage = declaringClass.getPackage().getName();
      
      if (!declaringPackage.startsWith("java.") && !declaringPackage.startsWith("javax."))
      {
        boolean isReadable = ((readTransient == null) && (readMethod != null) && ((readMeta != null) || (exposeLevel != EasyBeanExposureLevel.ANNOTATED)));
        boolean isWriteable = ((writeTransient == null) && (writeMeta != null) && ((writeMeta != null) || (exposeLevel == EasyBeanExposureLevel.ALL)));
  
        if (isReadable || isWriteable)
        {
          String name = (readMeta != null) ? readMeta.name() : null;
          if (nullEmpty(name)) name = (writeMeta != null) ? writeMeta.name() : null;
          if (nullEmpty(name)) name = property.getName();
          
          name = capatalize(name);
          
          String description = (readMeta != null) ? readMeta.description() : null;
          if (nullEmpty(description)) description = (writeMeta != null) ? writeMeta.description() : null;
          if (nullEmpty(description)) description = name;
          
          TypeWrapper typeWrapper = (readMethod != null) ? new TypeWrapper(readMethod) : new TypeWrapper(writeMethod);
          OpenTypeMapping typeMapping = openTypeMappingCreator.createOpenType(typeWrapper);
          
          if (typeMapping != null)
          {
            boolean isIs = (readMethod != null) ? readMethod.getName().startsWith("is") : false;
            
            attributesInfo.add(new OpenMBeanAttributeInfoSupport(name, description, (OpenType<Long>)typeMapping.getOpenType(), isReadable, isWriteable, isIs, getDescriptor(readMethod, writeMethod))); 
            
            if (isReadable) 
            {
              attributeReadMap.put(name, new MethodTypePair(readMethod, typeMapping));
              methodsAdded.add(readMethod.getName());
            }
            
            if (isWriteable) 
            {
              attributeWriteMap.put(name, new MethodTypePair(writeMethod, typeMapping));
              methodsAdded.add(writeMethod.getName());
            }
          }
        }
      }
    }
    
    for (Method method : beanClass.getMethods())
    {
      EasyBeanAttribute attMeta = method.getAnnotation(EasyBeanAttribute.class);
      EasyBeanTransient attTransient = method.getAnnotation(EasyBeanTransient.class);
      
      if ((attMeta != null) && (attTransient == null))
      {
        String methodName = method.getName();
        String packge = method.getDeclaringClass().getPackage().getName();
        
        if (!methodsAdded.contains(methodName) && !packge.startsWith("java.") && !packge.startsWith("javax."))
        {
          if ((method.getParameterTypes().length == 0) && (method.getReturnType() != void.class))
          {
            OpenTypeMapping typeMapping = openTypeMappingCreator.createOpenType(new TypeWrapper(method));
            if (typeMapping != null)
            {
              String name = attMeta.name();
              if (nullEmpty(name)) name = method.getName();
              
              name = capatalize(name);
              
              String description = attMeta.description();
              if (nullEmpty(description)) description = name;
  
              attributesInfo.add(new OpenMBeanAttributeInfoSupport(name, description, (OpenType<Long>)typeMapping.getOpenType(), true, false, false, getDescriptor(method))); 
              attributeReadMap.put(name, new MethodTypePair(method, typeMapping));
            }
          }
          else if ((method.getParameterTypes().length == 1) && (method.getReturnType() == void.class))
          {
            OpenTypeMapping typeMapping = openTypeMappingCreator.createOpenType(new TypeWrapper(method));
            if (typeMapping != null)
            {
              String name = attMeta.name();
              if (nullEmpty(name)) name = method.getName();
              
              name = capatalize(name);
              
              String description = attMeta.description();
              if (nullEmpty(description)) description = name;
  
              attributesInfo.add(new OpenMBeanAttributeInfoSupport(name, description, (OpenType<Long>)typeMapping.getOpenType(), false, true, false, getDescriptor(method))); 
              attributeReadMap.put(name, new MethodTypePair(method, typeMapping));
            }
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

  OpenMBeanOperationInfo[] loadOperationInfo(BeanInfo beanInfo)
  {
    operationMap = new HashMap<String, List<MethodTypePair>>();

    MethodDescriptor[] methodDescriptors = beanInfo.getMethodDescriptors();
    List<OpenMBeanOperationInfo> operationsInfo = new ArrayList<OpenMBeanOperationInfo>();
    
    for (MethodDescriptor methodDescriptor : methodDescriptors)
    {
      Method method = methodDescriptor.getMethod();
      String packge = method.getDeclaringClass().getPackage().getName();
      EasyBeanTransient metaTransient = method.getAnnotation(EasyBeanTransient.class);
      EasyBeanAttribute metaAtt = method.getAnnotation(EasyBeanAttribute.class);
      EasyBeanOperation metaOp = method.getAnnotation(EasyBeanOperation.class);
      
      if ((metaTransient == null) && !packge.startsWith("java.") && !packge.startsWith("javax.") && (metaAtt == null) && ((metaOp != null) || (exposeLevel == EasyBeanExposureLevel.ALL)))
      {
        String name = (metaOp == null) ? null : metaOp.name();
        if (nullEmpty(name)) name = method.getName();
        
        String description = (metaOp == null) ? null : metaOp.description();
        if (nullEmpty(description)) description = name;
        
        OpenTypeMapping typeMapping = openTypeMappingCreator.createOpenType(new TypeWrapper(method));
        
        if (typeMapping != null)
        {
          OpenMBeanParameterInfo[] paramsInfo = getParameterInfo(method.getParameterTypes(), method.getParameterAnnotations());
          if (paramsInfo != null)
          {
            int impact = (metaOp == null) ? OperationImpact.UNKNOWN.getMBeanImpact() : metaOp.impact().getMBeanImpact();
            operationsInfo.add(new OpenMBeanOperationInfoSupport(name, description, paramsInfo, typeMapping.getOpenType(), impact, getDescriptor(method)));
    
            List<MethodTypePair> operationMethods;
            if (operationMap.containsKey(name))
            {
              operationMethods = operationMap.get(name);
            }
            else
            {
              operationMethods = new ArrayList<MethodTypePair>();
              operationMap.put(name, operationMethods);
            }
    
            operationMethods.add(new MethodTypePair(method, typeMapping));
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
    if (proxiedObject instanceof NotificationBroadcaster)
    {
      return ((NotificationBroadcaster)proxiedObject).getNotificationInfo();
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
  

  OpenMBeanParameterInfo[] getParameterInfo(Class<?>[] paramTypes, Annotation[][] annotations)
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
      
      String description = name;
      
      for (Annotation annotation : annotations[i])
      {
        if (annotation instanceof Param)
        {
          Param jmxParameter = (Param)annotation;
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
