package com.github.psxpaul.task

import com.github.psxpaul.util.waitForPortOpen
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * An abstract task that will launch an executable as a background process, optionally
 * waiting until a specific port is opened. The task will also stop the process if given
 * a stopAfter or joinTask
 *
 * @see ExecFork
 * @see JavaExecFork
 *
 * @param workingDir the working directory that the process is run from
 * @param args the arguments to give the executable
 * @param standardOutput the name of the file to write the process's standard output to
 * @param errorOutput the name of the file to write the process's error output to
 * @param waitForPort if specified, block the task from completing until the given port is
 *                 open locally
 * @param timeout the length of time in seconds that the task will wait for the port to be
 *                 be opened, before failing
 * @param stopAfter if specified, this task will stop the running process after the stopAfter
 *                 task has been completed
 */
abstract class AbstractExecFork : DefaultTask() {
    val log: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    var workingDir: CharSequence = project.projectDir.absolutePath
    var args: MutableList<CharSequence> = mutableListOf()
    var standardOutput: CharSequence? = null
    var errorOutput: CharSequence? = null

    var waitForPort: Int? = null
    var process: Process? = null
    var timeout: Long = 60

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

        val processBuilder: ProcessBuilder = ProcessBuilder(getProcessArgs())
        redirectStreams(processBuilder)

        val processWorkingDir: File = File(workingDir.toString())
        processWorkingDir.mkdirs()
        processBuilder.directory(processWorkingDir)

        log.info("running process: {}", processBuilder.command().joinToString(separator = " "))

        this.process = processBuilder.start()

        val waitForPortVal:Int? = waitForPort
        if (waitForPortVal != null)
            waitForPortOpen(waitForPortVal, timeout, TimeUnit.SECONDS, process!!)
    }

    abstract fun getProcessArgs(): List<String>?

    private fun redirectStreams(processBuilder:ProcessBuilder) {
        if (standardOutput == null && errorOutput == null) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT).redirectError(ProcessBuilder.Redirect.INHERIT)
        } else if (standardOutput != null && errorOutput == null) {
            val outputFile: File = File(standardOutput.toString())
            outputFile.parentFile.mkdirs()
            processBuilder.redirectErrorStream(true)
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(outputFile))
        } else if (standardOutput != null && errorOutput != null) {
            val outputFile: File = File(standardOutput.toString())
            outputFile.parentFile.mkdirs()
            val errorFile: File = File(errorOutput.toString())
            errorFile.parentFile.mkdirs()

            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(outputFile))
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(errorFile))
        } else {
            val errorFile: File = File(errorOutput.toString())
            errorFile.parentFile.mkdirs()

            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(errorFile))
        }
    }

    /**
     * Stop the process that this task has spawned
     */
    fun stop() {
        if (process != null && process!!.isAlive)
            process!!.destroyForcibly().waitFor(15, TimeUnit.SECONDS)
    }
}
