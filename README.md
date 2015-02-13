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
If YourClass uses no EasyBean annotations then only public fields and public methods will be exposed. This includes public fields and public methods from all ancestor classes
YourClass extends from (all the way up the hierachy chain until a Class that is the java. or javax. package is reached). Public fields will be exposed as read/write attributes.
Public methods that follow the getter setter convetion will be exposed as read/write attributes. All other public methods will be exposed as operations.

### Using EasyBeans Annotations



