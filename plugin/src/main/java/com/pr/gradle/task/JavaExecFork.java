package com.pr.gradle.task;

import java.io.File;
import java.io.IOException;
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
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.jvm.Jvm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pr.gradle.util.PortUtils;

public class JavaExecFork extends DefaultTask {
  protected static final Logger log = LoggerFactory.getLogger(JavaExecFork.class);
  
  public FileCollection classpath;
  public String main;
  public List<String> jvmArgs = new ArrayList<>();
  public List<String> args = new ArrayList<>();
  public Map<String, ?> systemProperties = new HashMap<>();
  public Map<String, ?> environment = new HashMap<>();
  public String standardOutput = null;
  public String errorOutput = null;
  public Integer controlPort = PortUtils.findOpenPort();
  public Integer waitForPort = null;

  private Task stopAfter = null;
  private Process process = null;
  private JavaExecJoin joinTask = null;

  @TaskAction
  public void exec() throws IOException, InterruptedException {
    if (main == null) {
      throw new GradleException(JavaExecFork.class.getSimpleName() + " task '" + getName() + "' must specify a main class");
    }

    if (classpath == null) {
      throw new GradleException(JavaExecFork.class.getSimpleName() + " task '" + getName() + "' must specify a classpath");
    }

    if (joinTask == null) {
      throw new GradleException(JavaExecFork.class.getSimpleName() + " task '" + getName() + "' did not have a joinTask associated. Make sure you have \"apply plugin: 'gradle-javaexecfork-plugin'\" somewhere in your gradle file");
    }

    log.info("Starting main method {}", main);
    log.info("using args {}", args);
    log.info("using jvmArgs {}", jvmArgs);
    log.info("using systemProperties {}", systemProperties);
    log.info("using environment {}", environment);
    
    List<String> processArgs = new ArrayList<>();
    processArgs.add(Jvm.current().getJavaExecutable().getAbsoluteFile().getAbsolutePath());
    processArgs.add("-cp");
    processArgs.add(classpath.getAsPath());
    processArgs.addAll(jvmArgs.stream().map(a -> { return a.startsWith("-D") ? a : "-D" + a; }).collect(Collectors.toList()));
    processArgs.add(main);
    processArgs.addAll(args);

    ProcessBuilder processBuilder = new ProcessBuilder(processArgs);
    redirectStreams(processBuilder);
    log.info("running process: {}", processBuilder.command().stream().collect(Collectors.joining(" ")));
    this.process = processBuilder.start();
    
    if (waitForPort != null)
      PortUtils.waitForPortOpen(waitForPort, 60, TimeUnit.SECONDS, process);
  }
  
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
  
  public void setJoinTask(JavaExecJoin joinTask) {
    if (stopAfter != null) {
      log.info("Adding '{}' as a finalizing task to '{}'", joinTask.getName(), stopAfter.getName());
      stopAfter.finalizedBy(joinTask);
    }
    this.joinTask = joinTask;
  }
}