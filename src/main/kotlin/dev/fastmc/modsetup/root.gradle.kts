package dev.fastmc.modsetup

extensions.create("runVmOptions", RunVmOptionExtension::class.java)

println("[Mod Setup] [root] [${project.displayName}] Configuring root project")

plugins {
    java
    kotlin("jvm")
    idea
}

disableTask(tasks.jar)

subprojects {
    afterEvaluate {
        java {
            toolchain {
                languageVersion.set(javaVersion)
            }
        }

        tasks {
            jar {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }

            compileJava {
                options.encoding = "UTF-8"
                sourceCompatibility = javaVersion.fullJavaVersion
                targetCompatibility = javaVersion.fullJavaVersion
            }

            compileKotlin {
                kotlinOptions {
                    jvmTarget = javaVersion.fullJavaVersion
                    freeCompilerArgs += listOf("-Xlambdas=indy", "-Xjvm-default=all")
                }
            }
        }
    }
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
    }

    tasks.register("cleanJars") {
        group = "build"
        File(buildDir, "libs").deleteRecursively()
    }

    kotlin {
        val jvmArgs = mutableSetOf<String>()
        (rootProject.findProperty("kotlin.daemon.jvm.options") as? String)
            ?.split("\\s+".toRegex())?.toCollection(jvmArgs)
        System.getProperty("gradle.kotlin.daemon.jvm.options")
            ?.split("\\s+".toRegex())?.toCollection(jvmArgs)
        kotlinDaemonJvmArgs = jvmArgs.toList()
    }

    val library by configurations.creating
    val libraryImplementation by configurations.creating

    dependencies {
        val kotlinVersion: String by rootProject
        val kotlinxCoroutineVersion: String by rootProject

        libraryImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
        libraryImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")

        library(libraryImplementation)
        implementation(libraryImplementation)
    }
}

findProject(":shared")?.apply {
    apply {
        plugin("shared")
    }
}

findProject(":forge-1.12.2")?.apply {
    apply {
        plugin("forge.1-12-2")
    }
}