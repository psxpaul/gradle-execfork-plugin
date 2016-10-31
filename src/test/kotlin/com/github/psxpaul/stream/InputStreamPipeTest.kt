package com.github.psxpaul.stream

import org.junit.Assert.assertThat
import org.junit.Test
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.contains
import org.junit.After
import java.io.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class InputStreamPipeTest {
    val outputStream: PipedOutputStream = PipedOutputStream()
    val outputBuffer: BufferedWriter = outputStream.bufferedWriter()
    val inputStream: InputStream = PipedInputStream(outputStream)
    val outputFile: File = File.createTempFile(javaClass.simpleName, ".out")
    val waitForPattern = "Server Started!"
    val logger = InputStreamPipe(inputStream, outputFile, waitForPattern)
    val latch: CountDownLatch = CountDownLatch(1)

    @Test
    fun shouldCopyInputStreamToFile() {
        Thread({ ->
            writeLine("Line One", 100)
            writeLine("Line Two", 100)
            writeLine("Line Three", 100)
            writeLine("Line Four", 100)
            writeLine("Server Started!", 500)
            writeLine("Line Five", 100)
            writeLine("Line Six", 100)
            latch.countDown()
        }).start()
        logger.waitForPattern()

        val outputFileContents:List<String> = FileReader(outputFile).readLines()
        val msg = "outputFileContents: ${outputFileContents.joinToString(separator = "\\n")}"
        assertThat(msg, outputFileContents, hasSize(5))
        assertThat(msg, outputFileContents, contains("Line One", "Line Two", "Line Three", "Line Four", "Server Started!"))

        latch.await()

        val outputFileContentsTwo:List<String> = FileReader(outputFile).readLines()
        val msgTwo = "outputFileContents: ${outputFileContents.joinToString(separator = "\\n")}"
        assertThat(msgTwo, outputFileContentsTwo, hasSize(7))
        assertThat(msgTwo, outputFileContentsTwo, contains("Line One", "Line Two", "Line Three", "Line Four", "Server Started!", "Line Five", "Line Six"))
    }

    @After
    fun cleanup() {
        outputBuffer.close()
        outputStream.close()
    }

    private fun writeLine(output:String, postDelay:Long) {
        outputBuffer.appendln(output)
        outputBuffer.flush()
        Thread.sleep(postDelay)
    }
}
