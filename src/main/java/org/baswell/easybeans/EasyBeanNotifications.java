package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wrapper to specify multiple notifications. It's pretty lame that we can't 
 * have multiple annotations of the same type at the same location.
 * 
 * @author Corey Baswell
 */
@Target({ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanNotifications
{
  EasyBeanNotification[] value();
}
