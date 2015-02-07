package org.baswell.easybeans;

import javax.management.ObjectName;

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
