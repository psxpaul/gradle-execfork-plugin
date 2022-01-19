package com.github.psxpaul

import com.github.psxpaul.task.AbstractExecFork
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Shared service that collects all started AbstractExecFork tasks and stops the daemons when the build finished
 * in case they have not already been stopped.
 */
abstract class ForkTaskTerminationService : BuildService<BuildServiceParameters.None>, AutoCloseable, OperationCompletionListener {

    private val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    /** Holds references to all AbstractExecFork tasks that have been started during this build. */
    private val forkTasks: MutableList<AbstractExecFork> = mutableListOf()

    fun addAbstractExecForkTask(task: AbstractExecFork) {
        forkTasks.add(task)
    }

    override fun onFinish(event: FinishEvent?) {
        // We are not actually interested in task execution events.
        // We just want to be notified in #close() when no more task execution events can arrive, which means build has finished.
        // We still need to implement the method to conform to the OperationCompletionListener interface.
    }

    /**
     * Will be called after the build finished.
     */
    override fun close() {
        for (forkTask: AbstractExecFork in forkTasks) {
            try {
                forkTask.stop()
            } catch (e: InterruptedException) {
                logger.error("Error stopping daemon for {} task '{}'", forkTask.javaClass.simpleName, forkTask.name, e)
            }
        }
    }
}