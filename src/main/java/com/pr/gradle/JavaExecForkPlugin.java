package com.pr.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaExecForkPlugin implements Plugin<Project> {
  protected static final Logger log = LoggerFactory.getLogger(JavaExecForkPlugin.class);

  @Override
  public void apply(Project project) {
    log.info("***** applying to project *****");

    project.getTasks().whenTaskAdded(task -> {
      log.info("**** checking {} of type {}", task.getName(), task.getClass());
      
      if (task instanceof JavaExecFork) {
        JavaExecFork forkTask = (JavaExecFork) task;
        JavaExecJoin joinTask = project.getTasks().create(forkTask.getName() + "_join", JavaExecJoin.class);
        forkTask.joinTask = joinTask;
      }
    });
  }
}