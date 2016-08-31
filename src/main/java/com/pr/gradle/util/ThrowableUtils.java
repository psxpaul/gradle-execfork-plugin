package com.pr.gradle.util;

public class ThrowableUtils {

  public static Throwable rootCauseOf(Throwable t) {
    if (t.getCause() == null) return t;
    return rootCauseOf(t.getCause());
  }
}