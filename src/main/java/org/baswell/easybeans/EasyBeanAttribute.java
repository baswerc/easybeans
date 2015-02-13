package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata for a MBean attribute.
 * 
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanAttribute  
{
  /**
   * The JMX attribute name. If not provided the name will be created from the {@link java.lang.reflect.Field} or
   * {@link java.lang.reflect.Method}.
   *
   * @see javax.management.openmbean.OpenMBeanAttributeInfo#getName()
   */
  String name() default "";

  /**
   * Optional description of this attribute.
   *
   * @see javax.management.openmbean.OpenMBeanAttributeInfo#getDescription()
   */
  String description() default "";

  /**
   * Forces this attribute to be read-only even if the field or setter is available to write to.
   */
  boolean readOnly() default false;
}
