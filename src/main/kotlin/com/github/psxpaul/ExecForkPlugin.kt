package com.github.psxpaul

import com.github.psxpaul.task.AbstractExecFork
import com.github.psxpaul.task.ExecJoin
import com.github.psxpaul.task.createNameFor
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.util.GradleVersion
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Gradle plugin that will allow for 'com.github.psxpaul.ExecFork' and 'com.github.psxpaul.JavaExecFork' task
 * types. This plugin will make sure all of those tasks are stopped when a build completes, and optionally
 * create stop tasks for each one that specifies a 'stopAfter' task.
 *
 * Note: it is important to apply this plugin to your project for your ExecFork and JavaExecFork tasks to
 * work. E.g.:
 *          apply plugin: 'gradle-execfork-plugin'
 */
class ExecForkPlugin : Plugin<Project> {
    val log: Logger = LoggerFactory.getLogger(ExecForkPlugin::class.java)

    override fun apply(project: Project) {
        if (GradleVersion.current() < GradleVersion.version("5.3")) {
            throw GradleException("This version of the plugin is incompatible with gradle < 5.3! Please use execfork version 0.1.9, or upgrade gradle.")
        }

        val forkTasks: ArrayList<AbstractExecFork> = ArrayList()
        project.tasks.whenTaskAdded { task: Task ->
            if (task is AbstractExecFork) {
                val forkTask: AbstractExecFork = task
                val joinTask: ExecJoin = project.tasks.create(createNameFor(forkTask), ExecJoin::class.java)
                joinTask.forkTask = forkTask
                forkTask.joinTask = joinTask

                forkTasks.add(forkTask)
            }
        }

        project.gradle.addBuildListener(object: BuildAdapter() {
            override fun buildFinished(result: BuildResult) {
                for (forkTask: AbstractExecFork in forkTasks) {
                    try {
                        forkTask.stop()
                    } catch (e: InterruptedException) {
                        log.error("Error stopping daemon for {} task '{}'", forkTask.javaClass.simpleName, forkTask.name, e)
                    }
                }
            }
        })
    }
}
