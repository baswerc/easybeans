package org.baswell.easybeans.beans;

import java.util.Arrays;
import java.util.List;

import javax.management.MXBean;

import org.baswell.easybeans.EasyBeanAttribute;
import org.baswell.easybeans.EasyBean;
import org.baswell.easybeans.EasyBeanConstructor;
import org.baswell.easybeans.EasyBeanDescription;
import org.baswell.easybeans.EasyBeanDescriptions;
import org.baswell.easybeans.EasyBeanExposureLevel;
import org.baswell.easybeans.EasyBeanNotification;
import org.baswell.easybeans.P;

@EasyBean(objectName = "org.test:Name=One", exposeLevel = EasyBeanExposureLevel.ALL)
@MXBean
@EasyBeanDescriptions(
{ @EasyBeanDescription(name = "yo", value = "word up"), @EasyBeanDescription(name = "yox", value = "word up2") })
@EasyBeanNotification(name = "TEST", types =
{ "org.test" })
public class One implements OneMXBean
{
  /*
   * @BeanAttribute public Name getName() { return new Name(); }
   * 
   * @BeanAttribute public List<Name> getNames() { List<Name> names = new ArrayList<Name>(); names.add(new Name()); names.add(new Name()); names.add(new
   * Name()); names.add(new Name()); names.add(new Name()); names.add(new Name()); return names; }
   */

  /*
   * @BeanAttribute public Map<Name, List<? extends Name>> getMap() { Map<Name, List<? extends Name>> map = new HashMap<Name, List<? extends Name>>();
   * map.put(new Name(), Arrays.asList(new Name(), new Name(), new Name())); map.put(new Name("yo", "word"), Arrays.asList(new Name(), new Name(), new Name()));
   * return map; }
   */

  public One()
  {
  }

  @EasyBeanConstructor(name = "Super Constructor", description = "Word up")
  @EasyBeanDescription(name = "one", value = "word")
  public One(@P(value = "yo", description = "Something") String yo)
  {
  }

  /* (non-Javadoc)
   * @see OneMXBean#getOne()
   */
  @EasyBeanAttribute
  public OneMXBean getOne()
  {
    return new One();
  }

  /* (non-Javadoc)
   * @see OneMXBean#getAction()
   */
  public Action getAction()
  {
    return Action.THREE;
  }

  /* (non-Javadoc)
   * @see OneMXBean#test()
   */
  @EasyBeanAttribute
  public String test()
  {
    return "YO";
  }

  /* (non-Javadoc)
   * @see OneMXBean#getHere()
   */
  @EasyBeanAttribute
  public String getHere()
  {
    return "TEST";
  }

  /* (non-Javadoc)
   * @see OneMXBean#setHere(java.lang.String)
   */
  @EasyBeanAttribute
  public void setHere(String yo)
  {
    System.out.println(yo);
  }

  /* (non-Javadoc)
   * @see OneMXBean#callMe()
   */
  public void callMe()
  {
    System.out.println("callMe()");
  }

  /* (non-Javadoc)
   * @see OneMXBean#callMe(java.lang.String)
   */
  public void callMe(@P(value = "with", description = "The with") String with)
  {
    System.out.println("callMe(" + with + ")");
  }

  /* (non-Javadoc)
   * @see OneMXBean#callMeX(java.lang.String)
   */
  public String callMeX(String with)
  {
    return "YO -- " + with;
  }
  
  /* (non-Javadoc)
   * @see OneMXBean#getStringList()
   */
  public List<String> getStringList()
  {
    return Arrays.asList("ONE", "TWO", "THREE", "FOUR");
  }
  
  /* (non-Javadoc)
   * @see OneMXBean#getIntList()
   */
  public List<Integer> getIntList()
  {
    return Arrays.asList(1, 2, 3, 4);
  }
  
  /* (non-Javadoc)
   * @see OneMXBean#getTwoList()
   */
  public List<Two> getTwoList()
  {
    return Arrays.asList(new Two("One", 1, new Three(false)), new Two("Two", 2, new Three(true)), new Two("Three", 3, new Three(true)));
  }
  
  /* (non-Javadoc)
   * @see OneMXBean#getObject()
   */
  public Class getObject()
  {
    return getClass();
  }

  /*
   * @BeanAttribute public Map<Name, Map<String, List<? super Name>>> getArray() { Map<String, List<? super Name>> val1 = new HashMap(); val1.put("ONE",
   * Arrays.asList(new Name(), new Name())); val1.put("TWO", Arrays.asList(new Name(), new Name()));
   * 
   * Map<String, List<? super Name>> val2 = new HashMap(); val2.put("ONE", Arrays.asList(new Name(), new Name())); val2.put("TWO", Arrays.asList(new Name(), new
   * Name()));
   * 
   * Map<Name, Map<String, List<? super Name>>> vals = new HashMap(); vals.put(new Name("1", "2"), val1); vals.put(new Name("1", "3"), val2);
   * 
   * return vals; }
   */
}
