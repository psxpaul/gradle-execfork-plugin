package com.github.psxpaul.task

import com.github.psxpaul.ForkTaskTerminationService
import com.github.psxpaul.stream.InputStreamPipe
import com.github.psxpaul.stream.OutputStreamLogger
import com.github.psxpaul.util.waitForPortOpen
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ProcessForkOptions
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*
import java.util.stream.Stream

/**
 * An abstract task that will launch an executable as a background process, optionally
 * waiting until a specific port is opened. The task will also stop the process if given
 * a stopAfter or joinTask
 *
 * @see ExecFork
 * @see JavaExecFork
 * @see ProcessForkOptions
 *
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
abstract class AbstractExecFork(objectFactory: ObjectFactory) : DefaultTask(), ProcessForkOptions {

    @Input
    abstract override fun getEnvironment(): MutableMap<String, Any>

    @Input
    abstract override fun getExecutable(): String?

    @Input
    var args: MutableList<CharSequence> = mutableListOf()

    @InputDirectory
    abstract override fun getWorkingDir(): File

    @OutputFile
    @Optional
    val standardOutput: RegularFileProperty = objectFactory.fileProperty()

    @OutputFile
    @Optional
    val errorOutput: RegularFileProperty = objectFactory.fileProperty()

    @Input
    @Optional
    var waitForPort: Int? = null

    @Input
    @Optional
    var waitForOutput: String? = null

    @Input
    @Optional
    var waitForError: String? = null

    @Input
    var forceKill: Boolean = false

    @Input
    var killDescendants: Boolean = true

    @Internal
    var process: Process? = null

    @Input
    var timeout: Long = 60

    @Internal
    var stopAfter: TaskProvider<Task>? = null
        set(stopAfterValue: TaskProvider<Task>?) {
            if (stopAfterValue == null) {
                return
            }
            if (field != null) {
                throw GradleException("Cannot reassign stopAfter! Was set to ${field!!.name} already when attempting to set to ${stopAfterValue.name}")
            }
            val joinTask = project.tasks.register(createNameFor(this), ExecJoin::class.java) {
                it.forkTask = this
            }
            logger.info("Adding {} as a finalizing task to {}", joinTask.name, stopAfterValue.name)
            stopAfterValue.configure {
                it.finalizedBy(joinTask)
            }
            field = stopAfterValue
        }

    // It should be Property<ForkTaskTerminationService> but because of a bug in Gradle
    // we have to use a more generic type, see https://github.com/gradle/gradle/issues/17559
    // This can be reproduced in the spring_boot sample project
    @Internal
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    val forkTaskTerminationService: Property<in Object> = objectFactory.property(Object::class.java)

    init {
        // The exec fork task should be executed in any case if not manually specified otherwise.
        // By default, this is the case as the task has only inputs defined, but e.g. jacoco attaches a jvm argument
        // provider, which in turn contributes an output property, which causes the task to be considered up-to-date.
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun exec() {
        val processBuilder: ProcessBuilder = ProcessBuilder(getProcessArgs())
        redirectStreams(processBuilder)

        val processWorkingDir: File = workingDir
        processWorkingDir.mkdirs()
        processBuilder.directory(processWorkingDir)

        environment.forEach { processBuilder.environment()[it.key] = it.value.toString() }

        logger.info("running process: {}", processBuilder.command().joinToString(separator = " "))

        this.process = processBuilder.start()
        installPipesAndWait(this.process!!)

        val waitForPortVal: Int? = waitForPort
        if (waitForPortVal != null)
            waitForPortOpen(waitForPortVal, timeout, TimeUnit.SECONDS, process!!)

        val task: AbstractExecFork = this
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                task.stop()
            }
        })
        (forkTaskTerminationService.get() as ForkTaskTerminationService).addAbstractExecForkTask(this)
    }

    @Input
    abstract fun getProcessArgs(): List<String>?

    private fun installPipesAndWait(process: Process) {
        val processOut: OutputStream = if (standardOutput.isPresent) {
            standardOutput.asFile.get().parentFile.mkdirs()
            FileOutputStream(standardOutput.asFile.get())
        } else OutputStreamLogger(logger)
        val outPipe: InputStreamPipe = InputStreamPipe(process.inputStream, processOut, waitForOutput)
        if (errorOutput.isPresent) {
            errorOutput.asFile.get().parentFile.mkdirs()

            val errPipe: InputStreamPipe = InputStreamPipe(process.errorStream, FileOutputStream(errorOutput.asFile.get()), waitForError)
            errPipe.waitForPattern(timeout, TimeUnit.SECONDS)
        }
        outPipe.waitForPattern(timeout, TimeUnit.SECONDS)
    }

    private fun redirectStreams(processBuilder: ProcessBuilder) {
        if (!errorOutput.isPresent) {
            processBuilder.redirectErrorStream(true)
        }
    }

    /**
     * Stop the process that this task has spawned
     */
    fun stop() {
        try {
            if (killDescendants) {
                stopDescendants()
            }
        } catch (e: Exception) {
            logger.warn("Failed to stop descendants", e)
        }

        stopRootProcess()
    }

    private fun stopRootProcess() {
        val process: Process = process ?: return
        if (process.isAlive && !forceKill) {
            process.destroy()
            process.waitFor(15, TimeUnit.SECONDS)
        }
        if (process.isAlive) {
            process.destroyForcibly().waitFor(15, TimeUnit.SECONDS)
        }
    }

    private fun stopDescendants() {
        val process: Process = process ?: return
        if (!process.isAlive) {
            return
        }

        val toHandle = process::class.memberFunctions.singleOrNull { it.name == "toHandle" }
        if (toHandle == null) {
            logger.error("Could not load Process.toHandle(). The killDescendants flag requires Java 9+. Please set killDescendants=false, or upgrade to Java 9+.")
            return // not supported, pre java 9?
        }

        toHandle.isAccessible = true
        val handle = toHandle.call(process)
        if (handle == null) {
            logger.warn("Could not get process handle. Process descendants may not be stopped.")
            return
        }
        val descendants = handle::class.memberFunctions.single { it.name == "descendants" }
        descendants.isAccessible = true
        val children: Stream<*> = descendants.call(handle) as Stream<*>;
        val destroy = handle::class.memberFunctions.single { it.name == if (forceKill) "destroyForcibly" else "destroy" }
        destroy.isAccessible = true

        children.forEach {
            destroy.call(it)
        }
    }

    @Deprecated("Use a task provider instead of a concrete Task instance", ReplaceWith("stopAfter = tasks.named(':myTask')"))
    fun setStopAfter(task: Task) {
        stopAfter = task.project.tasks.named(task.name)
    }
}
