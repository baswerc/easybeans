package org.baswell.easybeans;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestInvalidAnnotations
{
  @Test(expected = InvalidEasyBeanAnnotation.class)
  public void testPrivateFieldAnnotated()
  {
    new EasyBeanWrapper(new TestOne());
    fail("Expected InvalidEasyBeanAnnotation exception.");
  }

  @Test(expected = InvalidEasyBeanAnnotation.class)
  public void testFieldAnnotatedWithTransient()
  {
    new EasyBeanWrapper(new TestTwo());
    fail("Expected InvalidEasyBeanAnnotation exception.");
  }

  @Test(expected = InvalidEasyBeanAnnotation.class)
  public void testPrivateGetterAnnotated()
  {
    new EasyBeanWrapper(new TestThree());
    fail("Expected InvalidEasyBeanAnnotation exception.");
  }

  @Test(expected = InvalidEasyBeanAnnotation.class)
  public void testGetterAnnotatedWithTransient()
  {
    new EasyBeanWrapper(new TestFour());
    fail("Expected InvalidEasyBeanAnnotation exception.");
  }

  @Test(expected = InvalidEasyBeanAnnotation.class)
  public void testPrivateOperationAnnotated()
  {
    new EasyBeanWrapper(new TestFive());
    fail("Expected InvalidEasyBeanAnnotation exception.");
  }

  @Test(expected = InvalidEasyBeanAnnotation.class)
  public void testOperationAnnotatedWithTransient()
  {
    new EasyBeanWrapper(new TestSix());
    fail("Expected InvalidEasyBeanAnnotation exception.");
  }

  @Test(expected = InvalidEasyBeanAnnotation.class)
  public void testOperationAttributeAnnotated()
  {
    new EasyBeanWrapper(new TestSeven());
    fail("Expected InvalidEasyBeanAnnotation exception.");
  }


  class TestOne
  {
    @EasyBeanAttribute
    private String attribute;
  }

  class TestTwo
  {
    @EasyBeanAttribute
    @EasyBeanTransient
    public String attribute;
  }

  class TestThree
  {
    @EasyBeanAttribute
    private String getAttribute()
    {
      return null;
    }
  }

  class TestFour
  {
    @EasyBeanAttribute
    @EasyBeanTransient
    public String getAttribute()
    {
      return null;
    }
  }

  class TestFive
  {
    @EasyBeanOperation
    private void operation()
    {}
  }

  class TestSix
  {
    @EasyBeanOperation
    @EasyBeanTransient
    public void operation()
    {}
  }

  class TestSeven
  {
    @EasyBeanAttribute
    @EasyBeanOperation
    public void operation()
    {}
  }
}
