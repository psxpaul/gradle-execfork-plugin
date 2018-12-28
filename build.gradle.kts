plugins {
    id("com.gradle.plugin-publish").version("0.9.7")
    id("org.jetbrains.kotlin.jvm").version("1.2.40")
    id("idea")
    id("maven")
}

group = "com.github.psxpaul"
version = File("VERSION").readText().trim()
buildDir = File("build/gradle")

dependencies {
    compile(gradleApi())
    compile("org.jetbrains.kotlin:kotlin-stdlib:1.2.40")

    testCompile("junit:junit:4.12")
    testCompile("org.hamcrest:hamcrest-all:1.3")
}

pluginBundle {
    website = "http://github.com/psxpaul"
    vcsUrl = "https://github.com/psxpaul/gradle-execfork-plugin"
    description = "Execute Java or shell processes in the background during a build"
    tags = listOf("java", "exec", "background", "process")

    (plugins) {
        create("execForkPlugin") {
            id = "com.github.psxpaul.execfork"
            displayName = "Gradle Exec Fork Plugin"
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

tasks {
    val cleanSampleProjects by creating(GradleBuild::class) {
        buildFile = File("${project.rootDir}/sample_projects/build.gradle")
        tasks = listOf("clean")
    }
    cleanSampleProjects.dependsOn("install")
    "clean" { finalizedBy(cleanSampleProjects) }

    val buildSampleProjects by creating(GradleBuild::class) {
        buildFile = File("${project.rootDir}/sample_projects/build.gradle")
        tasks = listOf("build")
    }
    buildSampleProjects.dependsOn("install")
    "build" { finalizedBy(buildSampleProjects) }
}


val javadocJar by tasks.creating(Jar::class) {
    classifier = "javadoc"
    from("javadoc")
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

artifacts {
    add("archives", javadocJar)
    add("archives", sourcesJar)
}

