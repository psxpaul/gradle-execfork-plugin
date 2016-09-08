package com.pr.gradle

import com.pr.gradle.task.AbstractExecFork
import com.pr.gradle.task.ExecJoin
import com.pr.gradle.task.createNameFor
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class ExecForkPlugin : Plugin<Project> {
    val log:Logger = LoggerFactory.getLogger(ExecForkPlugin::class.java)

    override fun apply(project: Project?) {
        val forkTasks:ArrayList<AbstractExecFork> = ArrayList()

        project?.tasks?.whenTaskAdded({ task: Task ->
            if (task is AbstractExecFork) {
                val forkTask:AbstractExecFork = task
                val joinTask:ExecJoin = project.tasks.create(createNameFor(forkTask), ExecJoin::class.java)
                joinTask.forkTask = forkTask
                forkTask.joinTask = joinTask

                forkTasks.add(task)
            }
        });

        project?.gradle?.addBuildListener(object:BuildAdapter() {
            override fun buildFinished(result:BuildResult?) {
                for (forkTask:AbstractExecFork in forkTasks) {
                    try {
                        forkTask.stop();
                    } catch (e:InterruptedException) {
                        log.error("Error stopping daemon for {} task '{}'", forkTask.javaClass.simpleName, forkTask.name, e);
                    }
                }
            }
        });
    }
}