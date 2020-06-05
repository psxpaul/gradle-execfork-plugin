package com.github.psxpaul.stream

import org.gradle.api.logging.Logger
import java.io.OutputStream

/**
 * This output stream logs all content written to it using the provided logger.
 */
class OutputStreamLogger(private val logger: Logger) : OutputStream() {

    var sb = StringBuilder()
    private var wasCR = false

    override fun write(b: Int) {
        val character = b.toChar()
        if (wasCR || character == '\n') {
            logger.lifecycle(sb.toString())
            wasCR = false
            sb = StringBuilder()
        } else if (character == '\r') {
            wasCR = true
        } else
            sb.append(character)
    }

}