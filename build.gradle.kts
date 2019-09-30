import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

group = "dev.reactant"
version = "0.1.3"

val kotlinVersion = "1.3.31"

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.3.31"
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("com.jfrog.bintray") version "1.8.4"
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
    maven { url = URI.create("https://hub.spigotmc.org/nexus/content/repositories/snapshots") }
    maven { url = URI.create("https://dl.bintray.com/reactant/reactant") }
    maven { url = URI.create("https://oss.sonatype.org/content/repositories/snapshots/") }
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8", kotlinVersion))
    compileOnly("dev.reactant:reactant:0.1.3")
    compileOnly("org.spigotmc:spigot-api:1.14.2-R0.1-SNAPSHOT")

    implementation("net.lingala.zip4j:zip4j:2.1.3")
    implementation("commons-codec:commons-codec:1.13")

}


val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val shadowJar = (tasks["shadowJar"] as ShadowJar).apply {
    relocate("net.lingala.zip4j", "dev.reactant.resourcestirrer.zip4j")
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
            sourcesJar,
            shadowJar,
            deployPlugin
    ).forEach { dependsOn(it) }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(shadowJar)

            groupId = group.toString()
            artifactId = project.name
            version = version
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    setPublications("maven")
    publish = true
    override = true
    pkg.apply {
        repo = "reactant"
        name = project.name
        userOrg = "reactant"
        setLicenses("GPL-3.0")
        vcsUrl = "https://gitlab.com/reactant/ResourceStirrer"
    }
}
