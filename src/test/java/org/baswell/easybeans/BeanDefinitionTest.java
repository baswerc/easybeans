package org.baswell.easybeans;

import org.junit.Test;
import static org.junit.Assert.*;

public class BeanDefinitionTest
{
  @Test
  public void testClass() throws Exception
  {
    DuplicateAttributes da = new DuplicateAttributes();

    BeanDefinition beanDefinition = new BeanDefinition(DuplicateAttributes.class);

    assertEquals(2, beanDefinition.attributes.size());



  }
}
