package org.baswell.easybeans.impl;

public class Pair<X, Y>
{
  private X x;
  
  private Y y;
  
  public Pair(X x, Y y)
  {
    this.x = x;
    this.y = y;
  }

  public X getX()
  {
    return x;
  }

  public Y getY()
  {
    return y;
  }
}
