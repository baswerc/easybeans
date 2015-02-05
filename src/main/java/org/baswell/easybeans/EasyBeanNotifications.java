package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Until 1.8 is minimum we can't have multiple annotations of the same type at the same location.
 */
@Target({ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanNotifications
{
  EasyBeanNotification[] value();
}
