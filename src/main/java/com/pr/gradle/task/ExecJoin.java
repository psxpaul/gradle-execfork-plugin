package com.pr.gradle.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecJoin extends DefaultTask {
  protected static final Logger log = LoggerFactory.getLogger(ExecJoin.class);
  private AbstractExecFork forkTask;

  @TaskAction
  public void exec() throws InterruptedException {
    log.info("Stopping '{}' task '{}'", AbstractExecFork.class.getSimpleName(), forkTask.getName());
    forkTask.stop();
  }
  
  public void setForkTask(AbstractExecFork fork) {
    this.forkTask = fork;
  }
  
  public AbstractExecFork getForkTask() {
    return forkTask;
  }
  
  public static String createNameFor(AbstractExecFork startTask) {
    String taskName = startTask.getName();
    
    if (hasWord(taskName, "start")) return replaceWord(taskName, "start", "stop");
    if (hasWord(taskName, "Start")) return replaceWord(taskName, "Start", "Stop");
    if (hasWord(taskName, "START")) return replaceWord(taskName, "START", "STOP");

    if (hasWord(taskName, "run")) return replaceWord(taskName, "run", "stop");
    if (hasWord(taskName, "Run")) return replaceWord(taskName, "Run", "Stop");
    if (hasWord(taskName, "RUN")) return replaceWord(taskName, "RUN", "STOP");

    if (hasWord(taskName, "exec")) return replaceWord(taskName, "exec", "stop");
    if (hasWord(taskName, "Exec")) return replaceWord(taskName, "Exec", "Stop");
    if (hasWord(taskName, "EXEC")) return replaceWord(taskName, "EXEC", "STOP");

    return taskName + "_stop";
  }
  
  private static boolean hasWord(String input, String pattern) {
    return input.startsWith(pattern) || input.endsWith(pattern);
  }
  
  private static String replaceWord(String input, String pattern, String replacement) {
    return input.replaceFirst("^" + pattern, replacement).replaceFirst(pattern + "$", replacement);
  }
}