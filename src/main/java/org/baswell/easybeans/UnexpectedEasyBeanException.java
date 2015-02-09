package org.baswell.easybeans;

/**
 * Thrown when an unexpected scenario occurs. This exception could indicate a bug in the EasyBean code.
 */
public class UnexpectedEasyBeanException extends EasyBeanException
{
  UnexpectedEasyBeanException(Throwable cause)
  {
    super(cause);
  }
}
