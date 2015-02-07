package org.baswell.easybeans;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.*;


public class EasyBeansRegistery
{
  private Map<Object, EasyBeanWrapper> registeredBeans = new HashMap<Object, EasyBeanWrapper>();

  /**
   *
   * @param obj
   * @throws InvalidEasyBeanNameException
   * @throws InvalidEasyBeanAnnotation
   * @throws InvalidEasyBeanOpenType
   * @throws ObjectNameAlreadyRegistered
   * @throws EasyBeanException
   */
  public void register(Object obj) throws InvalidEasyBeanNameException, InvalidEasyBeanAnnotation, InvalidEasyBeanOpenType, ObjectNameAlreadyRegistered, EasyBeanException
  {
    EasyBeanWrapper wrapper;

    if (obj instanceof EasyBeanWrapper)
    {
      wrapper = (EasyBeanWrapper) obj;
    }
    else if ((obj instanceof EasyBeansNotifierUser) || (obj instanceof NotificationBroadcaster))
    {
      wrapper = new EasyBeanNotificationWrapper(obj);
    }
    else
    {
      wrapper = new EasyBeanWrapper(obj);
    }

    try
    {
      ManagementFactory.getPlatformMBeanServer().registerMBean(wrapper, wrapper.objectName);
      registeredBeans.put(obj, wrapper);
    }
    catch (InstanceAlreadyExistsException e)
    {
      throw new ObjectNameAlreadyRegistered(e, obj.getClass(), wrapper.objectName);
    }
    catch (MBeanRegistrationException e)
    {
      throw new EasyBeanException(e);
    }
    catch (NotCompliantMBeanException e)
    {
      throw new EasyBeanException(e);
    }
  }

  /**
   *
   * @param objects
   * @throws InvalidEasyBeanNameException
   * @throws InvalidEasyBeanAnnotation
   * @throws InvalidEasyBeanOpenType
   * @throws ObjectNameAlreadyRegistered
   * @throws EasyBeanException
   */
  public void register(List objects) throws InvalidEasyBeanNameException, InvalidEasyBeanAnnotation, InvalidEasyBeanOpenType, ObjectNameAlreadyRegistered, EasyBeanException
  {
    for (Object object : objects)
    {
      register(object);
    }
  }

  /**
   *
   * @param obj
   * @throws EasyBeanException
   */
  public void unregister(Object obj) throws EasyBeanException
  {
    EasyBeanWrapper wrapper = registeredBeans.remove(obj);
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
        throw new EasyBeanException(e);
      }
    }
  }

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
