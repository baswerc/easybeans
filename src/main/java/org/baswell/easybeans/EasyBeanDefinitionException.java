package org.baswell.easybeans;

public class EasyBeanDefinitionException extends RuntimeException
{
  public EasyBeanDefinitionException(String message)
  {
    super(message);
  }

  public EasyBeanDefinitionException(Throwable cause)
  {
    super(cause);
  }

  public EasyBeanDefinitionException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
