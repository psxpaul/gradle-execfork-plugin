package com.pr.gradle;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import com.pr.gradle.task.JavaExecFork;
import com.pr.gradle.task.ExecJoin;

public class JavaExecForkPluginTest {

  @Test
  public void greeterPluginAddsGreetingTaskToProject() {
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply("gradle-execfork-plugin");

    Map<String, Object> opts = new HashMap<>();
    opts.put("type", JavaExecFork.class);
    project.task(opts, "startTestTask");
    
    Task startTask = project.getTasks().getByName("startTestTask");
    assertThat(startTask, instanceOf(JavaExecFork.class));
    JavaExecFork forkTask = (JavaExecFork) startTask;

    Task stopTask = project.getTasks().getByName("stopTestTask");
    assertThat(stopTask, instanceOf(ExecJoin.class));
    ExecJoin joinTask = (ExecJoin) stopTask;

    assertThat(joinTask.getForkTask(), sameInstance(forkTask));
    assertThat(forkTask.getJoinTask(), sameInstance(joinTask));
  }
}