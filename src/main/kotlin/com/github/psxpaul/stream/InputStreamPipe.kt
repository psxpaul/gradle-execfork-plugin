package com.github.psxpaul.stream

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.LinkedList
import java.util.concurrent.CountDownLatch

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

            outputStream.write(byte)
            outputStream.flush()
            byte = inputStream.read()
        }
        close()
    })
    init {
        thread.start()
    }

    fun waitForPattern() {
        patternLatch.await()
    }

    override fun close() {
        log.debug("closing file ${outputFile.absolutePath}")
        outputStream.close()
    }
}
