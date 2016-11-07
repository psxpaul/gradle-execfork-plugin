package com.github.psxpaul.task

import org.gradle.api.file.FileCollection
import org.gradle.internal.jvm.Jvm

open class JavaExecFork : AbstractExecFork() {
    var classpath: FileCollection? = null
    var main:CharSequence? = null
    var jvmArgs:List<CharSequence> = listOf()

    override fun getProcessArgs(): List<String>? {
        val processArgs:MutableList<String> = mutableListOf()
        processArgs.add(Jvm.current().javaExecutable.absoluteFile.absolutePath)
        processArgs.add("-cp")
        processArgs.add(classpath!!.asPath)
        processArgs.addAll(jvmArgs.map(CharSequence::toString))
        processArgs.add(main!!.toString())
        processArgs.addAll(args.map(CharSequence::toString))
        return processArgs
    }
}
