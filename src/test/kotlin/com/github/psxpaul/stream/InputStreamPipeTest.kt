package com.github.psxpaul.stream

import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.After
import java.io.*
import java.util.concurrent.CountDownLatch

class InputStreamPipeTest {
    val outputStream: PipedOutputStream = PipedOutputStream()
    val outputBuffer: BufferedWriter = outputStream.bufferedWriter()
    val inputStream: InputStream = PipedInputStream(outputStream)
    val pipeOutput = ByteArrayOutputStream()
    val waitForPattern = "Server Started!"
    val logger = InputStreamPipe(inputStream, pipeOutput, waitForPattern)
    val latch: CountDownLatch = CountDownLatch(1)

    @Test
    fun shouldCopyInputStreamToOutputStream() {
        shouldFindPatternFromLines("Line One", "Line Two", "Line Three", "Line Four", "Server Started!", "Line Five", "Line Six")
    }

    @Test
    fun shouldFindInLastLine() {
        shouldFindPatternFromLines("Line One", "Line Two", "Server Started!")
    }

    @Test
    fun shouldFindInFirstLine() {
        shouldFindPatternFromLines("Server Started!","Line Two","Line Three")
    }

    private fun shouldFindPatternFromLines(vararg lines: String) {
        Thread({
            lines.forEach { line -> writeLine(line, 100) }
            latch.countDown()
        }).start()
        logger.waitForPattern()

        val outputFileContents:List<String> = splitAndRemoveExtraEmptyString()
        val msg = "outputFileContents: ${outputFileContents.joinToString(separator = System.lineSeparator())}"

        assertThat(msg, outputFileContents, `is`(allLinesUntilPattern(lines)))

        latch.await()

        val outputFileContentsTwo:List<String> = splitAndRemoveExtraEmptyString()
        val msgTwo = "outputFileContents: ${outputFileContents.joinToString(separator = System.lineSeparator())}"
        assertThat(msgTwo, outputFileContentsTwo, contains(*lines))
    }

    private fun allLinesUntilPattern(lines: Array<out String>): List<String> {
        val takeWhile: MutableList<String> = lines.takeWhile { i -> i != "Server Started!" }.toMutableList()
        takeWhile.add("Server Started!")
        return takeWhile.toList()
    }

    private fun splitAndRemoveExtraEmptyString() = String(pipeOutput.toByteArray()).split(System.lineSeparator()).filter { i -> i != "" }

    @After
    fun cleanup() {
        outputBuffer.close()
        outputStream.close()
    }

    private fun writeLine(output:String, postDelay:Long) {
        outputBuffer.appendLine(output)
        outputBuffer.flush()
        Thread.sleep(postDelay)
    }
}
