package org.baswell.easybeans;

import javax.management.Descriptor;
import javax.management.modelmbean.DescriptorSupport;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Base class for EasyBeanConstructorStructure, EasyBeanAttributeStructure, and EasyBeanOperationStructure.
 */
abstract class EasyBeanMemberStructure
{
  final Class clazz;

  String name;

  String description;

  Descriptor descriptor;

  boolean wasAnnotated;

  OpenTypeMapping typeMapping;

  EasyBeanMemberStructure(Class clazz)
  {
    assert clazz != null;

    this.clazz = clazz;
  }
}
