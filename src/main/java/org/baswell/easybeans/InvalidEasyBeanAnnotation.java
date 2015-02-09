package org.baswell.easybeans;

/**
 * Thrown when an EasyBeans annotation is used incorrectly. For example you can't public a {@link org.baswell.easybeans.EasyBeanAttribute} on
 * a non-public field.
 */
public class InvalidEasyBeanAnnotation extends EasyBeanException
{
  /**
   * The annotated class that caused this exception.
   */
  public final Class annotatedClass;

  InvalidEasyBeanAnnotation(Class annotateClass, String message)
  {
    super(message + " (" + annotateClass.getCanonicalName() + ")");
    this.annotatedClass = annotateClass;
  }
}
