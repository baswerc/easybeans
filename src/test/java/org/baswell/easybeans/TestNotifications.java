package org.baswell.easybeans;

import org.junit.*;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.*;

public class TestNotifications
{
  @org.junit.Test
  public void testNotification() throws Exception
  {
    NotificationSender sender = new NotificationSender();
    EasyBeanNotificationWrapper wrapper = new EasyBeanNotificationWrapper(sender);
    EasyBeansRegistery registry = new EasyBeansRegistery();

    registry.register(wrapper);


    assertNotNull(sender.easyBeansNotifier);

    TestNotificationListener listener = new TestNotificationListener();
    ManagementFactory.getPlatformMBeanServer().addNotificationListener(wrapper.objectName, listener, null , null);

    sender.easyBeansNotifier.notify("TEST", "HELLO");

    assertNotNull(listener.notification);
    assertEquals("TEST", listener.notification.getType());
    assertEquals("HELLO", listener.notification.getMessage());
  }

  class NotificationSender implements EasyBeansNotifierUser
  {
    EasyBeansNotifier easyBeansNotifier;

    @Override
    public void setNotifier(EasyBeansNotifier easyBeansNotifier)
    {
      this.easyBeansNotifier = easyBeansNotifier;
    }
  }

  class TestNotificationListener implements NotificationListener
  {
    Notification notification;

    Object handback;

    @Override
    public void handleNotification(Notification notification, Object handback)
    {
      this.notification = notification;
      this.handback = handback;
    }

    void reset()
    {
      notification = null;
      handback = null;
    }
  }

}
