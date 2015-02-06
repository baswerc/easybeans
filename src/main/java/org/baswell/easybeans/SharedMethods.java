package org.baswell.easybeans;

class SharedMethods
{
  static boolean hasContent(String string)
  {
    return string != null && !string.trim().isEmpty();
  }

  static String capatalize(String text)
  {
    if (text.length() > 2)
    {
      return text.substring(0, 1).toUpperCase() + text.substring(1, text.length());
    }
    else
    {
      return text.toUpperCase();
    }
  }
}
