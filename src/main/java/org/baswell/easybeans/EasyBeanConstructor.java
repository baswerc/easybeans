package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MetaData for a JMX MBean operation.
 *
 */
@Target({ElementType.CONSTRUCTOR})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanConstructor
{
  /**
   * The JMX constructor name name. If not provided the name will be created from the {@link java.lang.reflect.Constructor}.
   *
   * @see javax.management.openmbean.OpenMBeanOperationInfo#getName()
   */
  String name() default "";

  /**
   * Optional description of this attribute.
   *
   * @see javax.management.openmbean.OpenMBeanOperationInfo#getDescription()
   */
  String description() default "";

  /**
   * The names of the parameters (in order) for this constructor.
   *
   * @see javax.management.openmbean.OpenMBeanParameterInfo#getName()
   */
  String[] parameterNames() default {};

  /**
   * The descriptions of the parameters (in order) for this constructor.
   *
   * @see javax.management.openmbean.OpenMBeanParameterInfo#getDescription()
   */
  String[] parameterDescriptions() default {};
}
