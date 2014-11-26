package org.baswell.easybeans;

import javax.management.ObjectName;

/**
 * If you want to provide a dynamic {@link ObjectName} for you easy bean implement this interface. This will take
 * priority over for the object name in the {@link EasyBean} annotation.
 *
 */
public interface EasyBeanNameProvider
{
  ObjectName getObjectName();
}
