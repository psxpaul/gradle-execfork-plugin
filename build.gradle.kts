import org.gradle.api.internal.classpath.ModuleRegistry
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.configurationcache.extensions.serviceOf

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.plugin-publish").version("1.0.0")
    kotlin("jvm") version "1.7.20"
    id("idea")
    id("maven-publish")
    id("java-gradle-plugin")
}

group = "com.github.psxpaul"
version = File(rootDir, "VERSION").readText().trim()

java {
    toolchain {
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.20")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")

    testImplementation("junit:junit:4.12")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    // https://github.com/gradle/gradle/issues/16774
    testRuntimeOnly(
        files(
            serviceOf<ModuleRegistry>().getModule("gradle-tooling-api-builders")
                .classpath.asFiles
        )
    )
}

pluginBundle {
    website = "http://github.com/psxpaul"
    vcsUrl = "https://github.com/psxpaul/gradle-execfork-plugin"
    tags = listOf("java", "exec", "background", "process")
}

gradlePlugin {
    plugins {
        create("execForkPlugin") {
            id = "com.github.psxpaul.execfork"
            displayName = "Gradle Exec Fork Plugin"
            description = "Execute Java or shell processes in the background during a build"
            implementationClass = "com.github.psxpaul.ExecForkPlugin"
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
        testLogging.showStandardStreams = false
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
