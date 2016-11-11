package com.github.psxpaul.task

import org.gradle.api.file.FileCollection
import org.gradle.internal.jvm.Jvm

/**
 * A task that will run a java class in a separate process, optionally
 * writing stdout and stderr to disk, and waiting for a specified
 * port to be open.
 *
 * @see AbstractExecFork
 *
 * @param classpath the classpath to call java with
 * @param main the fully qualified name of the class to execute (e.g. 'com.foo.bar.MainExecutable')
 * @param jvmArgs a list of arguments to provide the jvm (not to be confused with the args to give
 *              to the main class). E.g. ['-Xmx100m', '-Xmx500m', '-Dspring.profiles.active=dev', "-Djava.io.tmpdir=$buildDir/tmp"]
 */
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
