package com.pr.gradle

import com.pr.gradle.task.AbstractExecFork
import com.pr.gradle.task.ExecJoin
import com.pr.gradle.task.JavaExecFork
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.sameInstance
import org.junit.Assert.assertThat
import org.junit.Test

class ExecForkPluginTest {
    @Test
    fun shouldCreateStopTasks() {
        val project:Project = ProjectBuilder.builder().build()
        project.pluginManager.apply("gradle-execfork-plugin")

        val opts = hashMapOf("type" to JavaExecFork::class.java)
        project.task(opts, "startTestTask")

        val startTask = project.tasks.getByName("startTestTask")
        assertThat(startTask, instanceOf(JavaExecFork::class.java))
        val forkTask = startTask as AbstractExecFork

        val stopTask = project.tasks.getByName("stopTestTask")
        assertThat(stopTask, instanceOf(ExecJoin::class.java))

        val joinTask = stopTask as ExecJoin
        assertThat(joinTask.forkTask, sameInstance(forkTask))
        assertThat(forkTask.joinTask, sameInstance(joinTask))
    }
}
