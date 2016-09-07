package com.pr.gradle;

import java.util.ArrayList;
import java.util.List;

import org.gradle.BuildAdapter;
import org.gradle.BuildResult;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pr.gradle.task.AbstractExecFork;
import com.pr.gradle.task.ExecJoin;
import com.pr.gradle.task.JavaExecFork;

public class ExecForkPlugin implements Plugin<Project> {
  protected static final Logger log = LoggerFactory.getLogger(ExecForkPlugin.class);

  @Override
  public void apply(Project project) {
    List<AbstractExecFork> forkTasks = new ArrayList<>();

    project.getTasks().whenTaskAdded(task -> {
      if (task instanceof AbstractExecFork) {
        AbstractExecFork forkTask = (AbstractExecFork) task;
        ExecJoin joinTask = project.getTasks().create(ExecJoin.createNameFor(forkTask), ExecJoin.class);
        joinTask.setForkTask(forkTask);
        forkTask.setJoinTask(joinTask);
        
        forkTasks.add(forkTask);
      }
    });

    project.getGradle().addBuildListener(new BuildAdapter() {
      @Override
      public void buildFinished(BuildResult result) {
        for (AbstractExecFork forkTask : forkTasks) {
          try {
            forkTask.stop();
          } catch (InterruptedException e) {
            log.error("Error stopping daemon for {} task '{}'", JavaExecFork.class.getSimpleName(), forkTask.getName(), e);
          }
        }
      }
    });
  }
}