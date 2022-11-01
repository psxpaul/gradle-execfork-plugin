package com.github.psxpaul

import com.github.psxpaul.task.AbstractExecFork
import com.github.psxpaul.task.ExecJoin
import com.github.psxpaul.task.JavaExecFork
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.sameInstance
import org.junit.Assert.assertThat
import org.junit.Test

class ExecForkPluginTest {
    @Test
    fun shouldCreateStopTasks() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply("gradle-execfork-plugin")

        val someTask = project.tasks.register("someTask")

        val opts = hashMapOf("type" to JavaExecFork::class.java)
        val startTestTask = project.task(opts, "startTestTask") as JavaExecFork
        startTestTask.stopAfter = someTask

        val startTask = project.tasks.getByName("startTestTask")
        assertThat(startTask, instanceOf(JavaExecFork::class.java))
        val forkTask = startTask as AbstractExecFork

        val stopTask = project.tasks.getByName("stopTestTask")
        assertThat(stopTask, instanceOf(ExecJoin::class.java))

        val joinTask = stopTask as ExecJoin
        assertThat(joinTask.forkTask, sameInstance(forkTask))
    }
}
