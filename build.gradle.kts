import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.2.2"
    alias(libs.plugins.lombok);
}

project.group = "org.reprogle"
project.version = "1.0"
project.description =
    "A library for wrapping common tasks my plugins do, such as SQLite storage or command creation/registration"

val isReleaseBuild = project.hasProperty("releaseBuild")
val forceBuildId = project.hasProperty("forceBuildId")

if (!isReleaseBuild || forceBuildId) {
    val timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
    val newVersion = "${project.version}-SNAPSHOT-${timestamp}"
    project.version = newVersion
    println("Auto build ID enabled → version set to $newVersion")
} else {
    println("Release build → using version ${project.version}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    mavenCentral()
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.boosted.yaml)
    implementation(libs.bstats)
    compileOnly(libs.guice)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    archiveClassifier.set("") // Replace the normal JAR
    mergeServiceFiles()
    exclude("META-INF/*.MF")

    relocate("org.bstats", "org.reprogle.bytelib.bstats")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "$group"
            artifactId = "bytelib"
            version = version

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/TerrorByteTW/ByteLib")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}