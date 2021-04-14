import org.gradle.api.tasks.testing.logging.TestExceptionFormat

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.plugin-publish").version("0.14.0")
    id("org.jetbrains.kotlin.jvm").version("1.4.32")
    id("idea")
    id("maven-publish")
    id("java-gradle-plugin")
}

group = "com.github.psxpaul"
version = File(rootDir, "VERSION").readText().trim()

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.32")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.32")

    testImplementation("junit:junit:4.12")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
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

tasks {
    val sampleProjects by creating(GradleBuild::class) {
        buildFile = File("${project.rootDir}/sample_projects/build.gradle")
        tasks = listOf("clean", "build")
    }
    sampleProjects.dependsOn("publishToMavenLocal")
    "test" { finalizedBy(sampleProjects) }
    named<Test>("test") {
        testLogging.exceptionFormat = TestExceptionFormat.FULL
    }
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
    from("javadoc")
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

artifacts {
    add("archives", javadocJar)
    add("archives", sourcesJar)
}
