package org.baswell.easybeans.beans;

import org.baswell.easybeans.EasyBean;
import org.baswell.easybeans.EasyBeanExposure;
import org.baswell.easybeans.EasyBeanTransient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EasyBean(expose = EasyBeanExposure.ALL)
public class TestTypesBean
{
  private SelfReferece selfReferece = new SelfReferece("A", 1, true, "D");
  {
    selfReferece.self = new SelfReferece("B", 2, false, "E");
  }

  public String getTest()
  {
    return "HERE";
  }

  public SelfReferece getSelfReference()
  {
    return selfReferece;
  }

  public List<SelfReferece> getSelfReferences()
  {
    return Arrays.asList(selfReferece, selfReferece, selfReferece);
  }

  public Map<String, List<SelfReferece>> getSelfReferenceMap()
  {
    Map<String, List<SelfReferece>> map = new HashMap<String, List<SelfReferece>>();
    map.put("ONE", Arrays.asList(selfReferece));
    map.put("TWO", Arrays.asList(selfReferece, selfReferece));
    map.put("THREE", Arrays.asList(selfReferece, selfReferece, selfReferece));
    return map;
  }

  public class SelfReferece
  {
    public String a;

    public int b;

    public boolean c;

    private String d;

    public SelfReferece self;

    public SelfReferece(String a, int b, boolean c, String d)
    {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
    }

    public String toString()
    {
      return "A: " + a + ", b: " + b + ", c: " + c;
    }
  }
}
