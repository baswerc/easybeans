package org.baswell.easybeans.beans;

import java.util.List;

import org.baswell.easybeans.EasyBeanAttribute;
import org.baswell.easybeans.Param;

public interface OneMXBean
{

  @EasyBeanAttribute
  public abstract OneMXBean getOne();

  public abstract Action getAction();

  @EasyBeanAttribute
  public abstract String test();

  @EasyBeanAttribute
  public abstract String getHere();

  @EasyBeanAttribute
  public abstract void setHere(String yo);

  public abstract void callMe();

  public abstract void callMe(@Param(value = "with", description = "The with") String with);

  public abstract String callMeX(String with);

  public abstract List<String> getStringList();

  public abstract List<Integer> getIntList();

  public abstract List<Two> getTwoList();
}