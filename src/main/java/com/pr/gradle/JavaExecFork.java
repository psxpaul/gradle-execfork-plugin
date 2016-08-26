package com.pr.gradle;

import java.io.OutputStream;
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

public class JavaExecFork extends DefaultTask {
  protected static final Logger log = LoggerFactory.getLogger(JavaExecFork.class);
  
  public List<String> jvmArgs;
  public FileCollection classpath;
  public String main;
  public List<String> args;
  public Map<String, ?> systemProperties;
  public Map<String, ?> environment;
  public OutputStream standardOutput;
  public OutputStream errorOutput;
  public JavaExecJoin joinTask;

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
        spec.setMain(main);
        spec.setClasspath(classpath);

        if (args != null) spec.setArgs(args);
        if (args != null) spec.setJvmArgs(jvmArgs);
        if (args != null) spec.setSystemProperties(systemProperties);
        if (args != null) spec.setEnvironment(environment);
        if (args != null) spec.setStandardOutput(standardOutput);
        if (args != null) spec.setErrorOutput(errorOutput);
      }
    });
    log.info("done executing {}!", main);
  }
  
  public void setFinalizes(Task finalizes) {
    log.info("***** adding {}_join as a finalizing task to {}", getName(), finalizes.getName());
    finalizes.finalizedBy(joinTask);
  }
}