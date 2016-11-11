package com.github.psxpaul.task

/**
 * A task that will run a command in a separate process, optionally
 * writing stdout and stderr to disk, and waiting for a specified
 * port to be open.
 *
 * @see AbstractExecFork
 *
 * @param commandLine the path to the executable to run
 */
open class ExecFork : AbstractExecFork() {
    var commandLine:String? = null

    override fun getProcessArgs(): List<String>? {
        val processArgs:MutableList<String> = mutableListOf()
        processArgs.add(commandLine!!)
        processArgs.addAll(args.map(CharSequence::toString))
        return processArgs
    }
}