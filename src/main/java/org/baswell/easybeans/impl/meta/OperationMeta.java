package org.baswell.easybeans.impl.meta;

import org.baswell.easybeans.impl.OpenTypeMapping;
import org.baswell.easybeans.impl.TypeWrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.baswell.easybeans.impl.OpenTypeMappingCreator.*;

public class OperationMeta
{
  public final String name;

  public final OpenTypeMapping typeMapping;

  public final Class clazz;

  public final Method method;

  public OperationMeta(Class clazz, Method method)
  {
    this.clazz = clazz;
    this.method = method;

    name = method.getName();
    typeMapping = createOpenType(new TypeWrapper(method));
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotationClass)
  {
    return method.getAnnotation(annotationClass);
  }

  public void invoke(Object pojo, Object... parameters) throws IllegalAccessException, InvocationTargetException
  {
    method.invoke(pojo, parameters);
  }
}
