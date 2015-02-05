package org.baswell.easybeans.impl.meta;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClassMetaTest
{
  @Test
  public void testClass() throws Exception
  {
    DuplicateAttributes da = new DuplicateAttributes();

    ClassMeta classMeta = new ClassMeta(DuplicateAttributes.class);

    assertEquals(2, classMeta.attributes.size());



  }
}
