package org.baswell.easybeans;

import javax.management.ObjectName;

/**
 * Thrown when a duplicate {@link javax.management.ObjectName} is registered.
 *
 * @see org.baswell.easybeans.EasyBeansRegistery#register(Object)
 */
public class ObjectNameAlreadyRegistered extends EasyBeanException
{
  public final Class beanClass;

  public final ObjectName objectName;

  public ObjectNameAlreadyRegistered(Throwable cause, Class beanClass, ObjectName objectName)
  {
    super(cause);
    this.beanClass = beanClass;
    this.objectName = objectName;
  }
}
