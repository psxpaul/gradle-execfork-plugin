package com.pr.gradle;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Ignore;
import org.junit.Test;

import com.pr.gradle.task.JavaExecFork;

public class JavaExecForkPluginTest {

  @Ignore
  @Test
  public void greeterPluginAddsGreetingTaskToProject() {
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply("gradle-javaexecfork-plugin");
    
    //TODO: add a JavaExecFork task, and make sure it does a start/stop

    Task task = project.getTasks().getByName("JavaExecFork");
    assertThat(task, instanceOf(JavaExecFork.class));
  }
}