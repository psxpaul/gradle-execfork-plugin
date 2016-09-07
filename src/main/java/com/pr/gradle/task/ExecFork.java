package com.pr.gradle.task;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.GradleException;

public class ExecFork extends AbstractExecFork {
  public String commandLine;

  public void doValidation() {
    if (commandLine == null) {
      throw new GradleException(ExecFork.class.getSimpleName() + " task '" + getName() + "' must specify a commandLine");
    }
  }

  @Override
  public List<String> getProcessArgs() {
    List<String> processArgs = new ArrayList<>();
    processArgs.add(commandLine);
    processArgs.addAll(args);
    return processArgs;
  }
}