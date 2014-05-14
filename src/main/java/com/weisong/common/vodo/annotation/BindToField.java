package com.weisong.common.vodo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BindToField {

  /**
   * This annotation forces the source object of an attribute to be
   * a different source object with this class other than defined by
   * @BindToClass defined at VO class level.
   */
  String clazz() default "";
  
  /**
   * Name of the field, it's format is
   * <field>.<field>...
   */
  String value();
}
