package org.baswell.easybeans;

public class EasyBeanException extends RuntimeException
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
