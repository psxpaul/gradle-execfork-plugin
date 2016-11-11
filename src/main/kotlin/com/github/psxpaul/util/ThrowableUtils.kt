package com.github.psxpaul.util

/**
 * Loop through the causes of a Throwable, until the root
 * cause is found.
 *
 * @param t the Throwable to find nested causes in
 * @return the most-nestedest Throwable
 */
fun rootCauseOf(t:Throwable):Throwable  {
    val cause: Throwable = t.cause ?: return t
    return rootCauseOf(cause)
}
