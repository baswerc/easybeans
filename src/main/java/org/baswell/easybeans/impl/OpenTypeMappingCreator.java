package org.baswell.easybeans.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.ObjectName;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularType;

import org.baswell.easybeans.EasyBeanOpenType;
import org.baswell.easybeans.EasyBeanTransient;

@SuppressWarnings("unchecked")
public class OpenTypeMappingCreator
{
  static Map<Class, OpenTypeMapping> simpleTypeMapping = new ConcurrentHashMap<Class, OpenTypeMapping>();
  static
  {
    simpleTypeMapping.put(Object.class, new OpenTypeMapping(SimpleType.STRING, String.class));
    simpleTypeMapping.put(String.class, new OpenTypeMapping(SimpleType.STRING, String.class));
    simpleTypeMapping.put(BigDecimal.class, new OpenTypeMapping(SimpleType.BIGDECIMAL, BigDecimal.class));
    simpleTypeMapping.put(BigInteger.class, new OpenTypeMapping(SimpleType.BIGINTEGER, BigDecimal.class));
    simpleTypeMapping.put(boolean.class, new OpenTypeMapping(SimpleType.BOOLEAN, Boolean.class));
    simpleTypeMapping.put(Boolean.class, new OpenTypeMapping(SimpleType.BOOLEAN, Boolean.class));
    simpleTypeMapping.put(byte.class, new OpenTypeMapping(SimpleType.BYTE, Byte.class));
    simpleTypeMapping.put(Byte.class, new OpenTypeMapping(SimpleType.BYTE, Byte.class));
    simpleTypeMapping.put(char.class, new OpenTypeMapping(SimpleType.CHARACTER, Character.class));
    simpleTypeMapping.put(Character.class, new OpenTypeMapping(SimpleType.CHARACTER, Character.class));
    simpleTypeMapping.put(Date.class, new OpenTypeMapping(SimpleType.DATE, Date.class));
    simpleTypeMapping.put(double.class, new OpenTypeMapping(SimpleType.DOUBLE, Double.class));
    simpleTypeMapping.put(Double.class, new OpenTypeMapping(SimpleType.DOUBLE, Double.class));
    simpleTypeMapping.put(float.class, new OpenTypeMapping(SimpleType.FLOAT, Float.class));
    simpleTypeMapping.put(Float.class, new OpenTypeMapping(SimpleType.FLOAT, Float.class));
    simpleTypeMapping.put(int.class, new OpenTypeMapping(SimpleType.INTEGER, Integer.class));
    simpleTypeMapping.put(Integer.class, new OpenTypeMapping(SimpleType.INTEGER, Integer.class));
    simpleTypeMapping.put(long.class, new OpenTypeMapping(SimpleType.LONG, Long.class));
    simpleTypeMapping.put(Long.class, new OpenTypeMapping(SimpleType.LONG, Long.class));
    simpleTypeMapping.put(ObjectName.class, new OpenTypeMapping(SimpleType.OBJECTNAME, ObjectName.class));
    simpleTypeMapping.put(short.class, new OpenTypeMapping(SimpleType.SHORT, Short.class));
    simpleTypeMapping.put(Short.class, new OpenTypeMapping(SimpleType.SHORT, Short.class));
    simpleTypeMapping.put(long.class, new OpenTypeMapping(SimpleType.LONG, Long.class));
    simpleTypeMapping.put(Long.class, new OpenTypeMapping(SimpleType.LONG, Long.class));
    simpleTypeMapping.put(void.class, new OpenTypeMapping(SimpleType.VOID, Void.class));
    simpleTypeMapping.put(Void.class, new OpenTypeMapping(SimpleType.VOID, Void.class));
  }
  
  public OpenTypeMappingCreator()
  {}

  public OpenTypeMapping createOpenType(TypeWrapper typeWrapper)
  {
    return createOpenType(typeWrapper, new ArrayList<Class>());
  }
  
  public OpenTypeMapping createOpenType(TypeWrapper typeWrapper, List<Class> compositedClassesVisited)
  {
    Class rawClass = typeWrapper.getRawClass();
    
    if (isTransient(typeWrapper))
    {
      return null;
    }
    else if (simpleTypeMapping.containsKey(rawClass))
    {
      return simpleTypeMapping.get(rawClass);
    }
    else if (extendsClass(rawClass, Enum.class))
    {
      return simpleTypeMapping.get(String.class);
    }
    else if (rawClass.isArray() || implementsInterface(rawClass, Iterable.class))
    {
      return createArrayType(typeWrapper, compositedClassesVisited);
    }
    else if (implementsInterface(rawClass, Map.class))
    {
      return createTabularType(typeWrapper, compositedClassesVisited);
    }
    else
    {
      return createCompositeType(typeWrapper, compositedClassesVisited);
    }
  }

