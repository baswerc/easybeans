package org.baswell.easybeans;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

/*
 * Converts Java objects to runtime OpenTypes
 */
@SuppressWarnings("unchecked")
class OpenTypeConverter
{
  static Object convertToOpenType(Object obj, OpenTypeMapping typeMapping) throws OpenDataException
  {
    if (typeMapping.isSimpleType())
    {
      if (typeMapping.getOpenType() == SimpleType.STRING)
      {
        return (obj == null) ? null : obj.toString();
      }
      else
      {
        return obj;
      }
    }
    else if (typeMapping.isArrayType())
    {
      return convertToArray(obj, typeMapping);
    }
    else if (typeMapping.isTabularType())
    {
      return convertToTable((Map)obj, typeMapping);
    }
    else
    {
      return convertToComposite(obj, typeMapping);
    }
  }

  static Object convertToArray(Object obj, OpenTypeMapping typeMapping) throws OpenDataException
  {
    OpenTypeMapping elementTypeMapping = typeMapping.getElementTypeMapping();

    if (obj.getClass().isArray())
    {
      if (elementTypeMapping.isSimpleType())
      {
        return obj;
      }
      else
      {
        ArrayType arrayType = typeMapping.getArrayType();
        int numDimensions = arrayType.getDimension();

        if (numDimensions == 1)
        {
          int length = Array.getLength(obj);
          CompositeData[] compositeData = new CompositeData[length];
          for (int i = 0; i < length; i++)
          {
            compositeData[i] = convertToComposite(Array.get(obj, i), elementTypeMapping);
          }

          return compositeData;
        }
        else
        {
          int[] dimensions = new int[numDimensions];
          for (int i = 0; i < numDimensions; i++)
          {
            dimensions[i] = Array.getLength(Array.get(obj, i));
          }

          Object compositeData = Array.newInstance(CompositeData.class, dimensions);

          for (int i = 0; i < numDimensions; i++)
          {
            Object indexArray = Array.get(obj, i);
            int length = Array.getLength(indexArray);
            Object indexCompositeData = Array.newInstance(CompositeData.class, length);
            for (int j = 0; j < length; j++)
            {
              Array.set(indexCompositeData, j, convertToComposite(Array.get(indexArray, j), elementTypeMapping));
            }
            Array.set(compositeData, i, indexCompositeData);
          }

          return compositeData;
        }
      }
    }
    else if (obj instanceof Iterable)
    {
      Iterable iterable = (Iterable)obj;
      List list = new ArrayList();
      for (Object listObj : iterable)
      {
        list.add(listObj);
      }

      int length = list.size();
      boolean simpleType = elementTypeMapping.isSimpleType();
      Object array = Array.newInstance((simpleType ? elementTypeMapping.getSimpleClass() : CompositeData.class), length);

      for (int i = 0; i < length; i++)
      {
        Object listObj = list.get(i);
        if (simpleType)
        {
          /*
           * Generic list are converted to String[] so make sure we put Strings in String[] regardless of what's in the List.
           */
          if (elementTypeMapping.getSimpleClass() == String.class)
          {
            Array.set(array, i, listObj == null ? null : listObj.toString());
          }
          else
          {
            Array.set(array, i, listObj);
          }
        }
        else
        {
          Array.set(array, i, convertToComposite(listObj, elementTypeMapping));
        }
      }

      return array;
    }
    else
    {
      return obj; // Should never get here
    }
  }

  static TabularData convertToTable(Map map, OpenTypeMapping typeMapping) throws OpenDataException
  {
    TabularType tabularType = typeMapping.getTabularType();
    CompositeType rowType = tabularType.getRowType();

    TabularDataSupport tabularData = new TabularDataSupport(tabularType);

    OpenTypeMapping keyTypeMapping = typeMapping.getKeyTypeMapping();
    OpenTypeMapping valueTypeMapping = typeMapping.getValueTypeMapping();
    String[] rowAttNames = new String[] {"key", "value"};

    for (Object entryObj : map.entrySet())
    {
      Entry entry = (Entry)entryObj;
      Object key = entry.getKey();
      Object value = entry.getValue();

      Object mappedKey = convertToOpenType(key, keyTypeMapping);
      Object mappedValue = convertToOpenType(value, valueTypeMapping);
      tabularData.put(new CompositeDataSupport(rowType, rowAttNames, new Object[] {mappedKey, mappedValue}));
    }

    return tabularData;
  }

  static CompositeData convertToComposite(Object obj, OpenTypeMapping typeMapping) throws OpenDataException
  {
    CompositeType type = typeMapping.getCompositeType();
    Set<String> nameSet = type.keySet();

    List<String> names = new ArrayList<String>();
    List<Object> values = new ArrayList<Object>();

    for (String name : nameSet)
    {
      try
      {
        EasyBeanOpenTypeStructure beanAttribute = typeMapping.getAttributeStructure(name);
        OpenTypeMapping attributeTypeMapping = typeMapping.getAttributeMapping(name);

        Object value = beanAttribute.get(obj);
        Object mappedValue = (value == null) ? null : convertToOpenType(value, attributeTypeMapping);
        names.add(name);
        values.add(mappedValue);
      }
      catch (Exception exc)
      {
        throw new RuntimeException(exc);
      }
    }

    return new CompositeDataSupport(type, names.toArray(new String[names.size()]), values.toArray(new Object[values.size()]));
  }
}
