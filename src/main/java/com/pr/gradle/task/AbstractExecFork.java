package com.pr.gradle.task;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pr.gradle.util.PortUtils;

public abstract class AbstractExecFork extends DefaultTask {
  protected final Logger log = LoggerFactory.getLogger(getClass());

  public String workingDir = getProject().getProjectDir().getAbsolutePath();
  public List<String> args = new ArrayList<>();
  public Map<String, ?> environment = new HashMap<>();
  public String standardOutput = null;
  public String errorOutput = null;

  public Integer waitForPort = null;

  private Task stopAfter = null;
  private Process process = null;
  private ExecJoin joinTask = null;
  
  @TaskAction
  public void exec() throws Exception {
    if (joinTask == null) {
      throw new GradleException(JavaExecFork.class.getSimpleName() + " task '" + getName() + "' did not have a joinTask associated. Make sure you have \"apply plugin: 'gradle-javaexecfork-plugin'\" somewhere in your gradle file");
    }
    
    doValidation();
    
    ProcessBuilder processBuilder = new ProcessBuilder(getProcessArgs());
    redirectStreams(processBuilder);
    processBuilder.directory(new File(workingDir));

    log.info("running process: {}", processBuilder.command().stream().collect(Collectors.joining(" ")));

    this.process = processBuilder.start();
    
    if (waitForPort != null)
      PortUtils.waitForPortOpen(waitForPort, 60, TimeUnit.SECONDS, process);

  }
  
  public abstract void doValidation();
  public abstract List<String> getProcessArgs();
  
  private void redirectStreams(ProcessBuilder processBuilder) {
    if (standardOutput == null && errorOutput == null) {
      processBuilder.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
    } else if (standardOutput != null && errorOutput == null) {
      processBuilder.redirectErrorStream(true);
      processBuilder.redirectOutput(Redirect.appendTo(new File(standardOutput)));
    } else if (standardOutput != null && errorOutput != null) {
      processBuilder.redirectOutput(Redirect.appendTo(new File(standardOutput)));
      processBuilder.redirectError(Redirect.appendTo(new File(errorOutput)));
    } else {
      processBuilder.redirectOutput(Redirect.INHERIT);
      processBuilder.redirectError(Redirect.appendTo(new File(errorOutput)));
    }
  }

  public void stop() throws InterruptedException {
    if (process != null && process.isAlive())
      process.destroyForcibly().waitFor(15, TimeUnit.SECONDS);
  }
  
  public void setStopAfter(Task stopAfter) {
    if (joinTask != null) {
      log.info("Adding '{}' as a finalizing task to '{}'", joinTask.getName(), stopAfter.getName());
      stopAfter.finalizedBy(joinTask);
    }
    this.stopAfter = stopAfter;
  }
  
  public void setJoinTask(ExecJoin joinTask) {
    if (stopAfter != null) {
      log.info("Adding '{}' as a finalizing task to '{}'", joinTask.getName(), stopAfter.getName());
      stopAfter.finalizedBy(joinTask);
    }
    this.joinTask = joinTask;
  }
  
  public ExecJoin getJoinTask() {
    return joinTask;
  }
}