package com.github.psxpaul

import com.github.psxpaul.task.AbstractExecFork
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.util.GradleVersion
import javax.inject.Inject

/**
 * Gradle plugin that will allow for 'com.github.psxpaul.ExecFork' and 'com.github.psxpaul.JavaExecFork' task
 * types. This plugin will make sure all of those tasks are stopped when a build completes, and optionally
 * create stop tasks for each one that specifies a 'stopAfter' task.
 *
 * Note: it is important to apply this plugin to your project for your ExecFork and JavaExecFork tasks to
 * work. E.g.:
 *          apply plugin: 'gradle-execfork-plugin'
 */
class ExecForkPlugin @Inject constructor(private val buildEventsListenerRegistry: BuildEventsListenerRegistry) : Plugin<Project> {

    override fun apply(project: Project) {
        if (GradleVersion.current() < GradleVersion.version("6.7")) {
            throw GradleException("This version of the plugin is incompatible with Gradle < 6.7! Please use execfork version 0.1.15, or upgrade Gradle.")
        }

        // We have to create a separate service per project due to https://github.com/gradle/gradle/issues/17559
        val forkTaskTerminationServiceProvider: Provider<ForkTaskTerminationService> = project.gradle.sharedServices.registerIfAbsent("fork-task-termination-"+project.path, ForkTaskTerminationService::class.java) {}

        project.tasks.withType(AbstractExecFork::class.java) {
            @Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            it.forkTaskTerminationService.set(forkTaskTerminationServiceProvider as Provider<out Object>)
        }

        buildEventsListenerRegistry.onTaskCompletion(forkTaskTerminationServiceProvider)
    }
}
