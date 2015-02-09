package org.baswell.easybeans.beans;

import org.baswell.easybeans.EasyBean;
import org.baswell.easybeans.EasyBeanExposure;
import org.baswell.easybeans.EasyBeanOperation;

@EasyBean(description = "A test of operations.", exposure = EasyBeanExposure.ANNOTATED)
public class TestOperationsBean
{

  @EasyBeanOperation(name = "tellMeHello", parameterNames = {"to", "times"}, parameterDescriptions = {"Who to say hello to.", "How many times to say hello."}, parameterDefaultValues = {"", "1"})
  public String sayHello(String to, int times)
  {
    times = Math.max(0, times);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < times; i++)
    {
      if (i > 0) builder.append("\n");
      builder.append("HELLO " + to);
    }
    return builder.toString();
  }
}
