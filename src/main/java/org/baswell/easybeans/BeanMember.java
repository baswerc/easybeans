package org.baswell.easybeans;

import javax.management.Descriptor;
import javax.management.modelmbean.DescriptorSupport;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class BeanMember
{
  abstract OpenTypeMapping getTypeMapping();

  final Class clazz;

  String name;

  String description;

  Descriptor descriptor;

  boolean wasAnnotated;

  BeanMember(Class clazz)
  {
    assert clazz != null;

    this.clazz = clazz;
  }
}
