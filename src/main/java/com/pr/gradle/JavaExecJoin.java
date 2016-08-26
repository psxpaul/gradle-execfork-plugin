package com.pr.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaExecJoin extends DefaultTask {
  protected static final Logger log = LoggerFactory.getLogger(JavaExecFork.class);

  @TaskAction
  public void exec() {
    System.out.println("Stopping process...");
  }
}