  OpenTypeMapping createArrayType(TypeWrapper typeWrapper, List<Class> compositedClassesVisited)
  {
    Type type = typeWrapper.getType();
    Class rawClass = typeWrapper.getRawClass();

    try
    {
      if (rawClass.isArray())
      {
        int numDimensions = numberArrayDimensions(rawClass);
        OpenTypeMapping elementTypeMapping = null;
        
        if (type instanceof GenericArrayType)
        {
          GenericArrayType arrayType = (GenericArrayType)type;
          Type componentType = getArrayComponentType(arrayType);
          elementTypeMapping = createOpenType(new TypeWrapper(componentType), compositedClassesVisited);
        }
        else
        {
          Class componentClass = getArrayComponentClass(rawClass);
          elementTypeMapping = createOpenType(new TypeWrapper(componentClass), compositedClassesVisited);
        }

        if (elementTypeMapping == null)
        {
          return null;
        }
        else
        {
          return new OpenTypeMapping(new ArrayType(numDimensions, elementTypeMapping.getOpenType()), elementTypeMapping);
        }
      }
      else
      {
        Type componentType = null;
        if (type instanceof ParameterizedType)
        {
          ParameterizedType paramType = (ParameterizedType)type;
          if (paramType.getActualTypeArguments().length == 1)
          {
            componentType = paramType.getActualTypeArguments()[0];
          }
        }
        
        if (componentType == null)
        {
          componentType = String.class; // If we can't find the component type then we'll just display it as an array of strings
        }
        
        OpenTypeMapping listTypeMapping = createOpenType(new TypeWrapper(componentType), compositedClassesVisited);
        if (listTypeMapping == null)
        {
          return null;
        }
        else
        {
          return new OpenTypeMapping(new ArrayType(1, listTypeMapping.getOpenType()), listTypeMapping);
        }
      }
    }
    catch (OpenDataException odexc)
    {
      throw new RuntimeException(odexc);
    }
  }
  
  OpenTypeMapping createTabularType(TypeWrapper typeWrapper, List<Class> compositedClassesVisited)
  {
    Type type = typeWrapper.getType();
    Class rawClass = typeWrapper.getRawClass();

    Pair<Type, Type> keyValueTypePair = null;
    
    if (type instanceof ParameterizedType)
    {
      ParameterizedType paramType = (ParameterizedType)type;
      Type[] typeArgs = paramType.getActualTypeArguments();
      if (typeArgs.length == 2)
      {
        keyValueTypePair = new Pair<Type, Type>(typeArgs[0], typeArgs[1]);
      }
    }
    
    if (keyValueTypePair == null)
    {
      keyValueTypePair = new Pair<Type, Type>(String.class, String.class);
    }

    OpenTypeMapping keyMapping = createOpenType(new TypeWrapper(keyValueTypePair.getX()), compositedClassesVisited);
    OpenTypeMapping valueMapping = createOpenType(new TypeWrapper(keyValueTypePair.getY()), compositedClassesVisited);
    
    if ((keyMapping == null) || (valueMapping == null))
    {
      return null;
    }
    
    EasyBeanOpenType easyOpenType = typeWrapper.getAnnotation(EasyBeanOpenType.class);
    String name = ((easyOpenType == null) || (easyOpenType.name().length() == 0)) ? rawClass.getName() : easyOpenType.name();
    String description = ((easyOpenType == null) || (easyOpenType.description().length() == 0)) ? rawClass.getCanonicalName() : easyOpenType.description();

    String[] attributeNames = new String[] {"key", "value"};
    String[] attributeDescriptions = new String[] {"Map key", "Map value"};
    OpenType[] attributeTypes = new OpenType[] {keyMapping.getOpenType(), valueMapping.getOpenType()};

    try
    {
      CompositeType rowType = new CompositeType(name, description, attributeNames, attributeDescriptions, attributeTypes);
      return new OpenTypeMapping(new TabularType(name, description, rowType, new String[] {"key"}), keyMapping, valueMapping);
    }
    catch (OpenDataException odexc)
    {
      throw new RuntimeException(odexc);
    }
  }
  
