package com.github.psxpaul.stream

import org.gradle.api.GradleException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
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
class InputStreamPipe(private val inputStream: InputStream, private val outputStream: OutputStream, private val pattern: String?) : AutoCloseable {
    private val log: Logger = LoggerFactory.getLogger(InputStreamPipe::class.java)

    private val patternLength: Int = pattern?.toByteArray()?.size ?: 0
    private val patternLatch: CountDownLatch = CountDownLatch(if (pattern != null) 1 else 0)
    private val buffer: LinkedList<Int> = LinkedList()
    private val thread: Thread = Thread {

        var byte: Int = inputStream.safeRead()
        while (byte != -1) {
            outputStream.write(byte)
            outputStream.flush()

            if (patternLength == 0 || patternLatch.count == 0L) {
                log.debug("skipping pattern checking")
            } else if (buffer.size < patternLength - 1) {
                buffer.addLast(byte)
            } else {
                buffer.addLast(byte)
                val bufferStr = String(buffer.map(Int::toByte).toByteArray())

                log.debug("checking if |${bufferStr.replace("\n", "\\n")}| equals |$pattern|")
                if (bufferStr == pattern) {
                    patternLatch.countDown()
                }
                buffer.removeFirst()
            }

            byte = inputStream.safeRead()
        }
        close()
    }

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
    fun waitForPattern(timeout: Long, unit: TimeUnit) {
        if (!patternLatch.await(timeout, unit)) {
            throw GradleException("The waitForOutput pattern did not appear before timeout was reached.")
        }
    }

    /**
     * Close the outputFile
     */
    override fun close() {
        log.debug("closing given outputstream")
        outputStream.close()
    }
}

fun InputStream.safeRead(): Int {
    return try {
        read()
    } catch (e: IOException) {
        if (e.message == "Stream closed") {
            -1
        } else {
            throw e
        }
    }
}