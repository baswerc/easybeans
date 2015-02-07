package org.baswell.easybeans;

import javax.management.MalformedObjectNameException;

/**
 * Thrown when the {@link #objectName} generated (or provided) for the {@link #beanClass} is invalid.
 */
public class InvalidEasyBeanNameException extends EasyBeanException
{
  public final Class beanClass;

  public final String objectName;

  public InvalidEasyBeanNameException(Class beanClass, String objectName, MalformedObjectNameException cause)
  {
    super(cause);
    this.beanClass = beanClass;
    this.objectName = objectName;
  }
}
