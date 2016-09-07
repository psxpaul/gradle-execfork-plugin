package com.pr.gradle.task;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import com.pr.gradle.task.JavaExecFork;
import com.pr.gradle.task.ExecJoin;

public class JavaExecJoinTest {

  @Test
  public void testCreateNameFor() {
    assertName("startJohnnie", "stopJohnnie");
    assertName("johnnie_start", "johnnie_stop");
    assertName("johnnieStart", "johnnieStop");
    assertName("johnnie_Start", "johnnie_Stop");
    assertName("johnnieSTART", "johnnieSTOP");
    assertName("STARTjohnnie", "STOPjohnnie");

    assertName("runJohnnie", "stopJohnnie");
    assertName("johnnie_run", "johnnie_stop");
    assertName("johnnieRun", "johnnieStop");
    assertName("johnnie_Run", "johnnie_Stop");
    assertName("johnnieRUN", "johnnieSTOP");
    assertName("RUNjohnnie", "STOPjohnnie");

    assertName("execJohnnie", "stopJohnnie");
    assertName("johnnie_exec", "johnnie_stop");
    assertName("johnnieExec", "johnnieStop");
    assertName("johnnie_Exec", "johnnie_Stop");
    assertName("johnnieEXEC", "johnnieSTOP");
    assertName("EXECjohnnie", "STOPjohnnie");
    
    assertName("joseph", "joseph_stop");
  }
  
  private void assertName(String given, String expected) {
    Project project = ProjectBuilder.builder().build();
    JavaExecFork startTask = project.getTasks().create(given, JavaExecFork.class);
    assertThat(ExecJoin.createNameFor(startTask), equalTo(expected));
  }
}