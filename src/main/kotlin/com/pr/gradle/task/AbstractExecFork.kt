package com.pr.gradle.task

import com.pr.gradle.util.waitForPortOpen
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

abstract class AbstractExecFork : DefaultTask() {
    val log: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    var workingDir: String = project.projectDir.absolutePath
    var args: MutableList<String> = mutableListOf()
    var environment: MutableMap<String, Any> = mutableMapOf()
    var standardOutput: String? = null
    var errorOutput: String? = null

    var waitForPort: Int? = null
    var process: Process? = null

    var stopAfter: Task? = null
        set(value: Task?) {
            val joinTaskVal = joinTask
            if (joinTaskVal != null) {
                log.info("Adding '{}' as a finalizing task to '{}'", joinTaskVal.getName(), value?.getName())
                value?.finalizedBy(joinTask)
            }
            field = value
        }

    var joinTask: ExecJoin? = null
        set(value: ExecJoin?) {
            val stopAfterVal = stopAfter
            if (stopAfterVal != null) {
                log.info("Adding {} as a finalizing task to {}", value?.name, stopAfterVal.name)
                stopAfterVal.finalizedBy(value)
            }
            field = value
        }

    @TaskAction
    fun exec() {
        joinTask ?: throw GradleException("${javaClass.simpleName} task $name did not have a joinTask associated. Make sure you have \"apply plugin: 'gradle-javaexecfork-plugin'\" somewhere in your gradle file")

        val processBuilder:ProcessBuilder = ProcessBuilder(getProcessArgs())
        redirectStreams(processBuilder)
        processBuilder.directory(File(workingDir))

        log.info("running process: {}", processBuilder.command().joinToString(separator = " "))

        this.process = processBuilder.start()

        val waitForPortVal:Int? = waitForPort
        if (waitForPortVal != null)
            waitForPortOpen(waitForPortVal, 60, TimeUnit.SECONDS, process!!)
    }

    abstract fun getProcessArgs(): List<String>?

    private fun redirectStreams(processBuilder:ProcessBuilder) {
        if (standardOutput == null && errorOutput == null) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT).redirectError(ProcessBuilder.Redirect.INHERIT)
        } else if (standardOutput != null && errorOutput == null) {
            processBuilder.redirectErrorStream(true)
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(File(standardOutput)))
        } else if (standardOutput != null && errorOutput != null) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(File(standardOutput)))
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(File(errorOutput)))
        } else {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(File(errorOutput)))
        }
    }

    fun stop() {
        if (process != null && process!!.isAlive)
            process!!.destroyForcibly().waitFor(15, TimeUnit.SECONDS)
    }
}