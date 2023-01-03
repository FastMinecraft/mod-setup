group = "dev.fastmc"
version = "1.1-SNAPSHOT"

plugins {
    `kotlin-dsl`
    `maven-publish`
    kotlin("jvm")
    id("dev.fastmc.maven-repo").version("1.0.0")
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.fastmc.dev/")
    maven("https://maven.fabricmc.net/")
    maven("https://files.minecraftforge.net/maven/")
    maven("https://maven.architectury.dev/")
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

dependencies {
    val kotlinVersion: String by project

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation(kotlin("stdlib-jdk8", kotlinVersion))

    implementation("dev.luna5ama:jar-optimizer:1.2-SNAPSHOT")
    implementation("dev.fastmc:multi-jdk:1.1-SNAPSHOT")
    implementation("dev.fastmc:fast-remapper:1.0-SNAPSHOT")
    implementation("dev.fastmc:mod-loader-plugin:1.0-SNAPSHOT")

    implementation("architectury-plugin:architectury-plugin.gradle.plugin:3.4-SNAPSHOT")
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.0-SNAPSHOT")
    implementation("net.minecraftforge.gradle:ForgeGradle:5.1.58")
}

afterEvaluate {
    tasks {
        compileJava {
            options.encoding = "UTF-8"
            sourceCompatibility = "17"
            targetCompatibility = "17"
        }

        compileKotlin {
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs = listOf("-Xbackend-threads=0")
            }
        }
    }
}