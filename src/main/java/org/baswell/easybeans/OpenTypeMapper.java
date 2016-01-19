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

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.ObjectName;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularType;

import static org.baswell.easybeans.Pair.*;

/*
 * Maps Java objects to OpenTypes.
 */
@SuppressWarnings("unchecked")
class OpenTypeMapper
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

  static OpenTypeMapping mapToOpenType(Class clazz)
  {
    return mapOpenType(new EasyBeanOpenTypeStructure(clazz));
  }

  static OpenTypeMapping mapToOpenType(Field field)
  {
    return mapOpenType(new EasyBeanOpenTypeStructure(field));
  }

  static OpenTypeMapping mapAttributeToOpenType(Method method)
  {
    return mapOpenType(new EasyBeanOpenTypeStructure(method, true));
  }

  static OpenTypeMapping mapOperationToOpenType(Method method)
  {
    return mapOpenType(new EasyBeanOpenTypeStructure(method, false));
  }

  static OpenTypeMapping mapOpenType(EasyBeanOpenTypeStructure typeWrapper)
  {
    return mapOpenType(typeWrapper, new ArrayList<Class>());
  }

  static OpenTypeMapping mapOpenType(EasyBeanOpenTypeStructure typeWrapper, List<Class> compositedClassesVisited)
  {
    Class rawClass = typeWrapper.getRawClass();

    if (typeWrapper.isTransient())
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
      return mapArrayType(typeWrapper, compositedClassesVisited);
    }
    else if (implementsInterface(rawClass, Map.class))
    {
      return mapTabularType(typeWrapper, compositedClassesVisited);
    }
    else
    {
      return mapCompositeType(typeWrapper, compositedClassesVisited);
    }
  }

  static OpenTypeMapping mapArrayType(EasyBeanOpenTypeStructure typeWrapper, List<Class> compositedClassesVisited)
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
          elementTypeMapping = mapOpenType(new EasyBeanOpenTypeStructure(componentType), compositedClassesVisited);
        }
        else
        {
          Class componentClass = getArrayComponentClass(rawClass);
          elementTypeMapping = mapOpenType(new EasyBeanOpenTypeStructure(componentClass), compositedClassesVisited);
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

        OpenTypeMapping listTypeMapping = mapOpenType(new EasyBeanOpenTypeStructure(componentType), compositedClassesVisited);
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

  static OpenTypeMapping mapTabularType(EasyBeanOpenTypeStructure typeWrapper, List<Class> compositedClassesVisited)
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
        keyValueTypePair = pair(typeArgs[0], typeArgs[1]);
      }
    }

    if (keyValueTypePair == null)
    {
      keyValueTypePair = pair((Type) String.class, (Type) String.class);
    }

    OpenTypeMapping keyMapping = mapOpenType(new EasyBeanOpenTypeStructure(keyValueTypePair.x), compositedClassesVisited);
    OpenTypeMapping valueMapping = mapOpenType(new EasyBeanOpenTypeStructure(keyValueTypePair.y), compositedClassesVisited);

    if ((keyMapping == null) || (valueMapping == null))
    {
      return null;
    }


    String[] attributeNames = new String[] {"key", "value"};
    String[] attributeDescriptions = new String[] {"Map key", "Map value"};
    OpenType[] attributeTypes = new OpenType[] {keyMapping.getOpenType(), valueMapping.getOpenType()};

    try
    {
      CompositeType rowType = new CompositeType(typeWrapper.getName(), typeWrapper.getDescription(), attributeNames, attributeDescriptions, attributeTypes);
      return new OpenTypeMapping(new TabularType(typeWrapper.getName(), typeWrapper.getDescription(), rowType, new String[] {"key"}), keyMapping, valueMapping);
    }
    catch (OpenDataException odexc)
    {
      throw new RuntimeException(odexc);
    }
  }

  static OpenTypeMapping mapCompositeType(EasyBeanOpenTypeStructure typeWrapper, List<Class> compositedClassesVisited)
  {
    Class rawClass = typeWrapper.getRawClass();

    /*
     * Prevent infinite loops. Don't have a way to define self referring types.
     */
    if (compositedClassesVisited.contains(rawClass))
    {
      return simpleTypeMapping.get(String.class);
    }

    try
    {
      compositedClassesVisited.add(rawClass);

      Map<String, Pair<EasyBeanOpenTypeStructure, OpenTypeMapping>> attributeMappings = new HashMap<String, Pair<EasyBeanOpenTypeStructure,OpenTypeMapping>>();
      List<String> attributeNameList = new ArrayList<String>();
      List<String> attributeDescriptionList = new ArrayList<String>();
      List<OpenType> attributeTypeList = new ArrayList<OpenType>();

      for (EasyBeanOpenTypeStructure attributeTypeWrapper : typeWrapper.getAttributes())
      {
        OpenTypeMapping attributeTypeMapping = mapOpenType(attributeTypeWrapper, compositedClassesVisited);
        if (attributeTypeMapping != null)
        {
          String name = attributeTypeWrapper.getField().getName();
          attributeMappings.put(name, pair(attributeTypeWrapper, attributeTypeMapping));
          attributeNameList.add(name);
          attributeDescriptionList.add(attributeTypeWrapper.getDescription());
          attributeTypeList.add(attributeTypeMapping.getOpenType());
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

        return new OpenTypeMapping(new CompositeType(typeWrapper.getName(), typeWrapper.getDescription(), attributeNames, attributeDescriptions, attributeTypes), attributeMappings);
      }
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

  static boolean implementsInterface(Class clazz, Class interfce)
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

  static boolean extendsClass(Class clazz, Class superClass)
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

  /*
   * TODO Is there a better way to get this?
   */
  static int numberArrayDimensions(Class clazz)
  {
    char[] chars = clazz.getName().toCharArray();
    int numDimensions = 0;
    for (char chr :chars)
    {
      if (chr == '[') ++numDimensions;
    }
    return numDimensions;
  }

  static Class getArrayComponentClass(Class clazz)
  {
    Class componentClass = clazz.getComponentType();
    return (componentClass.isArray()) ? getArrayComponentClass(componentClass) : componentClass;
  }

  static Type getArrayComponentType(GenericArrayType arrayType)
  {
    Type type = arrayType.getGenericComponentType();
    return (type instanceof GenericArrayType) ? getArrayComponentType((GenericArrayType)type) : type;
  }
}
