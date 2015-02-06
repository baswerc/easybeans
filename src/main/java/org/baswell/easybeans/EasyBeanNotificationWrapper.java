package org.baswell.easybeans;

import javax.management.*;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.baswell.easybeans.SharedMethods.getDescriptor;

public class EasyBeanNotificationWrapper extends EasyBeanWrapper implements NotificationEmitter
{
  List<NotificationListenerEntry> listenerEntries;

  public EasyBeanNotificationWrapper(Object pojo) throws EasyBeanDefinitionException
  {
    super(pojo);
  }

  /**
   *
   * @see javax.management.NotificationEmitter#getNotificationInfo()
   */
  @Override
  public MBeanNotificationInfo[] getNotificationInfo()
  {
    if (pojo instanceof NotificationBroadcaster)
    {
      return ((NotificationBroadcaster) pojo).getNotificationInfo();
    }
    else
    {
      return info.getNotifications();
    }
  }

  /**
   *
   * @see javax.management.NotificationEmitter#addNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, Object)
   */
  @Override
  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
  {
    if (pojo instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster) pojo).addNotificationListener(listener, filter, handback);
    }
    else
    {
      listenerEntries.add(new NotificationListenerEntry(listener, filter, handback));
    }
  }

  /**
   *
   * @see javax.management.NotificationEmitter#removeNotificationListener(NotificationListener, NotificationFilter, Object)
   */
  @Override
  public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
  {
    if (pojo instanceof NotificationEmitter)
    {
      ((NotificationEmitter) pojo).removeNotificationListener(listener, filter, handback);
    }
    else if (pojo instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster) pojo).removeNotificationListener(listener);
    }
    else
    {
      for (int i = (listenerEntries.size() - 1); i >= 0; i--)
      {
        NotificationListenerEntry listenerEntry = (NotificationListenerEntry)listenerEntries.get(i);
        if (listenerEntry.equals(listener, filter, handback))
        {
          listenerEntries.remove(i);
        }
      }
    }
  }

  /**
   *
   * @see NotificationEmitter#removeNotificationListener(NotificationListener)
   */
  @Override
  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
  {
    if (pojo instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster) pojo).removeNotificationListener(listener);
    }
    else
    {
      for (int i = (listenerEntries.size() - 1); i >= 0; i--)
      {
        NotificationListenerEntry listenerEntry = (NotificationListenerEntry)listenerEntries.get(i);
        if (listenerEntry.equals(listener))
        {
          listenerEntries.remove(i);
        }
      }
    }
  }

  @Override
  MBeanNotificationInfo[] loadNotificationInfo()
  {
    if (pojo instanceof NotificationBroadcaster)
    {
      return ((NotificationBroadcaster) pojo).getNotificationInfo();
    }
    else
    {
      Annotation[] annotations = pojo.getClass().getAnnotations();
      List<MBeanNotificationInfo> notificationInfoList = new ArrayList<MBeanNotificationInfo>();

      for (Annotation annotation : annotations)
      {
        if (annotation instanceof EasyBeanNotification)
        {
          EasyBeanNotification notificationMeta = (EasyBeanNotification)annotation;
          String name = notificationMeta.name();
          String description = notificationMeta.description();
          String[] notifyTypes = notificationMeta.types();
          notificationInfoList.add(new ModelMBeanNotificationInfo(notifyTypes, name, description, getDescriptor(Arrays.asList(notificationMeta.descriptor()))));
        }
        else if (annotation instanceof EasyBeanNotifications)
        {
          for (EasyBeanNotification notificationMeta : ((EasyBeanNotifications)annotation).value())
          {
            String name = notificationMeta.name();
            String description = notificationMeta.description();
            String[] notifyTypes = notificationMeta.types();
            notificationInfoList.add(new ModelMBeanNotificationInfo(notifyTypes, name, description, getDescriptor(Arrays.asList(notificationMeta.descriptor()))));
          }
        }
      }

      listenerEntries = new ArrayList<NotificationListenerEntry>();
      return notificationInfoList.toArray(new ModelMBeanNotificationInfo[notificationInfoList.size()]);
    }
  }

  private class NotificationListenerEntry
  {
    private NotificationListener listener;
    private NotificationFilter filter;
    private Object handback;

    public NotificationListenerEntry(NotificationListener listener, NotificationFilter filter, Object handback)
    {
      this.listener = listener;
      this.filter = filter;
      this.handback = handback;
    }

    public void notifyIfNotFiltered(Notification notification)
    {
      if ((filter == null) || (filter.isNotificationEnabled(notification)))
      {
        listener.handleNotification(notification, handback);
      }
    }

    public boolean equals(NotificationListener listener, NotificationFilter filter, Object handback)
    {
      return ((this.listener == listener) && (this.filter == filter) && (this.handback == handback));
    }

    public boolean equals(NotificationListener listener)
    {
      return (this.listener == listener);
    }
  }

}
