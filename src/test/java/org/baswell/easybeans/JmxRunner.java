package org.baswell.easybeans;

import org.baswell.easybeans.beans.One;
import org.baswell.easybeans.beans.PublicAttributes;

import javax.management.JMX;

public class JmxRunner
{
  static public void main(String[] args) throws Exception
  {
    EasyBeansRegistery registry = new EasyBeansRegistery();

    One one = new One();
    registry.register(one);
    registry.register(new PublicAttributes());

    synchronized (JmxRunner.class)
    {
      JmxRunner.class.wait();
    }
  }
}
