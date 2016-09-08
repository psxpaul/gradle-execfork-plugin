package com.pr.gradle.util

fun rootCauseOf(t:Throwable):Throwable  {
    val cause: Throwable = t.cause ?: return t
    return rootCauseOf(cause)
}
