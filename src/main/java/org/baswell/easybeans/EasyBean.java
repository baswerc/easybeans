package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.management.ObjectName;

/**
 * Easy bean metadata for an JMX MBean.
 * 
 */
@Target({ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBean
{
  /**
   * The optional {@link ObjectName} for this easy bean. If not provided the object name will be generated from
   * the annotated object's class name.
   */
  String objectName() default "";

  /**
   * The optional description of this easy bean.
   */
  String description() default "";
  
  /**
   * The type of exposure for fields, methods and constructors of this easy bean. Defaults to {@link EasyBeanExposure#ANNOTATED}.
   */
  EasyBeanExposure expose() default EasyBeanExposure.ANNOTATED;
}
