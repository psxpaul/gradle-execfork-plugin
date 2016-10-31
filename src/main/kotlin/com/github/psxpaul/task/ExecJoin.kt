package com.github.psxpaul.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class ExecJoin() : DefaultTask() {
    val log: Logger = LoggerFactory.getLogger(ExecJoin::class.java)

    var forkTask: AbstractExecFork? = null

    @TaskAction
    fun exec() {
        log.info("Stopping {} task {}", forkTask!!.javaClass.simpleName, forkTask!!.name)
        forkTask!!.stop()
    }
}

fun createNameFor(startTask: AbstractExecFork):String {
    val taskName:String = startTask.name

    if (hasWord(taskName, "start")) return replaceWord(taskName, "start", "stop")
    if (hasWord(taskName, "Start")) return replaceWord(taskName, "Start", "Stop")
    if (hasWord(taskName, "START")) return replaceWord(taskName, "START", "STOP")

    if (hasWord(taskName, "run")) return replaceWord(taskName, "run", "stop")
    if (hasWord(taskName, "Run")) return replaceWord(taskName, "Run", "Stop")
    if (hasWord(taskName, "RUN")) return replaceWord(taskName, "RUN", "STOP")

    if (hasWord(taskName, "exec")) return replaceWord(taskName, "exec", "stop")
    if (hasWord(taskName, "Exec")) return replaceWord(taskName, "Exec", "Stop")
    if (hasWord(taskName, "EXEC")) return replaceWord(taskName, "EXEC", "STOP")

    return taskName + "_stop"
}

private fun hasWord(input:String, pattern:String):Boolean {
    return input.startsWith(pattern) || input.endsWith(pattern)
}

private fun replaceWord(input:String, pattern:String, replacement:String):String {
    return input.replace(Regex("^$pattern"), replacement).replace(Regex("$pattern$"), replacement)
}
