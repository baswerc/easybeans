package org.baswell.easybeans;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class EasyBeanServerRegistery
{
  private Map<IdentityHashKey, EasyBeanWrapper> beansMap = new HashMap<IdentityHashKey, EasyBeanWrapper>();

  public void register(Object obj) throws MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException, MalformedObjectNameException
  {
    EasyBeanWrapper wrapper = (obj instanceof EasyBeanWrapper) ? (EasyBeanWrapper)obj : new EasyBeanWrapper(obj);
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
    beansMap.put(new IdentityHashKey(obj), wrapper);
  }

  public void unregister(Object obj) throws InstanceNotFoundException, MBeanRegistrationException
  {
    IdentityHashKey key = new IdentityHashKey(obj);

    EasyBeanWrapper wrapper = beansMap.remove(key);
    if (wrapper == null)
    {
      throw new InstanceNotFoundException("No mbean registered for object " + obj);
    }
    else
    {
      ManagementFactory.getPlatformMBeanServer().unregisterMBean(wrapper.getObjectName());
    }
  }
  
  public void setEasyBeans(List objects) throws MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException, MalformedObjectNameException
  {
    for (Object object : objects)
    {
      register(object);
    }
  }
  
  public void unregisterAll()
  {
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    for (EasyBeanWrapper easyBeanWrapper : beansMap.values())
    {
      try
      {
        server.unregisterMBean(easyBeanWrapper.objectName);
      }
      catch (Exception exc)
      {}
    }
    
    beansMap.clear();
  }

  private class IdentityHashKey
  {
    Object obj;

    IdentityHashKey(Object obj)
    {
      this.obj = obj;
    }

    @Override
    public boolean equals(Object obj)
    {
      return this.obj == obj;
    }

    @Override
    public int hashCode()
    {
      return obj.hashCode();
    }
  }
}
