package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//用于方法之上
@Target(ElementType.METHOD)
//有效时长
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}
