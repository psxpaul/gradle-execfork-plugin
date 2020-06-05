package com.github.psxpaul.stream

import io.mockk.MockKAnnotations
import org.junit.Test
import org.gradle.api.logging.Logger
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before

internal class OutputStreamLoggerTest {
    @MockK
    lateinit var logger: Logger

    @Before
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun writeWritesLinesWithoutLF() {
        val sut = OutputStreamLogger(logger)
        val testString = "test"
        testString.forEach {
            sut.write(it.toInt())
        }
        sut.write('\n'.toInt())
        verify { logger.lifecycle(testString) }
    }

    @Test
    fun writeWritesLinesWithoutCR() {
        val sut = OutputStreamLogger(logger)
        val testString = "test"
        testString.forEach {
            sut.write(it.toInt())
        }
        sut.write('\r'.toInt())
        // We need to supply another character to trigger the logging
        sut.write(' '.toInt())
        verify { logger.lifecycle(testString) }
    }

    @Test
    fun writeWritesLinesWithoutCRLF() {
        val sut = OutputStreamLogger(logger)
        val testString = "test"
        testString.forEach {
            sut.write(it.toInt())
        }
        sut.write('\r'.toInt())
        sut.write('\n'.toInt())
        verify { logger.lifecycle(testString) }
    }
}