package org.baswell.easybeans.beans;

import org.baswell.easybeans.EasyBean;
import org.baswell.easybeans.EasyBeanAttribute;
import org.baswell.easybeans.EasyBeanExposure;

@EasyBean(exposure = EasyBeanExposure.ALL)
public class TestPublicAttributesBean
{
  public String one = "1";

  public int two = 2;

  public boolean three = false;

  public final String readOnly = "A";

  public static String staticField = "B";

  @EasyBeanAttribute(readOnly = true)
  public String annotatedReadOnly = "ReadOnly";

  private int test;

  @EasyBeanAttribute(readOnly = true)
  public int getTest()
  {
    return test;
  }

  public void setTest(int test)
  {
    this.test = test;
  }
}
