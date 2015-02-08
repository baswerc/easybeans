package org.baswell.easybeans;

import javax.management.*;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.baswell.easybeans.SharedMethods.getDescriptor;

public class EasyBeanNotificationWrapper extends EasyBeanWrapper implements NotificationEmitter, EasyBeansNotifier
{
  List<NotificationListenerDatum> notificationListenerData;

  private AtomicLong sequenceNumberGenerator;

  public EasyBeanNotificationWrapper(Object pojo) throws InvalidEasyBeanNameException, InvalidEasyBeanAnnotation, InvalidEasyBeanOpenType
  {
    super(pojo);

    if (pojo instanceof EasyBeansNotifierUser)
    {
      ((EasyBeansNotifierUser)pojo).setNotifier(this);
      sequenceNumberGenerator = new AtomicLong();
      notificationListenerData = new ArrayList<NotificationListenerDatum>();
    }
  }

  @Override
  public void notify(String type, String message)
  {
    notify(new Notification(type, bean.getClass().getName(), sequenceNumberGenerator.incrementAndGet(), message));
  }

  @Override
  public void notify(Notification notification)
  {
    for (NotificationListenerDatum notificationListenerDatum : notificationListenerData)
    {
      notificationListenerDatum.notifyIfNotFiltered(notification);
    }
  }

  @Override
  public MBeanNotificationInfo[] getNotificationInfo()
  {
    return mBeanInfo.getNotifications();
  }

  @Override
  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
  {
    if (bean instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster) bean).addNotificationListener(listener, filter, handback);
    }

    if (bean instanceof EasyBeansNotifierUser)
    {
      notificationListenerData.add(new NotificationListenerDatum(listener, filter, handback));
    }
  }

  @Override
  public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
  {
    if (bean instanceof NotificationEmitter)
    {
      ((NotificationEmitter) bean).removeNotificationListener(listener, filter, handback);
    }
    else if (bean instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster) bean).removeNotificationListener(listener);
    }

    if (bean instanceof EasyBeansNotifierUser)
    {
      for (int i = (notificationListenerData.size() - 1); i >= 0; i--)
      {
        NotificationListenerDatum listenerEntry = (NotificationListenerDatum) notificationListenerData.get(i);
        if (listenerEntry.equals(listener, filter, handback))
        {
          notificationListenerData.remove(i);
        }
      }
    }
  }

  @Override
  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
  {
    if (bean instanceof NotificationBroadcaster)
    {
      ((NotificationBroadcaster) bean).removeNotificationListener(listener);
    }

    if (bean instanceof EasyBeansNotifierUser)
    {
      for (int i = (notificationListenerData.size() - 1); i >= 0; i--)
      {
        NotificationListenerDatum listenerEntry = (NotificationListenerDatum) notificationListenerData.get(i);
        if (listenerEntry.equals(listener))
        {
          notificationListenerData.remove(i);
        }
      }
    }
  }

  @Override
  MBeanNotificationInfo[] loadNotificationInfo()
  {
    MBeanNotificationInfo[] notificationInfo = null;

    if (bean instanceof NotificationBroadcaster)
    {
      notificationInfo = ((NotificationBroadcaster) bean).getNotificationInfo();
    }

    if (notificationInfo != null && notificationInfo.length > 0)
    {
      return notificationInfo;
    }
    else
    {
      Annotation[] annotations = bean.getClass().getAnnotations();
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

      return notificationInfoList.toArray(new ModelMBeanNotificationInfo[notificationInfoList.size()]);
    }
  }

  private class NotificationListenerDatum
  {
    private NotificationListener listener;
    private NotificationFilter filter;
    private Object handback;

    public NotificationListenerDatum(NotificationListener listener, NotificationFilter filter, Object handback)
    {
      this.listener = listener;
      this.filter = filter;
      this.handback = handback;
    }

    void notifyIfNotFiltered(Notification notification)
    {
      if ((filter == null) || (filter.isNotificationEnabled(notification)))
      {
        listener.handleNotification(notification, handback);
      }
    }

    boolean equals(NotificationListener listener, NotificationFilter filter, Object handback)
    {
      return ((this.listener == listener) && (this.filter == filter) && (this.handback == handback));
    }

    boolean equals(NotificationListener listener)
    {
      return (this.listener == listener);
    }
  }
}
