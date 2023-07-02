import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "dev.fastmc"
version = "1.3-SNAPSHOT"

plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm")
    id("dev.fastmc.maven-repo").version("1.0.0")
}

repositories {
    maven("https://maven.fastmc.dev/")
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://files.minecraftforge.net/maven/")
    maven("https://maven.architectury.dev/")
}

val kotlinVersion: String by project

configurations {
    all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "org.jetbrains.kotlin") {
                    useVersion(kotlinVersion)
                }
            }
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation(kotlin("stdlib-jdk8", kotlinVersion))

    implementation("dev.luna5ama:jar-optimizer:1.2-SNAPSHOT")
    implementation("dev.fastmc:multi-jdk:1.1-SNAPSHOT")
    implementation("dev.fastmc:fast-remapper:1.1-SNAPSHOT")
    implementation("dev.fastmc:mod-loader-plugin:1.1-SNAPSHOT")

    implementation("architectury-plugin:architectury-plugin.gradle.plugin:3.4-SNAPSHOT")
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.0-SNAPSHOT")
    implementation("net.minecraftforge.gradle:ForgeGradle:6.0.7")
}

gradlePlugin {
    plugins {
        create("mod-setup") {
            id = "dev.fastmc.mod-setup"
            displayName = "Mod Setup"
            description = "Gradle plugin for setting up Minecraft mod project"
            implementationClass = "dev.fastmc.modsetup.ModSetupPlugin"
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

kotlin {
    val regex = "\\s+".toRegex()
    val jvmArgs = mutableSetOf<String>()
    (rootProject.findProperty("kotlin.daemon.jvm.options") as? String)
        ?.split(regex)?.toCollection(jvmArgs)
    System.getProperty("gradle.kotlin.daemon.jvm.options")
        ?.split(regex)?.toCollection(jvmArgs)
    kotlinDaemonJvmArgs = jvmArgs.toList()
}

tasks {
    withType(JavaCompile::class.java) {
        options.encoding = "UTF-8"
    }

    withType(KotlinCompile::class.java) {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xlambdas=indy", "-Xbackend-threads=0")
        }
    }
}
