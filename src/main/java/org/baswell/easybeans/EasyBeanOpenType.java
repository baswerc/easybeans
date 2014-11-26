package org.baswell.easybeans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EasyBeanOpenType
{
  String name() default "";
  
  String description() default "";
  
  Class containerType() default void.class;
}
