package com.pr.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pr.gradle.task.JavaExecFork;
import com.pr.gradle.task.JavaExecJoin;

public class JavaExecForkPlugin implements Plugin<Project> {
  protected static final Logger log = LoggerFactory.getLogger(JavaExecForkPlugin.class);

  @Override
  public void apply(Project project) {
    project.getTasks().whenTaskAdded(task -> {
      if (task instanceof JavaExecFork) {
        JavaExecFork forkTask = (JavaExecFork) task;
        JavaExecJoin joinTask = project.getTasks().create(JavaExecJoin.createNameFor(forkTask), JavaExecJoin.class);
        joinTask.setControlPort(forkTask.controlPort);
        forkTask.joinTask = joinTask;
      }
    });
  }
}