package org.baswell.easybeans;

import org.baswell.easybeans.beans.TestNotificationsBean;
import org.baswell.easybeans.beans.TestOperationsBean;
import org.baswell.easybeans.beans.TestPublicAttributesBean;
import org.baswell.easybeans.beans.TestTypesBean;

public class JmxRunner
{
  static public void main(String[] args) throws Exception
  {
    EasyBeansRegistery registry = new EasyBeansRegistery();

    registry.register(new TestPublicAttributesBean());
    registry.register(new TestTypesBean());
    registry.register(new TestNotificationsBean());
    registry.register(new TestOperationsBean());

    synchronized (JmxRunner.class)
    {
      JmxRunner.class.wait();
    }
  }
}
