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
  private Map<Object, EasyBeanWrapper> registeredBeans = new HashMap<Object, EasyBeanWrapper>();

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

    try
    {
      ManagementFactory.getPlatformMBeanServer().registerMBean(wrapper, wrapper.objectName);
      registeredBeans.put(bean, wrapper);
    }
    catch (InstanceAlreadyExistsException e)
    {
      throw new ObjectNameAlreadyRegistered(e, bean.getClass(), wrapper.objectName);
    }
    catch (MBeanRegistrationException e)
    {
      throw new UnexpectedEasyBeanException(e);
    }
    catch (NotCompliantMBeanException e)
    {
      throw new UnexpectedEasyBeanException(e);
    }
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
      try
      {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(wrapper.objectName);
      }
      catch (InstanceNotFoundException e)
      {}
      catch (MBeanRegistrationException e)
      {
        throw new UnexpectedEasyBeanException(e);
      }
    }
  }

  /**
   * Unregisters all previously registered beans.
   */
  public void unregisterAll()
  {
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    for (EasyBeanWrapper easyBeanWrapper : registeredBeans.values())
    {
      try
      {
        server.unregisterMBean(easyBeanWrapper.objectName);
      }
      catch (Exception exc)
      {}
    }
    
    registeredBeans.clear();
  }
}
