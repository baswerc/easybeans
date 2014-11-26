package org.baswell.easybeans;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

public class Test
{

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception
  {
    Method method = Test.class.getMethods()[1];
   
    TypeVariable<Method>[] typeVars = method.getTypeParameters();
    System.out.println(typeVars.length);
  }
}
