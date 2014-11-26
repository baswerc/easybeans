package org.baswell.easybeans;

/**
 * The exposure level for the MBean for it's attributes, operations, and constructors.
 * 
 */
public enum EasyBeanExposureLevel
{
  /**
   * Only expose annotated members.
   */
  ANNOTATED,
  /**
   * Only expose annotated members and read only attributes.
   */
  READ_ONLY_ATTRIBUTES,
  /**
   * Expose all members.
   */
  ALL;
}
