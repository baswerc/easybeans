package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides meta data for a MBean operation.
 * 
 */
@Target({ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanOperation
{
  /**
   * Specify this attribute if you need to override the operation name based
   * on the constructor name of this annotation.
   */
  String name() default "";
  String description() default "";
  String[] parameterNames() default {};
  String[] parameterDescriptions() default {};
  String[] parameterDefaultValues() default {};
  OperationImpact impact() default OperationImpact.UNKNOWN;
}
