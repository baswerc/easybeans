package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wrapper to specify multiple descriptor name value pairs per type, constructor, method, etc. It's pretty
 * lame that we can't have multiple annotations of the same type at the same location.
 * 
 * @author Corey Baswell
 */
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanDescriptions
{
  EasyBeanDescription[] value();
}
