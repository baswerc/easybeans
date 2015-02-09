package org.baswell.easybeans;

/**
 * Base class for all EasyBean related exceptions.
 */
abstract class EasyBeanException extends RuntimeException
{
  EasyBeanException(String message)
  {
    super(message);
  }

  EasyBeanException(Throwable cause)
  {
    super(cause);
  }
}
