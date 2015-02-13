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

import java.util.Map;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularType;

/*
 * The OpenType mapping details for a Java object.
 */
@SuppressWarnings("unchecked")
class OpenTypeMapping
{
  private OpenType openType;

  private Class simpleClass;
  
  private OpenTypeMapping elementTypeMapping;
  
  private OpenTypeMapping keyTypeMapping;
  
  private OpenTypeMapping valueTypeMapping;
  
  private Map<String, Pair<EasyBeanOpenTypeStructure, OpenTypeMapping>> attributeMappings;
  
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

  OpenTypeMapping(CompositeType compositeType, Map<String, Pair<EasyBeanOpenTypeStructure, OpenTypeMapping>> attributeMappings)
  {
    openType = compositeType;
    this.attributeMappings = attributeMappings;
  }

  OpenType getOpenType()
  {
    return openType;
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

  EasyBeanOpenTypeStructure getAttributeStructure(String name)
  {
    return attributeMappings.get(name).x;
  }
  
  OpenTypeMapping getAttributeMapping(String name)
  {
    return attributeMappings.get(name).y;
  }
}
