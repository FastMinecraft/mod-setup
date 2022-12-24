package dev.fastmc.modsetup

extensions.create("runVmOptions", RunVmOptionExtension::class.java)

println("[Mod Setup] [root] [${project.displayName}] Configuring root project")

plugins {
    java
    kotlin("jvm")
    idea
}

repositories {
    mavenCentral()
}

dependencies {
    val kotlinVersion: String by rootProject
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}

disableTask(tasks.jar)

subprojects {
    apply {
        plugin("java")
        plugin("kotlin")
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

    sourceSets.configureEach {
        if (this == sourceSets.test.get()) return@configureEach
        val newName = if (this == sourceSets.main.get()) "modCore" else "${name}ModCore"
        val modCoreRuntime = configurations.create("${newName}Runtime")
        val modCore = configurations.create(newName)
        val modCoreOutput = configurations.create("${newName}Output")
        modCoreRuntime.extendsFrom(modCore)
        modCoreOutput.extendsFrom(modCoreRuntime)
        configurations.getByName(implementationConfigurationName).extendsFrom(modCore)
        project.afterEvaluate {
            configurations.findByName(apiElementsConfigurationName)?.let {
                modCoreOutput.artifacts.addAll(it.artifacts)
            }
        }
    }

    dependencies {
        library(libraryImplementation)
        implementation(libraryImplementation)
    }

    tasks.register("cleanJars") {
        group = "build"
        doLast {
            File(buildDir, "libs").deleteRecursively()
            File(buildDir, "devlibs").deleteRecursively()
        }
    }

    afterEvaluate {
        java {
            toolchain {
                languageVersion.set(javaVersion)
            }
        }

        tasks {
            jar {
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
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

findProject(":shared")?.apply {
    apply {
        plugin("dev.fastmc.modsetup.shared")
    }
}

findProject(":forge-1.12.2")?.apply {
    apply {
        plugin("dev.fastmc.modsetup.forge.1-12-2")
    }
}

if (subprojects.any { it.name.contains("architectury") }) {
    apply {
        plugin("dev.fastmc.modsetup.root.architectury")
    }
}