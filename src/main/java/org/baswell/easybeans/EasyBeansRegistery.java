/*
 * Copyright 2015 Corey Baswell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.baswell.easybeans;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.*;

/**
 * Helper class for registering and unregistering Java beans and JMX MBeans. All beans are registered with
 * {@link java.lang.management.ManagementFactory#getPlatformMBeanServer()}.
 */
public class EasyBeansRegistery
{
  private final MBeanServer mBeanServer;
  
  private Map<Object, EasyBeanWrapper> registeredBeans = new HashMap<Object, EasyBeanWrapper>();

  public EasyBeansRegistery()
  {
    this(ManagementFactory.getPlatformMBeanServer());
  }

  public EasyBeansRegistery(MBeanServer mBeanServer)
  {
    this.mBeanServer = mBeanServer;
  }

  /**
   * If the given bean is already an instanceof of {@link org.baswell.easybeans.EasyBeanWrapper} then the given bean
   * will be registered directly with the MBean server. Otherwise the object will be wrapped with either {@link org.baswell.easybeans.EasyBeanNotificationWrapper}
   * (if the bean implements {@link EasyBeansNotifierUser} or {@link NotificationBroadcaster}) or a {@link org.baswell.easybeans.EasyBeanWrapper}.
   *
   * @param bean The bean to register.
   * @throws InvalidEasyBeanNameException If the ObjectName used for this bean in invalid.
   * @throws InvalidEasyBeanAnnotation If an EasyBean annotation is used incorrectly.
   * @throws InvalidEasyBeanOpenType If the given object (or a descendant of this object) cannot be mapped to an OpenType.
   * @throws ObjectNameAlreadyRegistered If the object name used for this bean is already registered.
   * @throws UnexpectedEasyBeanException If something unexpected occurred.
   */
  public void register(Object bean) throws InvalidEasyBeanNameException, InvalidEasyBeanAnnotation, InvalidEasyBeanOpenType, ObjectNameAlreadyRegistered, UnexpectedEasyBeanException
  {
    EasyBeanWrapper wrapper;

    if (bean instanceof EasyBeanWrapper)
    {
      wrapper = (EasyBeanWrapper) bean;
    }
    else if (bean instanceof EasyBeansNotifierUser)
    {
      wrapper = new EasyBeanNotificationWrapper((EasyBeansNotifierUser)bean);
    }
    else if (bean instanceof NotificationBroadcaster)
    {
      wrapper = new EasyBeanNotificationWrapper((NotificationBroadcaster)bean);
    }
    else
    {
      wrapper = new EasyBeanWrapper(bean);
    }

    wrapper.register(mBeanServer);
    registeredBeans.put(bean, wrapper);
  }

  /**
   * Registers each of the given beans.
   *
   * @param beans
   * @throws InvalidEasyBeanNameException If the ObjectName used for this bean in invalid.
   * @throws InvalidEasyBeanAnnotation If an EasyBean annotation is used incorrectly.
   * @throws InvalidEasyBeanOpenType If the given object (or a descendant of this object) cannot be mapped to an OpenType.
   * @throws ObjectNameAlreadyRegistered If the object name used for this bean is already registered.
   * @throws UnexpectedEasyBeanException If something unexpected occurred.
   * @see #register(Object)
   */
  public void setBeans(List beans) throws InvalidEasyBeanNameException, InvalidEasyBeanAnnotation, InvalidEasyBeanOpenType, ObjectNameAlreadyRegistered, UnexpectedEasyBeanException
  {
    register(beans);
  }

  /**
   * Registers each of the given beans.
   *
   * @param beans
   * @throws InvalidEasyBeanNameException If the ObjectName used for this bean in invalid.
   * @throws InvalidEasyBeanAnnotation If an EasyBean annotation is used incorrectly.
   * @throws InvalidEasyBeanOpenType If the given object (or a descendant of this object) cannot be mapped to an OpenType.
   * @throws ObjectNameAlreadyRegistered If the object name used for this bean is already registered.
   * @throws UnexpectedEasyBeanException If something unexpected occurred.
   * @see #register(Object)
   */
  public void register(List beans) throws InvalidEasyBeanNameException, InvalidEasyBeanAnnotation, InvalidEasyBeanOpenType, ObjectNameAlreadyRegistered, UnexpectedEasyBeanException
  {
    for (Object bean : beans)
    {
      register(bean);
    }
  }

  /**
   * Unregisters the given bean from the MBean server.
   *
   * @param bean
   * @throws UnexpectedEasyBeanException If something unexpected occurred.
   */
  public void unregister(Object bean) throws UnexpectedEasyBeanException
  {
    EasyBeanWrapper wrapper = registeredBeans.remove(bean);
    if (wrapper != null)
    {
      wrapper.unregister(mBeanServer);
    }
  }

  /**
   * Unregisters all previously registered beans.
   */
  public void unregisterAll()
  {
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    for (EasyBeanWrapper wrapper : registeredBeans.values())
    {
      try
      {
        wrapper.unregister(mBeanServer);
      }
      catch (Exception exc)
      {}
    }
    
    registeredBeans.clear();
  }
}
