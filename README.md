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
YourClass yourObject = new YourClass();
EasyBeanWrapper wrapper = new EasyBeanWrapper(yourObject);
wrapper.register();
```
Now your object is exposed via. JMX. To unregister your object call:

```Java
wrapper.unregister();
```
If `YourClass` uses no EasyBean annotations then only public fields and public methods will be exposed. This includes public fields and public methods from all ancestor classes
`YourClass` extends from (all the way up the hierarchy chain until a Class that is the `java.` or `javax.` package is reached). Public fields will be exposed as read/write attributes.
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
public class YourClass
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
   * This field will not be exposed as an attribute since exposure = EasyBeanExposure.ANNOTATED
   * and there isn't an EasyBeanAttribute. If exposure = EasyBeanExposure.ANNOTATED_READ_ONLY
   * it would be exposed as a read-only attribute. If exposure = EasyBeanExposure.ALL it would
   * be exposed as a read-write attribute.
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

