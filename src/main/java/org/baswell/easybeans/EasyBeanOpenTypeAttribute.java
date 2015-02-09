package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata for how an object's attributes are mapped to {@link javax.management.openmbean.OpenType}.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanOpenTypeAttribute
{
  /**
   * The open type attribute name. If not provided the name will be created from the {@link java.lang.reflect.Field} or
   * {@link java.lang.reflect.Method}.
   */
  String name() default "";

  /**
   * Optional description of this attribute.
   */
  String description() default "";
}
