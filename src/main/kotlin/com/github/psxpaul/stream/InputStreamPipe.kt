package com.github.psxpaul.stream

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.LinkedList
import java.util.concurrent.CountDownLatch

/**
 * Object that will copy the inputStream to the outputFile. You can optionally call waitForPattern()
 * to block until the pattern is seen in the outputFile
 *
 * @param inputStream the InputStream to copy to the outputFile
 * @param outputFile the outputFile to copy to
 * @param pattern the optional pattern to wait for when calling waitForPattern()
 */
class InputStreamPipe(val inputStream: InputStream, val outputFile: File, val pattern: String?) : AutoCloseable {
    val log: Logger = LoggerFactory.getLogger(InputStreamPipe::class.java)

    val outputStream: FileOutputStream = FileOutputStream(outputFile)
    val patternLength: Int = if (pattern != null) pattern.toByteArray().size else 0
    val patternLatch: CountDownLatch = CountDownLatch(if (pattern != null) 1 else 0)
    val buffer: LinkedList<Int> = LinkedList()
    val thread: Thread = Thread({
        log.debug("writing to ${outputFile.absolutePath}")

        var byte:Int = inputStream.read()
        while(byte != -1) {
            outputStream.write(byte)
            outputStream.flush()

            if (patternLength == 0 || patternLatch.count == 0L) {
                log.debug("skipping pattern checking")
            } else if (buffer.size < patternLength) {
                buffer.addLast(byte)
            } else {
                buffer.removeFirst()
                buffer.addLast(byte)

                val bufferStr: String = String(buffer.map(Int::toByte).toByteArray())
                log.debug("checking if |${bufferStr.replace("\n", "\\n")}| equals |$pattern|")
                if (bufferStr == pattern) {
                    patternLatch.countDown()
                }
            }

            byte = inputStream.read()
        }
        close()
    })
    init {
        thread.start()
    }

    /**
     * Block until the pattern has been seen in the InputStream
     */
    fun waitForPattern() {
        patternLatch.await()
    }

    /**
     * Close the outputFile
     */
    override fun close() {
        log.debug("closing file ${outputFile.absolutePath}")
        outputStream.close()
    }
}
