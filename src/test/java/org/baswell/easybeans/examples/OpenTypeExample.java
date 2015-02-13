package org.baswell.easybeans.examples;

import org.baswell.easybeans.EasyBean;
import org.baswell.easybeans.EasyBeanExposure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EasyBean(exposure = EasyBeanExposure.ALL)
public class OpenTypeExample
{
  public List<String> getNames()
  {
    return Arrays.asList("One", "Two", "Three");
  }

  public Map<String, List<Integer>>  getNumbers()
  {
    Map<String, List<Integer>> numbers = new HashMap<String, List<Integer>>();
    numbers.put("A", Arrays.asList(1, 2, 3, 5));
    numbers.put("B", Arrays.asList(6, 7, 8, 9));
    numbers.put("C", Arrays.asList(10));
    return numbers;
  }

  public Address getAddress()
  {
    return new Address("Mulch lane", null, "Anytown", "NY", "12345");
  }

  public class Address
  {
    public String street1;

    public String street2;

    public String city;

    public String state;

    public String zip;

    public Address(String street1, String street2, String city, String state, String zip)
    {
      this.street1 = street1;
      this.street2 = street2;
      this.city = city;
      this.state = state;
      this.zip = zip;
    }
  }
}
