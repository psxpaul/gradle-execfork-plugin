package com.pr.gradle.util

import org.gradle.api.GradleException
import java.net.*
import java.util.concurrent.TimeUnit

fun findOpenPort():Int {
    ServerSocket(0).use { return it.localPort }
}

private fun isPortOpen(port:Int):Boolean {
    Socket().use {
        val inetAddress:InetAddress = InetAddress.getByName("127.0.0.1")
        val socketAddress:InetSocketAddress = InetSocketAddress(inetAddress, port)
        try {
            it.connect(socketAddress)
            return true;
        } catch (e:ConnectException) {
            return false
        }
    }
}

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
