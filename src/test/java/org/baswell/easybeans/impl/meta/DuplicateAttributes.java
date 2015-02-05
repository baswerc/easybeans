package org.baswell.easybeans.impl.meta;

import org.baswell.easybeans.EasyBeanAttribute;

public class DuplicateAttributes
{
  public String one;

  public Integer two;

  public String getOne()
  {
    return this.one;
  }

  public void setOne(String one)
  {
    this.one = one;
  }

  @EasyBeanAttribute
  public String myTest()
  {
    return "ONE";
  }

  @EasyBeanAttribute
  public void myTest(String value)
  {

  }

}
