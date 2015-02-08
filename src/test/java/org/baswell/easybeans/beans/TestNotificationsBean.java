package org.baswell.easybeans.beans;

import org.baswell.easybeans.EasyBeanNotification;
import org.baswell.easybeans.EasyBeansNotifier;
import org.baswell.easybeans.EasyBeansNotifierUser;

@EasyBeanNotification(types="Test", name = "NotificationOne", description = "Blah blah blah blah.")
public class TestNotificationsBean implements EasyBeansNotifierUser
{
  @Override
  public void setNotifier(final EasyBeansNotifier easyBeansNotifier)
  {
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        while (true)
        {
          try
          {
            Thread.sleep(5000);
          }
          catch (InterruptedException e)
          {}

          easyBeansNotifier.notify("Test", "HELLO: " + System.currentTimeMillis());
        }
      }
    }).start();
  }
}
