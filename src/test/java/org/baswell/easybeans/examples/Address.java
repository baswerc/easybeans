package org.baswell.easybeans.examples;

import org.baswell.easybeans.EasyBeanOpenType;
import org.baswell.easybeans.EasyBeanOpenTypeAttribute;
import org.baswell.easybeans.EasyBeanOpenTypeExposure;
import org.baswell.easybeans.EasyBeanTransient;

@EasyBeanOpenType(description = "A street address", exposure = EasyBeanOpenTypeExposure.ALL)
public class Address
{
  @EasyBeanOpenTypeAttribute(name = "street")
  public String street1;

  @EasyBeanTransient
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