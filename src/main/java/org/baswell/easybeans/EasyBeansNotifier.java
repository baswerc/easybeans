package org.baswell.easybeans;

import javax.management.Notification;

/**
 * Emits JMX notifications. If your bean implements {@link org.baswell.easybeans.EasyBeansNotifierUser} and is wrapped with
 * {@link org.baswell.easybeans.EasyBeanNotificationWrapper} this object will be passed to your bean and can be used to
 * send out {@link javax.management.Notification}.
 *
 */
public interface EasyBeansNotifier
{
  /**
   * @param type The notification type.
   * @param message The notification message.
   */
  void notify(String type, String message);
  
  void notify(Notification notification);
}
