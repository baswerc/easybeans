package org.baswell.easybeans;

/**
 *
 * The exposure level for the MBean for it's attributes, operations, and constructors.
 * 
 */
public enum EasyBeanExposure
{
  /**
   * Only exposure public fields, methods and constructors annotated with {@link org.baswell.easybeans.EasyBeanAttribute} ,
   * {@link org.baswell.easybeans.EasyBeanOperation} or {@link org.baswell.easybeans.EasyBeanConstructor}.
   */
  ANNOTATED,

  /**
   * Expose any annotated public fields, methods and constructors and any unannotated public fields (read only) and getter methods.
   */
  ANNOTATED_AND_READ_ONLY,

  /**
   * Expose any annotated public fields, methods and constructors and any unannotated public fields (read & write) and methods.
   */
  ALL;
}
