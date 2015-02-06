package org.baswell.easybeans;

class Pair<X, Y>
{
  X x;
  
  Y y;
  
  Pair(X x, Y y)
  {
    this.x = x;
    this.y = y;
  }

  X getX()
  {
    return x;
  }

  Y getY()
  {
    return y;
  }
}
