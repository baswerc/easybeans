package org.baswell.easybeans.impl;

import java.lang.reflect.Method;
import java.util.Map;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularType;

@SuppressWarnings("unchecked")
public class OpenTypeMapping
{
  private OpenType openType;

  private boolean nativeType;
  
  private Class simpleClass;
  
  private OpenTypeMapping elementTypeMapping;
  
  private OpenTypeMapping keyTypeMapping;
  
  private OpenTypeMapping valueTypeMapping;
  
  private Map<String, Pair<Method, OpenTypeMapping>> attributeMappings;
  
  public OpenTypeMapping(OpenType nativeOpenType)
  {
    this.openType = nativeOpenType;
    nativeType = true;
  }

  public OpenTypeMapping(SimpleType simpleType, Class simpleClass)
  {
    this.openType = simpleType;
    this.simpleClass = simpleClass;
  }
  
  public OpenTypeMapping(ArrayType arrayType, OpenTypeMapping elementTypeMapping)
  {
    openType = arrayType;
    this.elementTypeMapping = elementTypeMapping;
  }
  
  public OpenTypeMapping(TabularType tabularType, OpenTypeMapping keyTypeMapping, OpenTypeMapping valueTypeMapping)
  {
    openType = tabularType;
    this.keyTypeMapping = keyTypeMapping;
    this.valueTypeMapping = valueTypeMapping;
  }

  public OpenTypeMapping(CompositeType compositeType, Map<String, Pair<Method, OpenTypeMapping>> attributeMappings)
  {
    openType = compositeType;
    this.attributeMappings = attributeMappings;
  }

  public OpenType getOpenType()
  {
    return openType;
  }
  
  public boolean isNativeType()
  {
    return nativeType;
  }

  public boolean isSimpleType()
  {
    return (openType instanceof SimpleType);
  }
  
  public boolean isArrayType()
  {
    return (openType instanceof ArrayType);
  }
  
  public boolean isTabularType()
  {
    return (openType instanceof TabularType);
  }
  
  public boolean isCompositeType()
  {
    return (openType instanceof CompositeType);
  }
  
  public SimpleType getSimpleType()
  {
    return (SimpleType)openType;
  }
  
  public ArrayType getArrayType()
  {
    return (ArrayType)openType;
  }
  
  public TabularType getTabularType()
  {
    return (TabularType)openType;
  }
  
  public Class getSimpleClass()
  {
    return simpleClass;
  }

  public OpenTypeMapping getElementTypeMapping()
  {
    return elementTypeMapping;
  }
  
  public OpenTypeMapping getKeyTypeMapping()
  {
    return keyTypeMapping;
  }

  public OpenTypeMapping getValueTypeMapping()
  {
    return valueTypeMapping;
  }

  public CompositeType getCompositeType()
  {
    return (CompositeType)openType;
  }
  
  public Method getAttributeMethod(String name)
  {
    return attributeMappings.get(name).getX();
  }
  
  public OpenTypeMapping getAttributeMapping(String name)
  {
    return attributeMappings.get(name).getY();
  }
}
