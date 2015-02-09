package org.baswell.easybeans;

import org.junit.Test;
import static org.junit.Assert.*;

public class BeanDefinitionTest
{
  @Test
  public void testClass() throws Exception
  {
    DuplicateAttributes da = new DuplicateAttributes();

    EasyBeanStructure beanDefinition = new EasyBeanStructure(DuplicateAttributes.class);

    assertEquals(2, beanDefinition.attributes.size());



  }

  class DuplicateAttributes
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
}
