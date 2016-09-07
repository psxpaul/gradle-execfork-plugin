package com.pr.gradle.task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.internal.jvm.Jvm;

public class JavaExecFork extends AbstractExecFork {
  public FileCollection classpath;
  public String main;
  public List<String> jvmArgs = new ArrayList<>();

  public void doValidation() {
    if (main == null) {
      throw new GradleException(JavaExecFork.class.getSimpleName() + " task '" + getName() + "' must specify a main class");
    }

    if (classpath == null) {
      throw new GradleException(JavaExecFork.class.getSimpleName() + " task '" + getName() + "' must specify a classpath");
    }
  }

  @Override
  public List<String> getProcessArgs() {
    List<String> processArgs = new ArrayList<>();
    processArgs.add(Jvm.current().getJavaExecutable().getAbsoluteFile().getAbsolutePath());
    processArgs.add("-cp");
    processArgs.add(classpath.getAsPath());
    processArgs.addAll(jvmArgs.stream().map(a -> { return a.startsWith("-D") ? a : "-D" + a; }).collect(Collectors.toList()));
    processArgs.add(main);
    processArgs.addAll(args);
    return processArgs;
  }
}