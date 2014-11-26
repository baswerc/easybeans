package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides meta data for a MBean operation parameter.
 * 
 * @author Corey Baswell
 *
 */
@Target({ElementType.PARAMETER})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Param
{
  String value() default "";
  String description() default "";
  String[] descriptorNames() default {};
  String[] descriptorValues() default {};
}
