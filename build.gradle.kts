import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String = "2.1.21"

plugins {
    kotlin("jvm") version "2.1.21"
    id("com.adarshr.test-logger") version "4.0.0"
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

group = "io.github.kscripting"
version = "0.6.0-SNAPSHOT"

sourceSets {
    create("integration") {
//        test {  //With that idea can understand that 'integration' is test source set and do not complain about test
//        names starting with upper case, but it doesn't compile correctly with it
        java.srcDir("$projectDir/src/integration/kotlin")
        resources.srcDir("$projectDir/src/integration/resources")
        compileClasspath += main.get().output + test.get().output
        runtimeClasspath += main.get().output + test.get().output
    }
//    }
}

configurations {
    get("integrationImplementation").apply { extendsFrom(get("testImplementation")) }
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompile>().all {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}


java {
    withJavadocJar()
    withSourcesJar()
}

tasks.register<Test>("integration") {
    val itags = System.getProperty("includeTags") ?: ""
    val etags = System.getProperty("excludeTags") ?: ""

    useJUnitPlatform {
        if (itags.isNotBlank()) {
            includeTags(itags)
        }

        if (etags.isNotBlank()) {
            excludeTags(etags)
        }
    }

    systemProperty("osType", System.getProperty("osType"))
    systemProperty("projectPath", projectDir.absolutePath)
    systemProperty("shellPath", System.getProperty("shellPath"))

    description = "Runs the integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integration"].output.classesDirs
    classpath = sourceSets["integration"].runtimeClasspath
    outputs.upToDateWhen { false }
    mustRunAfter(tasks["test"])
    //dependsOn(tasks["assemble"], tasks["test"])

    doLast {
        println("Include tags: $itags")
        println("Exclude tags: $etags")
    }
}

tasks.register<Task>("printIntegrationClasspath") {
    doLast {
        println(sourceSets["integration"].runtimeClasspath.asPath)
    }
}

testlogger {
    showStandardStreams = true
    showFullStackTraces = false
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "shell"
            from(components["java"])

            pom {
                name.set("kscript")
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
        }
    }

    repositories {
        maven {
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (project.version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

            credentials {
                username = project.findProperty("sonatype.user") as String? ?: System.getenv("SONATYPE_USER")
                password = project.findProperty("sonatype.password") as String? ?: System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven-all:$kotlinVersion")
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
