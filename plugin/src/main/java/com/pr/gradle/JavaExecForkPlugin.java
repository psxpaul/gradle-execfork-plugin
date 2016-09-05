package com.pr.gradle;

import java.util.ArrayList;
import java.util.List;

import org.gradle.BuildAdapter;
import org.gradle.BuildResult;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pr.gradle.task.JavaExecFork;
import com.pr.gradle.task.JavaExecJoin;

public class JavaExecForkPlugin implements Plugin<Project> {
  protected static final Logger log = LoggerFactory.getLogger(JavaExecForkPlugin.class);

  @Override
  public void apply(Project project) {
    List<JavaExecFork> forkTasks = new ArrayList<>();

    project.getTasks().whenTaskAdded(task -> {
      if (task instanceof JavaExecFork) {
        JavaExecFork forkTask = (JavaExecFork) task;
        JavaExecJoin joinTask = project.getTasks().create(JavaExecJoin.createNameFor(forkTask), JavaExecJoin.class);
        joinTask.setForkTask(forkTask);
        forkTask.setJoinTask(joinTask);
        
        forkTasks.add(forkTask);
      }
    });

    project.getGradle().addBuildListener(new BuildAdapter() {
      @Override
      public void buildFinished(BuildResult result) {
        for (JavaExecFork forkTask : forkTasks) {
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