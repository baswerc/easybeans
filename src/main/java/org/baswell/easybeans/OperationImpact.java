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

import javax.management.MBeanOperationInfo;

/**
 * The enumeration of the {@link javax.management.MBeanOperationInfo} impact integer values.
 * 
 * @see javax.management.MBeanOperationInfo#getImpact()
 */
public enum OperationImpact
{
  /**
   * @see javax.management.MBeanOperationInfo#ACTION
   */
  ACTION,

  /**
   * @see javax.management.MBeanOperationInfo#ACTION_INFO
   */
  ACTION_INFO,

  /**
   * @see javax.management.MBeanOperationInfo#INFO
   */
  INFO,

  /**
   * @see javax.management.MBeanOperationInfo#UNKNOWN
   */
  UNKNOWN;

  /*
   * The JMX integer value of this impact.
   *
   * @see javax.management.MBeanOperationInfo#getImpact()
   */
  int getMBeanImpact()
  {
    switch (this)
    {
      case ACTION:
        return MBeanOperationInfo.ACTION;

      case ACTION_INFO:
        return MBeanOperationInfo.ACTION_INFO;

      case INFO:
        return MBeanOperationInfo.INFO;

      default:
        return MBeanOperationInfo.UNKNOWN;
    }
  }
}
