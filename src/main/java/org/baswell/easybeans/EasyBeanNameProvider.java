package org.baswell.easybeans;

import javax.management.ObjectName;

/**
 * Provide a dynamic {@link ObjectName} for Java objects wrapped with {@link org.baswell.easybeans.EasyBeanWrapper}. This will take
 * priority over for the object name in the {@link EasyBean} annotation.
 *
 */
public interface EasyBeanNameProvider
{
  ObjectName getObjectName();
}
