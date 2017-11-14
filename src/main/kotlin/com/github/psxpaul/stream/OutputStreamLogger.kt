package com.github.psxpaul.stream

import org.gradle.api.logging.Logger
import java.io.OutputStream

/**
 * This output stream logs all content written to it using the provided logger.
 */
class OutputStreamLogger(val logger: Logger) : OutputStream() {

    var sb = StringBuilder()

    override fun write(b: Int) {
        var character = b.toChar()
        if(character == '\n') {
            logger.lifecycle(sb.toString())
            sb = StringBuilder()
        }
        else
            sb.append(character)
    }

}