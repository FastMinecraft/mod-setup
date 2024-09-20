package dev.fastmc.modsetup

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

class RootConfigure(project: Project) : ProjectConfigure("root", project) {
    override fun configure() {
        project.extensions.create("runVmOptions", RunVmOptionExtension::class.java)

        project.pluginManager {
            apply("java")
            apply("idea")
            apply("org.jetbrains.kotlin.jvm")
        }

        project.repositories {
            mavenCentral()
        }

        project.tasks.jar {
            isEnabled = false
        }

        project.subprojects { subproject ->
            subproject.pluginManager {
                apply("java")
                apply("idea")
                apply("org.jetbrains.kotlin.jvm")
            }

            subproject.repositories {
                mavenCentral()
            }

            subproject.dependencies {
                add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            }

            subproject.base {
                val minecraftVersion = subproject.minecraftVersion
                if (minecraftVersion != null) {
                    archivesName.set("${rootProject.name}-${minecraftVersion}")
                } else {
                    archivesName.set("${rootProject.name}-${subproject.name}")
                }
            }

            project.configurations {
                all { configuration ->
                    configuration.resolutionStrategy {
                        it.force("dev.architectury:architectury-transformer:5.2.9999")
                    }
                }
            }

            val library = subproject.configurations.create("library")
            val libraryImplementation = subproject.configurations.create("libraryImplementation")

            library.extendsFrom(libraryImplementation)
            subproject.configurations.getByName("implementation").extendsFrom(library)

            subproject.sourceSets.getByName("main").let { sourceSet ->
                val modCore = subproject.configurations.create("modCore")
                val modCoreRuntime = subproject.configurations.create("modCoreRuntime")
                val modCoreOutput = subproject.configurations.create("modCoreOutput")
                modCoreRuntime.extendsFrom(modCore)
                modCoreOutput.extendsFrom(modCoreRuntime)
                subproject.configurations.getByName(sourceSet.implementationConfigurationName).extendsFrom(modCore)
                modCoreOutput.artifacts.addAllLater(subproject.provider {
                    subproject.configurations.findByName(sourceSet.apiElementsConfigurationName)?.artifacts
                        ?: emptySet()
                })
            }

            subproject.tasks.register("cleanJars") {
                it.group = "build"
                @Suppress("ObjectLiteralToLambda")
                it.doLast(object : Action<Task> {
                    override fun execute(t: Task) {
                        File(subproject.layout.buildDirectory.asFile.get(), "libs").deleteRecursively()
                        File(subproject.layout.buildDirectory.asFile.get(), "devlibs").deleteRecursively()
                    }
                })
            }

            subproject.java {
                toolchain {
                    it.languageVersion.set(JavaLanguageVersion.of(8))
                }
            }

            subproject.tasks.jar {
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }

            subproject.tasks.withType(JavaCompile::class.java) {
                it.options.encoding = "UTF-8"
            }

            val jvmArgs = mutableSetOf<String>()
            (rootProject.findProperty("kotlin.daemon.jvmargs") as? String)
                ?.split("\\s+".toRegex())?.toCollection(jvmArgs)
            System.getenv("KOTLIN_DAEMON_VM")
                ?.split("\\s+".toRegex())?.toCollection(jvmArgs)

            subproject.kotlinExtension.kotlinDaemonJvmArgs = jvmArgs.toList()
            subproject.extensions.configure(KotlinJvmProjectExtension::class.java) { kotlinJvmProjectExtension ->
                kotlinJvmProjectExtension.compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                    freeCompilerArgs.map { it + listOf("-Xbackend-threads=0", "-Xjvm-default=all") }
                }
            }
        }

        project.subprojects { subproject ->
            subproject.afterEvaluate {
                (subproject.tasks.findByName("modLoaderJar") as Jar?)?.apply {
                    archiveClassifier.set("release")
                }
            }
        }

        project.findProject(":shared")?.let {
            SharedConfigure(it).configure()
        }

        project.findProject(":forge-1.12.2")?.let {
            LegacyForgeConfigure(it).configure()
        }

        project.subprojects {
            if (it.name.startsWith("architectury-")) {
                ArchitecturyRootConfigure(it).configure()
            }
        }

        project.tasks.create("clearRuns") { clearRuns ->
            project.subprojects.forEach {
                it.tasks.findByName("ideaSyncTask")?.finalizedBy(clearRuns)
            }

            rootProject.file(".idea/runConfigurations").listFiles()?.forEach {
                if (it.name.startsWith("Minecraft_Client") || it.name.startsWith("Minecraft_Server")) {
                    it.delete()
                }
            }
        }
    }
}