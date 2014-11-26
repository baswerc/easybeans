package org.baswell.easybeans;

import javax.management.ObjectName;

import junit.framework.TestCase;

public class ObjectNameTest extends TestCase
{
  public void testNoAnnoation()
  {
    EasyBeanWrapper wrapper = new EasyBeanWrapper(new One());
    ObjectName objectName = wrapper.objectName;
    
    assertNotNull(objectName);
    assertEquals("org.easybean", objectName.getDomain());
    assertEquals("One", objectName.getKeyProperty("Name"));
  }
  
  
  public class One
  {}
  
  @EasyBean(objectName="org.helloworld:Name=")
  public class Two
  {}
}
