package org.baswell.easybeans;

import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static org.junit.Assert.*;

public class TestWrapper
{
  @Test
  public void testObjectName()
  {
    EasyBeanWrapper wrapper = new EasyBeanWrapper(new NameFromClassName());
    assertNotNull(wrapper.objectName);

    ObjectNameProvider nameProvider = new ObjectNameProvider("d:type=Foo,name=Bar");
    wrapper = new EasyBeanWrapper(nameProvider);

    assertNotNull(nameProvider.objectName);
    assertEquals(nameProvider.objectName, wrapper.objectName);

    wrapper = new EasyBeanWrapper(new EasyBeanAnnotated());
    assertEquals("hello:type=A,name=Bar", wrapper.objectName.toString());

    try
    {
      new EasyBeanWrapper(new ObjectNameProvider(";"));
      fail("Expected InvalidEasyBeanNameException");
    }
    catch (InvalidEasyBeanNameException e)
    {}

    try
    {
      new EasyBeanWrapper(new EasyBeanNameProvider()
      {
        @Override
        public ObjectName getObjectName() throws MalformedObjectNameException
        {
          return null;
        }
      });
      fail("Expected InvalidEasyBeanNameException");
    }
    catch (InvalidEasyBeanNameException e)
    {}
  }

  @Test
  public void testDescription()
  {
    EasyBeanWrapper wrapper = new EasyBeanWrapper(new EasyBeanAnnotated());
    assertEquals("HELLO WORLD", wrapper.mBeanInfo.getDescription());
  }

  class NameFromClassName
  {}

  @EasyBean(objectName = "hello:type=A,name=Bar", description = "HELLO WORLD")
  class EasyBeanAnnotated
  {}

  class ObjectNameProvider implements  EasyBeanNameProvider
  {

    String objectNameString;

    ObjectName objectName;

    ObjectNameProvider(String objectNameString)
    {
      this.objectNameString = objectNameString;
    }

    @Override
    public ObjectName getObjectName() throws MalformedObjectNameException
    {
      objectName = new ObjectName(objectNameString);
      return objectName;
    }
  }
}
