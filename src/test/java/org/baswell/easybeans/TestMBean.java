package org.baswell.easybeans;
import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.baswell.easybeans.beans.One;


public class TestMBean implements DynamicMBean
{
  public static void main(String[] args) throws Exception
  {

    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    ObjectName name = new ObjectName("com.example:type=HelloWorld");
    Object mbean = new One();
    mbs.registerMBean(mbean, name);

    new EasyBeansRegistery().register(new One());
    synchronized (TestMBean.class)
    {
      TestMBean.class.wait();
    }
    
    
//    List<String> list = new ArrayList<String>();
//    System.out.println(implementsIterable(list.getClass()));
    
    /*
    Method[] methods = TestMBean.class.getMethods();
    for (Method constructor : methods)
    {
      System.out.println(constructor.getName());
      Class clazz = constructor.getReturnType();
      System.out.println(clazz);
    }
    */
    
    /*
    TestMBean mbean = new TestMBean();
    ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, new ObjectName("org.test:Name=Test"));
    
    synchronized (TestMBean.class)
    {
      TestMBean.class.wait();
    }
    */
  }
  
  static public boolean implementsIterable(Class clazz)
  {
    Class[] interfaces = clazz.getInterfaces();
    for (Class interf : interfaces)
    {
      if (interf.equals(Iterable.class))
      {
        return true;
      }
      else
      {
        Class[] superInterfaces = interf.getInterfaces();
        for (Class superInterface : superInterfaces)
        {
          if (implementsIterable(superInterface))
          {
            return true;
          }
        }
      }
    }
    
    return false;
  }
  
  private CompositeType compositeType;
  
  private CompositeType subType;
  
  private ArrayType<CompositeType> arrayType;
  
  public TestMBean() throws OpenDataException
  {
    subType = new CompositeType("subType", "This is the description", new String[] {"one", "two"}, new String[] {"one", "two"}, 
        new OpenType[] {SimpleType.STRING, SimpleType.LONG});

    compositeType = new CompositeType("testType", "This is the description", new String[] {"one", "two"}, new String[] {"one", "two"}, 
        new OpenType[] {SimpleType.STRING, subType});
    
    arrayType = new ArrayType<CompositeType>(1, compositeType);
  }
  
  public int test()
  {
    return 0;
  }

  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
  {
    try
    {
      CompositeData[] data = new CompositeData[2];
      
      CompositeData subData = new CompositeDataSupport(subType, new String[] {"one", "two"}, new Object[] {"1B", 1l});
      CompositeData attData = new CompositeDataSupport(compositeType, new String[] {"one", "two"}, new Object[] {"1A", subData});
      
      data[0] = attData;

      subData = new CompositeDataSupport(subType, new String[] {"one", "two"}, new Object[] {"2B", 1l});
      attData = new CompositeDataSupport(compositeType, new String[] {"one", "two"}, new Object[] {"2A", subData});

      data[1] = attData;

      return data;
    }
    catch (OpenDataException exc)
    {
      exc.printStackTrace();
      return null;
    }
  }

  public AttributeList getAttributes(String[] attNames)
  {
    AttributeList attList = new AttributeList();
    for (String attribute : attNames)
    {
      try
      {
        attList.add(new Attribute(attribute, getAttribute(attribute)));
      }
      catch (Exception exc)
      {}
    }
    
    return attList;
  }

  public MBeanInfo getMBeanInfo()
  {
    OpenMBeanAttributeInfo[] attInfo = new OpenMBeanAttributeInfo[1];
    
    OpenMBeanAttributeInfo att = new OpenMBeanAttributeInfoSupport("one", "description", arrayType, true, false, false);
    attInfo[0] = att;
    
    OpenMBeanInfoSupport mbeanInfo = new OpenMBeanInfoSupport("test", "description", attInfo, new OpenMBeanConstructorInfo[0],
        new OpenMBeanOperationInfo[0], new MBeanNotificationInfo[0]);

    return mbeanInfo;
  }

  public Object invoke(String arg0, Object[] arg1, String[] arg2) throws MBeanException, ReflectionException
  {
    // TODO Auto-generated constructor stub
    return null;
  }

  public void setAttribute(Attribute arg0) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
  {
    // TODO Auto-generated constructor stub
    
  }

  public AttributeList setAttributes(AttributeList arg0)
  {
    // TODO Auto-generated constructor stub
    return null;
  }
}
