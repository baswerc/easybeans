package org.baswell.easybeans;

/**
 *
 * The exposure level for an object's attributes.
 * 
 */
public enum EasyBeanOpenTypeExposure
{
  /**
   * Only exposure the data in public fields and getter methods and constructors annotated with {@link org.baswell.easybeans.EasyBeanOpenTypeAttribute}.
   */
  ANNOTATED,

  /**
   * Expose the data in all public fields and getter methods.
   */
  ALL;
}
