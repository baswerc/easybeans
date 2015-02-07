package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides meta data for a MBean attribute.
 * 
 * @author Corey Baswell
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanAttribute  
{
  /**
   * Specify this attribute if you need to override the attribute name based
   * on the constructor name of this annotation.
   */
  String name() default "";
  String description() default "";
}
