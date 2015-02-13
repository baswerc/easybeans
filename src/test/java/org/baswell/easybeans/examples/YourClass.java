package org.baswell.easybeans.examples;

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
