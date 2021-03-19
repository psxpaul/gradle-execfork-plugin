package com.github.psxpaul.task

import org.gradle.api.tasks.InputFile
import org.gradle.process.ProcessForkOptions
import org.gradle.process.internal.JavaForkOptionsFactory
import javax.inject.Inject

/**
 * A task that will run a command in a separate process, optionally
 * writing stdout and stderr to disk, and waiting for a specified
 * port to be open.
 *
 * @see AbstractExecFork
 * @see ProcessForkOptions for all available configuration options
 */
open class ExecFork @Inject constructor(forkOptionsFactory: JavaForkOptionsFactory) : AbstractExecFork(),
        ProcessForkOptions by forkOptionsFactory.newJavaForkOptions() {

    /**
     * The path to the executable to run
     * @deprecated Use #executable instead
     */
    @get:InputFile
    var commandLine: String?
        get() = executable
        set(value) {
            executable = value
        }

    override fun getProcessArgs(): List<String>? {
        val processArgs: MutableList<String> = mutableListOf()
        processArgs.add(executable!!)
        processArgs.addAll(args.map(CharSequence::toString))
        return processArgs
    }

}