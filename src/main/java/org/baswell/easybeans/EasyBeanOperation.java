package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata for a JMX MBean operation.
 *
 * @see javax.management.openmbean.OpenMBeanOperationInfo
 */
@Target({ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanOperation
{
  /**
   * The JMX operation name. If not provided the name will be created from the {@link java.lang.reflect.Method}.
   *
   * @see javax.management.openmbean.OpenMBeanOperationInfo#getName()
   */
  String name() default "";

  /**
   * Optional description of this operation.
   *
   * @see javax.management.openmbean.OpenMBeanOperationInfo#getDescription()
   */
  String description() default "";

  /**
   * The names of the parameters (in order) for this operation.
   *
   * @see javax.management.openmbean.OpenMBeanParameterInfo#getName()
   */
  String[] parameterNames() default {};

  /**
   * The descriptions of the parameters (in order) for this operation.
   *
   * @see javax.management.openmbean.OpenMBeanParameterInfo#getDescription()
   */
  String[] parameterDescriptions() default {};

  /**
   * The default values of the parameters (in order) for this operation.
   *
   * @see javax.management.openmbean.OpenMBeanParameterInfo#getDefaultValue()
   */
  String[] parameterDefaultValues() default {};

  /**
   * The impact of this operation.
   *
   * @see javax.management.openmbean.OpenMBeanOperationInfo#getImpact()
   */
  OperationImpact impact() default OperationImpact.UNKNOWN;
}
