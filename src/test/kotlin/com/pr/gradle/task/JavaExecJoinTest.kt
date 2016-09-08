package com.pr.gradle.task

import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class JavaExecJoinTest {
    @Test
    fun testCreateNameFor() {
        assertName("startJohnnie", "stopJohnnie")
        assertName("johnnie_start", "johnnie_stop")
        assertName("johnnieStart", "johnnieStop")
        assertName("johnnie_Start", "johnnie_Stop")
        assertName("johnnieSTART", "johnnieSTOP")
        assertName("STARTjohnnie", "STOPjohnnie")

        assertName("runJohnnie", "stopJohnnie")
        assertName("johnnie_run", "johnnie_stop")
        assertName("johnnieRun", "johnnieStop")
        assertName("johnnie_Run", "johnnie_Stop")
        assertName("johnnieRUN", "johnnieSTOP")
        assertName("RUNjohnnie", "STOPjohnnie")

        assertName("execJohnnie", "stopJohnnie")
        assertName("johnnie_exec", "johnnie_stop")
        assertName("johnnieExec", "johnnieStop")
        assertName("johnnie_Exec", "johnnie_Stop")
        assertName("johnnieEXEC", "johnnieSTOP")
        assertName("EXECjohnnie", "STOPjohnnie")

        assertName("joseph", "joseph_stop")
    }

    fun assertName(given:String, expected:String) {
        val project = ProjectBuilder.builder().build()
        val startTask = project.tasks.create(given, JavaExecFork::class.java)
        assertThat(ExecJoin.createNameFor(startTask), equalTo(expected))
    }
}