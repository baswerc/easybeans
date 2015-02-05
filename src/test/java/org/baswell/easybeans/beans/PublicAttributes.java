package org.baswell.easybeans.beans;

import org.baswell.easybeans.EasyBean;
import org.baswell.easybeans.EasyBeanExposureLevel;

@EasyBean(exposeLevel = EasyBeanExposureLevel.ALL)
public class PublicAttributes
{
  public String one = "1";

  public int two = 2;

  public boolean three = false;
}
