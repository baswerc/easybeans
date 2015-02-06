package org.baswell.easybeans;

import java.lang.reflect.Method;
import java.util.Map;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularType;

@SuppressWarnings("unchecked")
class OpenTypeMapping
{
  private OpenType openType;

  private boolean nativeType;
  
  private Class simpleClass;
  
  private OpenTypeMapping elementTypeMapping;
  
  private OpenTypeMapping keyTypeMapping;
  
  private OpenTypeMapping valueTypeMapping;
  
  private Map<String, Pair<Method, OpenTypeMapping>> attributeMappings;
  
  OpenTypeMapping(OpenType nativeOpenType)
  {
    this.openType = nativeOpenType;
    nativeType = true;
  }

  OpenTypeMapping(SimpleType simpleType, Class simpleClass)
  {
    this.openType = simpleType;
    this.simpleClass = simpleClass;
  }
  
  OpenTypeMapping(ArrayType arrayType, OpenTypeMapping elementTypeMapping)
  {
    openType = arrayType;
    this.elementTypeMapping = elementTypeMapping;
  }
  
  OpenTypeMapping(TabularType tabularType, OpenTypeMapping keyTypeMapping, OpenTypeMapping valueTypeMapping)
  {
    openType = tabularType;
    this.keyTypeMapping = keyTypeMapping;
    this.valueTypeMapping = valueTypeMapping;
  }

  OpenTypeMapping(CompositeType compositeType, Map<String, Pair<Method, OpenTypeMapping>> attributeMappings)
  {
    openType = compositeType;
    this.attributeMappings = attributeMappings;
  }

  OpenType getOpenType()
  {
    return openType;
  }
  
  boolean isNativeType()
  {
    return nativeType;
  }

  boolean isSimpleType()
  {
    return (openType instanceof SimpleType);
  }
  
  boolean isArrayType()
  {
    return (openType instanceof ArrayType);
  }
  
  boolean isTabularType()
  {
    return (openType instanceof TabularType);
  }
  
  boolean isCompositeType()
  {
    return (openType instanceof CompositeType);
  }
  
  SimpleType getSimpleType()
  {
    return (SimpleType)openType;
  }
  
  ArrayType getArrayType()
  {
    return (ArrayType)openType;
  }
  
  TabularType getTabularType()
  {
    return (TabularType)openType;
  }
  
  Class getSimpleClass()
  {
    return simpleClass;
  }

  OpenTypeMapping getElementTypeMapping()
  {
    return elementTypeMapping;
  }
  
  OpenTypeMapping getKeyTypeMapping()
  {
    return keyTypeMapping;
  }

  OpenTypeMapping getValueTypeMapping()
  {
    return valueTypeMapping;
  }

  CompositeType getCompositeType()
  {
    return (CompositeType)openType;
  }
  
  Method getAttributeMethod(String name)
  {
    return attributeMappings.get(name).getX();
  }
  
  OpenTypeMapping getAttributeMapping(String name)
  {
    return attributeMappings.get(name).getY();
  }
}
