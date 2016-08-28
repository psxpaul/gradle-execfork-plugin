package com.pr.gradle.task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.JavaExecSpec;
import org.gradle.process.internal.ExecActionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pr.gradle.daemon.Server;

public class JavaExecFork extends DefaultTask {
  protected static final Logger log = LoggerFactory.getLogger(JavaExecFork.class);
  
  public FileCollection classpath;
  public String main;
  public List<String> jvmArgs = new ArrayList<>();
  public List<String> args = new ArrayList<>();
  public Map<String, ?> systemProperties = new HashMap<>();
  public Map<String, ?> environment = new HashMap<>();
  public OutputStream standardOutput = new ByteArrayOutputStream();
  public OutputStream errorOutput = new ByteArrayOutputStream();
  public JavaExecJoin joinTask;
  public Integer controlPort = Server.findOpenPort();

  @Inject
  protected ExecActionFactory getExecActionFactory() {
      throw new UnsupportedOperationException();
  }

  @TaskAction
  public void exec() {
    if (main == null) {
      throw new GradleException(JavaExecFork.class.getSimpleName() + " task '" + getName() + "' must specify a main class");
    }

    if (classpath == null) {
      throw new GradleException(JavaExecFork.class.getSimpleName() + " task '" + getName() + "' must specify a classpath");
    }

    getProject().javaexec(new Action<JavaExecSpec>() {
      @Override
      public void execute(JavaExecSpec spec) {
        spec.setMain(Server.class.getName());
        spec.setClasspath(classpath);
        
        log.info("buildscript dependencies: {}", getProject().getBuildscript().getDependencies());

        if (args == null)
          args = new ArrayList<>();

        args.add(0, main);
        args.add(1, Integer.toString(controlPort));
        spec.setArgs(args);

        spec.setJvmArgs(jvmArgs);
        spec.setSystemProperties(systemProperties);
        spec.setEnvironment(environment);
        spec.setStandardOutput(standardOutput);
        spec.setErrorOutput(errorOutput);
      }
    });
    log.info("done executing {}!", main);
  }
  
  public void setFinalizes(Task finalizes) {
    log.info("***** adding {}_join as a finalizing task to {}", getName(), finalizes.getName());
    finalizes.finalizedBy(joinTask);
  }
  
  public void setStandardOutput(File file) throws FileNotFoundException {
    this.standardOutput = new FileOutputStream(file);
  }

  public void setErrorOutput(File file) throws FileNotFoundException {
    this.errorOutput = new FileOutputStream(file);
  }
}