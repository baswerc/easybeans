package org.baswell.easybeans;

import java.lang.reflect.Type;

/**
 * Thrown when a {@link java.lang.reflect.Type} cannot be mapped to an {@link javax.management.openmbean.OpenType}.
 */
public class InvalidEasyBeanOpenType extends EasyBeanException
{
  /**
   * The type that caused this exception.
   */
  public final Type openTypeClass;

  InvalidEasyBeanOpenType(Type openTypeClass, String message)
  {
    super(message + " (" + openTypeClass.getTypeName() + ")");
    this.openTypeClass = openTypeClass;
  }
}
