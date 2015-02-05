package org.baswell.easybeans;

import javax.management.MBeanOperationInfo;

/**
 * The enumeration of the {@link javax.management.MBeanOperationInfo} impact integer values.
 * 
 * @see javax.management.MBeanOperationInfo#getImpact()
 * @see javax.management.MBeanOperationInfo#ACTION
 * @see javax.management.MBeanOperationInfo#ACTION_INFO
 * @see javax.management.MBeanOperationInfo#INFO
 * @see javax.management.MBeanOperationInfo#UNKNOWN
 *
 */
public enum OperationImpact
{
  ACTION,
  ACTION_INFO,
  INFO,
  UNKNOWN;
  
  public int getMBeanImpact()
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
