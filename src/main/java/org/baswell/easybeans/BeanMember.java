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
  final Class clazz;

  protected String name;

  protected String description;

  protected OpenTypeMapping typeMapping;

  protected Descriptor descriptor;

  protected boolean wasAnnotated;

  protected BeanMember(Class clazz)
  {
    assert clazz != null;

    this.clazz = clazz;
  }
}
