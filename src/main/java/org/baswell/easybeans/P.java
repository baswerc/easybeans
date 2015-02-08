package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata for a MBean operation parameter.
 */
@Target({ElementType.PARAMETER})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface P
{
  /**
   * The name of this parameter.
   */
  String value() default "";

  /**
   * The description of this parameter.
   */
  String description() default "";

  String[] defaultValue() default {};

  /**
   * {@link javax.management.Descriptor} information about this parameter. The number of descriptorNames must match the
   * number of descriptor values so that descriptorNames[i] = descriptorValues[i].
   */
  String[] descriptorNames() default {};

  /**
   * {@link javax.management.Descriptor} information about this parameter. The number of descriptorNames must match the
   * number of descriptor values so that descriptorNames[i] = descriptorValues[i].
   */
  String[] descriptorValues() default {};
}
