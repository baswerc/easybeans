package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Notification emitted by an object.
 *
 * @see javax.management.MBeanNotificationInfo
 */
@Target({ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanNotification
{
  /**
   * @see javax.management.MBeanNotificationInfo#getNotifTypes()
   */
  String[] types();

  /**
   * @see javax.management.MBeanFeatureInfo#getName()
   */
  String name();

  /**
   * @see javax.management.MBeanFeatureInfo#getDescription()
   */
  String description() default "";

  /**
   * @see javax.management.MBeanFeatureInfo#getDescriptor()
   */
  EasyBeanDescriptor[] descriptor() default {};
}
