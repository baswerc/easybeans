package org.baswell.easybeans;

import javax.management.Notification;

public interface NotificationBridge
{
  void notify(String message);
  
  void notify(String type, String message);
  
  void notify(Notification notification);
}
