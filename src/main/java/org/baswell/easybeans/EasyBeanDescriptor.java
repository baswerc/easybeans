package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * {@link javax.management.Descriptor} information for classes, constructors, methods or fields. The number of names should
 * matches the number of values.
 *
 */
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanDescriptor
{
  /**
   * @see javax.management.Descriptor#getFieldNames()
   */
  String[] names();

  /**
   * @see javax.management.Descriptor#getFieldNames()
   */
  String[] values();
}
