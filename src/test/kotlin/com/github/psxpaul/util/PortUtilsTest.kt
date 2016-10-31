package com.github.psxpaul.util

import org.gradle.api.GradleException
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PortUtilsTest {
    @Test
    fun testFindOpenPort() {
        val port = findOpenPort()
        assertThat(port, greaterThanOrEqualTo(1024))
        assertThat(port, lessThanOrEqualTo(65535))

        try {
            Socket(InetAddress.getLoopbackAddress(), port).use { fail("Socket should not have been in use already!") }
        } catch (e: ConnectException) {
            assertThat(e.message, containsString("Connection refused"))
        }
    }

    @Test(timeout=2000)
    fun testWaitForPortOpen_timeout() {
        val stubProcess:Process = StubProcess()
        val port = findOpenPort()

        try {
            waitForPortOpen(port, 1, TimeUnit.SECONDS, stubProcess)
        } catch (e:Exception) {
            assertThat(e, instanceOf(GradleException::class.java))
            assertThat(e.message, equalTo("Timed out waiting for port $port to be opened"))
        }
    }

    @Test(timeout=2000)
    fun testWaitForPortOpen_processDied() {
        val stubProcess:Process = StubProcess(false)
        val port = findOpenPort()

        try {
            waitForPortOpen(port, 1, TimeUnit.MINUTES, stubProcess)
        } catch (e:Exception) {
            assertThat(e, instanceOf(GradleException::class.java))
            assertThat(e.message, equalTo("Process died before port $port was opened"))
        }
    }

    @Test(timeout=2000)
    fun testWaitForPortOpen_success() {
        val stubProcess: Process = StubProcess()
        val port = findOpenPort()
        val latch = CountDownLatch(1)

        Thread({
            ServerSocket(port, 1, InetAddress.getLoopbackAddress()).use {
                it.accept()
                latch.countDown()
            }
        }).start()

        waitForPortOpen(port, 1, TimeUnit.MINUTES, stubProcess)
        latch.await(1, TimeUnit.SECONDS)
        assertThat(latch.count, equalTo(0L))
    }

    class StubProcess(val alive:Boolean = true) : Process() {
        override fun destroy() {}
        override fun exitValue(): Int { return 0 }
        override fun getOutputStream(): OutputStream? { return null }
        override fun getErrorStream(): InputStream? { return null }
        override fun getInputStream(): InputStream? { return null }
        override fun waitFor(): Int { return 0 }

        override fun isAlive():Boolean {
            return alive
        }
    }
}