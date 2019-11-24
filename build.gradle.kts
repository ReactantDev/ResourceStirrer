import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI
import org.jetbrains.dokka.gradle.DokkaTask

val isSnapshot = true
group = "dev.reactant"
version = "0.1.4${if (isSnapshot) "-SNAPSHOT" else ""}"

val kotlinVersion = "1.3.31"

plugins {
    java
    `maven-publish`
    signing
    kotlin("jvm") version "1.3.31"
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("com.jfrog.bintray") version "1.8.4"
    id("org.jetbrains.dokka") version "0.10.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=compatibility")
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = URI.create("https://hub.spigotmc.org/nexus/content/repositories/snapshots") }
    maven { url = URI.create("https://dl.bintray.com/reactant/reactant") }
    maven { url = URI.create("https://oss.sonatype.org/content/repositories/snapshots/") }
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8", kotlinVersion))
    compileOnly("dev.reactant:reactant:0.1.4")
    compileOnly("org.spigotmc:spigot-api:1.14.2-R0.1-SNAPSHOT")

    implementation("net.lingala.zip4j:zip4j:2.1.3")
    implementation("commons-codec:commons-codec:1.13")

}

val dokka = (tasks["dokka"] as DokkaTask).apply {
    outputFormat = "html"
}

val dokkaJavadoc by tasks.registering(DokkaTask::class) {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
}

val dokkaJar by tasks.registering(Jar::class) {
    dependsOn(dokka)
    archiveClassifier.set("dokka")
    from(tasks.dokka)
}

gradle.taskGraph.whenReady {
    if (allTasks.any { it is Sign }) {
        allprojects {
            extra["signing.keyId"] = findProperty("signingKeyId") as String?;
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val shadowJar = (tasks["shadowJar"] as ShadowJar).apply {
    relocate("net.lingala.zip4j", "dev.reactant.resourcestirrer.zip4j")
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

val deployPlugin by tasks.registering(Copy::class) {
    dependsOn(shadowJar)
    System.getenv("PLUGIN_DEPLOY_PATH")?.let {
        from(shadowJar)
        into(it)
    }
}

val build = (tasks["build"] as Task).apply {
    arrayOf(
            deployPlugin,
            shadowJar,
            sourcesJar
    ).forEach { dependsOn(it) }
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(shadowJar)
            artifact(javadocJar.get())
            artifact(dokkaJar.get())

            groupId = group.toString()
            artifactId = project.name
            version = version

            pom {
                name.set(project.name)
                description.set("Resource stirrer library for reactant")
                url.set("https://reactant.dev")
                licenses {
                    license {
                        name.set("GPL-3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:git@gitlab.com:reactant/resourcestirrer.git")
                    url.set("https://gitlab.com/reactant/resourcestirrer/")
                }

                developers {
                    developer {
                        id.set("setako")
                        name.set("Setako")
                        organization.set("Reactant Dev Team")
                        organizationUrl.set("https://gitlab.com/reactant")
                    }
                }

            }


        }
    }

    repositories {
        maven {

            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }


        }
    }
}
if (!isSnapshot) {
    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey?.replace("\\n", "\n"), signingPassword)
        sign(publishing.publications["maven"])
    }
}
