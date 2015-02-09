package org.baswell.easybeans.beans;

import org.baswell.easybeans.EasyBean;
import org.baswell.easybeans.EasyBeanExposure;

@EasyBean(exposure = EasyBeanExposure.ALL)
public class TestPublicAttributesBean
{
  public String one = "1";

  public int two = 2;

  public boolean three = false;
}
