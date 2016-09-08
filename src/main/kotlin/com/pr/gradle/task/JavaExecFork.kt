package com.pr.gradle.task

import org.gradle.api.file.FileCollection
import org.gradle.internal.jvm.Jvm

open class JavaExecFork : AbstractExecFork() {
    var classpath: FileCollection? = null
    var main:String? = null
    var jvmArgs:List<String> = listOf()

    override fun getProcessArgs(): List<String>? {
        val processArgs:MutableList<String> = mutableListOf()
        processArgs.add(Jvm.current().javaExecutable.absoluteFile.absolutePath)
        processArgs.add("-cp")
        processArgs.add(classpath!!.asPath)
        processArgs.addAll(jvmArgs.map { s -> if (s.startsWith("-D")) return@map s else "-D" + s })
        processArgs.add(main!!)
        processArgs.addAll(args)
        return processArgs
    }
}