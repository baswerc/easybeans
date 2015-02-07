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
