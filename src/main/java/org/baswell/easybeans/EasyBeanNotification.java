package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides meta data for a MBean class.
 * 
 * @author Corey Baswell
 *
 */
@Target({ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanNotification
{
  String name();
  String[] types();
  String defaultType() default "";
  String description() default "";
  EasyBeanDescription[] descriptor() default {};
}
