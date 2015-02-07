package org.baswell.easybeans;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.*;


public class EasyBeansRegistery
{
  private Map<Object, EasyBeanWrapper> registeredBeans = new HashMap<Object, EasyBeanWrapper>();

  public void register(Object obj) throws MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException, MalformedObjectNameException
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

    ObjectName oName = wrapper.getObjectName();
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    
    if (server.isRegistered(oName))
    {
      try
      {
        server.unregisterMBean(oName);
      }
      catch (InstanceNotFoundException e)
      {}
    }
    
    ManagementFactory.getPlatformMBeanServer().registerMBean(wrapper, oName);
    registeredBeans.put(obj, wrapper);
  }

  public void register(List objects) throws MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException, MalformedObjectNameException
  {
    for (Object object : objects)
    {
      register(object);
    }
  }

  public void unregister(Object obj) throws InstanceNotFoundException, MBeanRegistrationException
  {
    EasyBeanWrapper wrapper = registeredBeans.remove(obj);
    if (wrapper == null)
    {
      throw new InstanceNotFoundException("No mbean registered for object " + obj);
    }
    else
    {
      ManagementFactory.getPlatformMBeanServer().unregisterMBean(wrapper.getObjectName());
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
