package org.baswell.easybeans.beans;

public class Two
{
  private String yo;
  
  private int dog;
  
  private Three three;
  
  public Two(String yo, int dog, Three three)
  {
    this.yo = yo;
    this.dog = dog;
    this.three = three;
  }

  public String getYo()
  {
    return yo;
  }

  public int getDog()
  {
    return dog;
  }

  public Three getThree()
  {
    return three;
  }
}
