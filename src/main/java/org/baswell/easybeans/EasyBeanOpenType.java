package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata for how objects are mapped to {@link javax.management.openmbean.OpenType}.
 */
@Target({ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanOpenType
{
  /**
   * @see javax.management.openmbean.OpenType#getTypeName()
   */
  String name() default "";

  /**
   * @see javax.management.openmbean.OpenType#getDescription()
   */
  String description() default "";

  /**
   * If true the toString method of this annotated object will be used for it's JMX representation (SimpleType<String>).
   */
  boolean exposeAsString() default false;

  /**
   * The type of exposure for this object's (attribute) fields and methods. Defaults to {@link EasyBeanOpenTypeExposure#ALL}.
   */
  EasyBeanOpenTypeExposure exposure() default EasyBeanOpenTypeExposure.ALL;
}
