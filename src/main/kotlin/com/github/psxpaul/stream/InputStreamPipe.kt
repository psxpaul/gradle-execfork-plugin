package com.github.psxpaul.stream

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.LinkedList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Object that will copy the inputStream to the outputStream. You can optionally call waitForPattern()
 * to block until the pattern is seen in the given stream
 *
 * @param inputStream the InputStream to copy to the outputFile
 * @param outputStream the outputStream to copy to
 * @param pattern the optional pattern to wait for when calling waitForPattern()
 */
class InputStreamPipe(val inputStream: InputStream, val outputStream: OutputStream, val pattern: String?) : AutoCloseable {
    val log: Logger = LoggerFactory.getLogger(InputStreamPipe::class.java)

    val patternLength: Int = if (pattern != null) pattern.toByteArray().size else 0
    val patternLatch: CountDownLatch = CountDownLatch(if (pattern != null) 1 else 0)
    val buffer: LinkedList<Int> = LinkedList()
    val thread: Thread = Thread({

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
     * Block until the pattern has been seen in the InputStream
     *
     * @param timeout the maximum number of TimeUnits to wait
     * @param unit the unit of time to wait
     */
    fun waitForPattern(timeout:Long, unit: TimeUnit) {
        patternLatch.await(timeout, unit)
    }

    /**
     * Close the outputFile
     */
    override fun close() {
        log.debug("closing given outputstream")
        outputStream.close()
    }
}