  OpenTypeMapping createCompositeType(TypeWrapper typeWrapper, List<Class> compositedClassesVisited)
  {
    Class rawClass = typeWrapper.getRawClass();
    
    /*
     * Prevent infinite loops. Don't have a way (currently) to define self referring types. 
     */
    if (compositedClassesVisited.contains(rawClass))
    {
      return null;
    }

    try
    {
      compositedClassesVisited.add(rawClass);
      
      EasyBeanOpenType easyOpenType = typeWrapper.getAnnotation(EasyBeanOpenType.class);
      String name = ((easyOpenType == null) || (easyOpenType.name().length() == 0)) ? rawClass.getName() : easyOpenType.name();
      String description = ((easyOpenType == null) || (easyOpenType.description().length() == 0)) ? rawClass.getCanonicalName() : easyOpenType.description();
      
      List<String> attributeNameList = new ArrayList<String>();
      List<String> attributeDescriptionList = new ArrayList<String>();
      List<OpenType> attributeTypeList = new ArrayList<OpenType>();

      Map<String, Pair<Method, OpenTypeMapping>> attributeMappings = new HashMap<String, Pair<Method,OpenTypeMapping>>();

      BeanInfo beanInfo = Introspector.getBeanInfo(rawClass);
      PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
      
      for (PropertyDescriptor property : properties)
      {
        Method getter = property.getReadMethod();
        
        if (getter != null)
        {
          if (getter.getDeclaringClass() == Object.class) continue;
          
          TypeWrapper attributeTypeWrapper = new TypeWrapper(getter);
          OpenTypeMapping attributeTypeMapping = createOpenType(attributeTypeWrapper, compositedClassesVisited);
          if (attributeTypeMapping != null)
          {
            EasyBeanOpenType attributeEasyOpenType = attributeTypeWrapper.getAnnotation(EasyBeanOpenType.class);
            String attributeName = ((attributeEasyOpenType == null) || (attributeEasyOpenType.name().length() == 0)) ? property.getName() : attributeEasyOpenType.name();
            String attributeDescription = ((attributeEasyOpenType == null) || (attributeEasyOpenType.description().length() == 0)) ? property.getName() : attributeEasyOpenType.description();
            
            attributeMappings.put(attributeName, new Pair<Method, OpenTypeMapping>(getter, attributeTypeMapping));
            attributeNameList.add(attributeName);
            attributeDescriptionList.add(attributeDescription);
            attributeTypeList.add(attributeTypeMapping.getOpenType());
          }
        }
      }
      
      if (attributeNameList.size() == 0)
      {
        return null;
      }
      else
      {
        String[] attributeNames = attributeNameList.toArray(new String[attributeNameList.size()]);
        String[] attributeDescriptions = attributeDescriptionList.toArray(new String[attributeDescriptionList.size()]);
        OpenType[] attributeTypes = attributeTypeList.toArray(new OpenType[attributeTypeList.size()]);
        
        return new OpenTypeMapping(new CompositeType(name, description, attributeNames, attributeDescriptions, attributeTypes), attributeMappings);
      }
    }
    catch (IntrospectionException iexc)
    {
      throw new RuntimeException(iexc);
    }
    catch (OpenDataException odexc)
    {
      throw new RuntimeException(odexc);
    }
    finally
    {
      compositedClassesVisited.remove(rawClass);
    }
  }
  
  boolean isOpenType(Class clazz)
  {
    if (clazz == Object.class)
    {
      return false;
    }
    else if (clazz == OpenType.class)
    {
      return true;
    }
    else
    {
      return isOpenType(clazz.getSuperclass());
    }
  }
  
  boolean implementsInterface(Class clazz, Class interfce)
  {
    if (clazz.equals(interfce))
    {
      return true;
    }
    else
    {
      Class[] superInterfaces = clazz.getInterfaces();
      for (Class superInterface : superInterfaces)
      {
        if (implementsInterface(superInterface, interfce))
        {
          return true;
        }
      }
      return false;
    }
  }
  
  boolean extendsClass(Class clazz, Class superClass)
  { 
    if (clazz == superClass)
    {
      return true;
    }
    else if (clazz == Object.class)
    {
      return false;
    }
    else if (clazz.getSuperclass() == null)
    {
      return false;
    }
    else
    {
      return extendsClass(clazz.getSuperclass(), superClass);
    }
  }
  
  /**
   * Is there a better way to get this?
   * 
   * @param clazz The array class.
   * @return The number of dimensions found in the class name of the given class.
   */
  int numberArrayDimensions(Class clazz)
  {
    char[] chars = clazz.getName().toCharArray();
    int numDimensions = 0;
    for (char chr :chars)
    {
      if (chr == '[') ++numDimensions;
    }
    return numDimensions;
  }
  
  Class getArrayComponentClass(Class clazz)
  {
    Class componentClass = clazz.getComponentType();
    return (componentClass.isArray()) ? getArrayComponentClass(componentClass) : componentClass;
  }
  
  Type getArrayComponentType(GenericArrayType arrayType)
  {
    Type type = arrayType.getGenericComponentType();
    return (type instanceof GenericArrayType) ? getArrayComponentType((GenericArrayType)type) : type;
  }

  boolean isTransient(TypeWrapper typeWrapper)
  {
    return (typeWrapper.getAnnotation(EasyBeanTransient.class) != null);
  }
}
