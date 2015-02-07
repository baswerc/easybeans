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
  String[] types();
  String name();
  String description() default "";
  EasyBeanDescription[] descriptor() default {};
}
