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

  static <A, B> Pair<A, B> pair(A a, B b)
  {
    return new Pair<A, B>(a, b);
  }
}
