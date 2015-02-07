package org.baswell.easybeans;

import javax.management.Notification;

public interface EasyBeansNotifier
{
  void notify(String type, String message);
  
  void notify(Notification notification);
}
