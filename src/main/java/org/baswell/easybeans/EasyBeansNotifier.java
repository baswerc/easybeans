/*
 * Copyright 2015 Corey Baswell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
