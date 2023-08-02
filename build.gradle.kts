
val kotlinVersion: String = "1.8.22"

plugins {
    kotlin("jvm") version "1.8.22"
    id("com.adarshr.test-logger") version "3.2.0"
    `maven-publish`
    signing
    idea
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "io.github.kscripting"
version = "0.6.0-SNAPSHOT"

kotlin {
    jvmToolchain(11)
}

sourceSets {
    create("itest") {
        kotlin.srcDir("$projectDir/src/itest/kotlin")
        resources.srcDir("$projectDir/src/itest/resources")

        compileClasspath += main.get().output + test.get().output
        runtimeClasspath += main.get().output + test.get().output
    }
}

configurations {
    get("itestImplementation").apply { extendsFrom(get("testImplementation")) }
}

tasks.create<Test>("itest") {
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
    testClassesDirs = sourceSets["itest"].output.classesDirs
    classpath = sourceSets["itest"].runtimeClasspath
    outputs.upToDateWhen { false }
    dependsOn(tasks["assemble"])

    doLast {
        println("Include tags: $itags")
        println("Exclude tags: $etags")
    }
}

tasks.create<Task>("printIntegrationClasspath") {
    doLast {
        println(sourceSets["itest"].runtimeClasspath.asPath)
    }
}

idea {
    module {
        testSources.from(sourceSets["itest"].kotlin.srcDirs)
    }
}

testlogger {
    showStandardStreams = true
    showFullStackTraces = false
}

tasks.test {
    useJUnitPlatform()
}

val testToolsJar by tasks.registering(Jar::class) {
    //archiveFileName.set("eulenspiegel-testHelpers-$version.jar")
    archiveClassifier.set("test")
    include("io/github/kscripting/shell/integration/tools/*")
    from(sourceSets["itest"].output)
}

val licencesSpec = Action<MavenPomLicenseSpec> {
    license {
        name.set("MIT License")
        url.set("https://opensource.org/licenses/MIT")
    }
}

val developersSpec = Action<MavenPomDeveloperSpec> {
    developer {
        id.set("aartiPl")
        name.set("Marcin Kuszczak")
        email.set("aarti@interia.pl")
    }
}

val scmSpec = Action<MavenPomScm> {
    connection.set("scm:git:git://https://github.com/kscripting/shell.git")
    developerConnection.set("scm:git:ssh:https://github.com/kscripting/shell.git")
    url.set("https://github.com/kscripting/shell")
}

val publishArtifact = artifacts.add("archives", testToolsJar)

publishing {
    publications {
        create<MavenPublication>("shell") {
            artifactId = "shell"
            from(components["java"])
            artifact(publishArtifact)

            pom {
                name.set("shell")
                description.set("Shell - library for interoperability with different system shells")
                url.set("https://github.com/kscripting/shell")

                licenses(licencesSpec)
                developers(developersSpec)
                scm(scmSpec)
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
    sign(publishing.publications["shell"])
}

dependencies {
    api("net.igsoft:typeutils:0.6.0-SNAPSHOT")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.arrow-kt:arrow-core:1.1.2")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.slf4j:slf4j-nop:2.0.5")

    testImplementation("org.junit.platform:junit-platform-suite-engine:1.9.0")
    testImplementation("org.junit.platform:junit-platform-suite-api:1.9.0")
    testImplementation("org.junit.platform:junit-platform-suite-commons:1.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("io.mockk:mockk:1.13.2")

    testImplementation(kotlin("script-runtime"))
}
