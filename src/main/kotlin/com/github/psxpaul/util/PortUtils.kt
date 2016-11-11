package com.github.psxpaul.util

import org.gradle.api.GradleException
import java.net.*
import java.util.concurrent.TimeUnit

/**
 * Find a random port that is available to listen on
 * @return a port number that is available
 */
fun findOpenPort():Int {
    ServerSocket(0).use { return it.localPort }
}

/**
 * Check if a given port is in use
 * @param port the port number to check
 * @return true if the port is open, false otherwise
 */
private fun isPortOpen(port:Int):Boolean {
    Socket().use {
        val inetAddress: InetAddress = InetAddress.getByName("127.0.0.1")
        val socketAddress: InetSocketAddress = InetSocketAddress(inetAddress, port)
        try {
            it.connect(socketAddress)
            return true;
        } catch (e: ConnectException) {
            return false
        }
    }
}

/**
 * Wait for a given amount of time for a port to be opened locally, by a given
 * process. This method will poll every 100 ms and try to connect to the port. If
 * the amount of time is reached and the port is not yet open, a GradleException
 * is thrown. If the given process terminates before the port is open, a
 * GradleException is thrown.
 *
 * @param port the port number to check
 * @param timeout the maximum number of TimeUnits to wait
 * @param unit the unit of time to wait
 * @param process the process to monitor for early termination
 *
 * @throws GradleException when the timeout has elapsed and the port is still
 *          not open, OR the given process has terminated before the port is
 *          opened (whichever occurs first)
 */
fun waitForPortOpen(port:Int, timeout:Long, unit: TimeUnit, process:Process) {
    val millisToWait:Long = unit.toMillis(timeout)
    val waitUntil:Long = System.currentTimeMillis() + millisToWait

    while (System.currentTimeMillis() < waitUntil) {
        Thread.sleep(100)
        if (!process.isAlive) throw GradleException("Process died before port $port was opened")
        if (isPortOpen(port)) return
    }

    throw GradleException("Timed out waiting for port $port to be opened")
}
