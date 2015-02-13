# EasyBeans

EasyBeans is a library aims to make bridging your Java objects to JMX easy. It provides the following functionality:

* A wrapper to turn your Java objects into Dynamic MBeans. The functionality of the MBean can be specified with EasyBean annotations are through a convention based approach.
* A Java object to <a href="http://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/OpenType.html">OpenType</a> converter to make complex objects your object exposes (through attributes or operations) consumable by JMX clients.
* A simplified mechanism for emitting JMX notifications.
* A centralized EasyBean repository for registering and unregistering your Java objects to a MBeanServer.

## Getting Started

### Using Gradle

### Using Maven

## How to use

EasyBeans uses the <a href="http://docs.oracle.com/javase/7/docs/api/javax/management/DynamicMBean.html">DynamicMBean</a> API to make Java
objects accessible via. JMX. To create an MBean from your Java object and expose it via. JMX do the following:

```Java
org.baswell.easybeans.examples.YourClass yourObject = new org.baswell.easybeans.examples.YourClass();
EasyBeanWrapper wrapper = new EasyBeanWrapper(yourObject);
wrapper.register();
```
Now your object is exposed via. JMX. To unregister your object call:

```Java
wrapper.unregister();
```
If `org.baswell.easybeans.examples.YourClass` uses no EasyBean annotations then only public fields and public methods will be exposed. This includes public fields and public methods from all ancestor classes
`org.baswell.easybeans.examples.YourClass` extends from (all the way up the hierarchy chain until a Class that is the `java.` or `javax.` package is reached). Public fields will be exposed as read/write attributes.
Public methods that follow the getter setter convention will be exposed as read/write attributes. All other public methods will be exposed as operations.

### Using EasyBeans Annotations

Below is an example of Java class using EasyBeans annotations to determine how the class is exposed via. JMX:

```Java
import org.baswell.easybeans.EasyBean;
import org.baswell.easybeans.EasyBeanAttribute;
import org.baswell.easybeans.EasyBeanExposure;
import org.baswell.easybeans.EasyBeanOperation;
import org.baswell.easybeans.OperationImpact;

/*
 * If objectName is not provided the object name will be created from your fully qualified
 * class name. Implement the EasyBeanNameProvider to return a dynamic object name.
 */
@EasyBean(objectName = "my.custom:Name=ObjectName",
          description = "The description of what this MBean does.",
          exposure = EasyBeanExposure.ANNOTATED)
public class org.baswell.easybeans.examples.YourClass
{
  // This attribute will read-only since final
  @EasyBeanAttribute(description = "When this object was created.")
  public final long createdAt = System.currentTimeMillis();

  // This attribute will be read-only since readOnly set on annotation
  @EasyBeanAttribute(description = "The name", readOnly = true)
  public String name;

  @EasyBeanAttribute
  public int readWriteAttribute;

  /*
   * This field will not be exposed as an attribute since exposure =
   * EasyBeanExposure.ANNOTATED and there isn't an EasyBeanAttribute. If
   * exposure = EasyBeanExposure.ANNOTATED_READ_ONLY it would be exposed as a read-only
   * attribute. If exposure = EasyBeanExposure.ALL it would be exposed as a read-write
   * attribute.
   */
  public int notExposed;

  private double rate;

  /*
   * This getter setter combination will be exposed as a read-write attribute. If the
   * readOnly value was set to true on the annotation this attribute would be read only.
   */
  @EasyBeanAttribute
  public double getRate()
  {
    return rate;
  }

  public void setRate(double rate)
  {
    this.rate = rate;
  }

  @EasyBeanOperation(description = "Returns the input text the given number of times.",
                     impact = OperationImpact.INFO,
                     parameterNames = {"textToEcho", "numberTimes"})
  public String echo(String textToEcho, int numberTimes)
  {
    String echo = "";
    for (int i = 0; i < numberTimes; i++)
    {
      echo += textToEcho + " ";
    }
    return echo;
  }

  /*
   * This method will not be exposed as an operation since there is no EasyBeanOperaiton
   * and exposure = EasyBeanExposure.ANNOTATED. If exposure = EasyBeanExposure.ALL then
   * this public method would be exposed.
   */
  public void doNothing()
  {}
}
```

### Marking Members Transient

The annotation `EasyBeanTransient` can be used on any class members to make sure that member isn't exposed via. JMX.
If this annotation is present on a class member no other EasyBean annotation can be used in conjunction.


### Converting Objects To Open Types

The JMX API addresses the question of complex data types with Open MBeans. Open MBeans use open types such as
<a href="http://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/ArrayType.html">ArrayType</a>,
<a href="http://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/CompositeType.html">CompositeType</a>,
<a href="http://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/TabularType.html">TabularType</a>, and
<a href="http://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/SimpleType.html">SimpleType</a> to pass
information to and from JMX clients. EasyBeans converts Java objects returned by attributes and operations in a MBean
into one of of these open types so it can be consumeable by a JMX client. For example if your MBean class looks like:

````Java
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
````

EasyBeans will convert `List<String>` to `ArrayType<SimpleType<String>>` and `Map<String, List<Integer>>` will be converted
to a `TabularType`. The custom `Address` class will be converted to a `CompositeType` that contains five `SimpleType<String>'
items (street1, street2, city, state and zip).


### EasyBean Open Type Annotations


### EasyBeans Notifications
