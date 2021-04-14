package com.github.psxpaul.task

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.internal.jvm.Jvm
import org.gradle.process.JavaForkOptions
import org.gradle.process.internal.JavaForkOptionsFactory
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import javax.inject.Inject

/**
 * A task that will run a java class in a separate process, optionally
 * writing stdout and stderr to disk, and waiting for a specified
 * port to be open.
 *
 * @see AbstractExecFork
 *
 * @param classpath the classpath to call java with
 * @param main the fully qualified name of the class to execute (e.g. 'com.foo.bar.MainExecutable')
 */
open class JavaExecFork @Inject constructor(forkOptionsFactory: JavaForkOptionsFactory) : AbstractExecFork(),
        JavaForkOptions by forkOptionsFactory.newJavaForkOptions() {

    @InputFiles
    var classpath: FileCollection? = null

    @Input
    var main: String? = null

    override fun getProcessArgs(): List<String>? {
        val processArgs: MutableList<String> = mutableListOf()
        processArgs.add(Jvm.current().javaExecutable.absolutePath)
        processArgs.add("-cp")
        processArgs.add((bootstrapClasspath + classpath!!).asPath)
        processArgs.addAll(allJvmArgs)
        processArgs.add(main!!)
        processArgs.addAll(args.map(CharSequence::toString))

        if (hasCommandLineExceedMaxLength(processArgs)) {
            processArgs[processArgs.indexOf("-cp") + 1] =
                    writePathingJarFile(bootstrapClasspath + classpath).path
        }

        return processArgs
    }

    private fun writePathingJarFile(classPath: FileCollection): File {
        val pathingJarFile = File.createTempFile("gradle-javaexec-classpath", ".jar")
        FileOutputStream(pathingJarFile).use { fileOutputStream ->
            JarOutputStream(fileOutputStream, toManifest(classPath)).use { jarOutputStream ->
                jarOutputStream.putNextEntry(ZipEntry("META-INF/"))
            }
        }
        return pathingJarFile
    }

    private fun toManifest(classPath: FileCollection): Manifest {
        val manifest = Manifest()
        val attributes = manifest.mainAttributes
        attributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
        attributes.putValue("Class-Path",
                classPath.files.stream().map(File::toURI).map(URI::toString).collect(Collectors.joining(" ")))
        return manifest
    }

    private fun hasCommandLineExceedMaxLength(args: List<String>): Boolean {
        // See http://msdn.microsoft.com/en-us/library/windows/desktop/ms682425(v=vs.85).aspx
        // Derived from MAX_ARG_STRLEN as per http://man7.org/linux/man-pages/man2/execve.2.html
        val maxCommandLineLength = if (System.getProperty("os.name").contains("Windows")) 32767 else 131072
        return args.joinToString(" ").length > maxCommandLineLength
    }
}
