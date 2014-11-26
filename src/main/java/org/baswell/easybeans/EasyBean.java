package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.management.ObjectName;

/**
 * Easy bean metadata for an JMX MBean.
 * 
 * @author Corey Baswell
 */
@Target({ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBean
{
  /**
   * The {@link ObjectName} for this easy bean.
   */
  String objectName() default "";

  /**
   * The description of this easy bean.
   */
  String description() default "";
  
  /**
   * The exposure level for members of this easy bean. Defaults to {@link EasyBeanExposureLevel#ANNOTATED}.
   */
  EasyBeanExposureLevel exposeLevel() default EasyBeanExposureLevel.ANNOTATED;
}
