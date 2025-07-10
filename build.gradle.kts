import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String = "2.1.21"

plugins {
    `maven-publish`
    kotlin("jvm") version "2.1.21"
    id("com.adarshr.test-logger") version "4.0.0"
    id("com.vanniktech.maven.publish") version "0.33.0"
}

group = "io.github.kscripting"
version = "0.6.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompile>().all {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

testlogger {
    showStandardStreams = true
    showFullStackTraces = false
}

tasks.test {
    useJUnitPlatform()
}

// NOTE: deploying snapshots is complicated with this plugin
// It is better to use repository with the staging version of artifact to test it
mavenPublishing {
    pom {
        name.set("shell")
        description.set("Shell - library for interoperability with different system shells")
        url.set("https://github.com/kscripting/shell")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("aartiPl")
                name.set("Marcin Kuszczak")
                email.set("aarti@interia.pl")
            }
        }
        scm {
            connection.set("scm:git:git://https://github.com/kscripting/shell.git")
            developerConnection.set("scm:git:ssh:https://github.com/kscripting/shell.git")
            url.set("https://github.com/kscripting/shell")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven-all:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("io.arrow-kt:arrow-core:2.1.2")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.slf4j:slf4j-nop:2.0.17")

    val junitVersion = "6.0.0-M1"
    testImplementation("org.junit.platform:junit-platform-suite-engine:$junitVersion")
    testImplementation("org.junit.platform:junit-platform-suite-api:$junitVersion")
    testImplementation("org.junit.platform:junit-platform-suite-commons:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")

    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")
    testImplementation("io.mockk:mockk:1.14.4")
    testImplementation(kotlin("script-runtime"))
}
