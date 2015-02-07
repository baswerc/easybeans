package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Multiple {@link EasyBeanDescription}. Until 1.8 is minimum there can't be multiple annotations
 * of the same type at the same location.
 */
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanDescriptions
{
  EasyBeanDescription[] value();
}
