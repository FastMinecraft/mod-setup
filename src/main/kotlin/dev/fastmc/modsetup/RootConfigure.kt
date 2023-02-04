package dev.fastmc.modsetup

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
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

            subproject.extensions.configure(KotlinJvmProjectExtension::class.java) {
                val jvmArgs = mutableSetOf<String>()
                (rootProject.findProperty("kotlin.daemon.jvm.options") as? String)
                    ?.split("\\s+".toRegex())?.toCollection(jvmArgs)
                System.getProperty("gradle.kotlin.daemon.jvm.options")
                    ?.split("\\s+".toRegex())?.toCollection(jvmArgs)
                it.kotlinDaemonJvmArgs = jvmArgs.toList()
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

            subproject.sourceSets.configureEach { sourceSet ->
                if (sourceSet.name == "test") return@configureEach
                val newName = if (sourceSet.name == "main") "modCore" else "${sourceSet.name}ModCore"
                val modCoreRuntime = subproject.configurations.create("${newName}Runtime")
                val modCore = subproject.configurations.create(newName)
                val modCoreOutput = subproject.configurations.create("${newName}Output")
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
                        File(subproject.buildDir, "libs").deleteRecursively()
                        File(subproject.buildDir, "devlibs").deleteRecursively()
                    }
                })
            }

            val javaVersion = subproject.javaVersion

            subproject.java {
                toolchain {
                    it.languageVersion.set(javaVersion)
                }
            }

            subproject.tasks.jar {
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }

            subproject.tasks.withType(JavaCompile::class.java) {
                it.options.encoding = "UTF-8"
            }

            subproject.tasks.withType(KotlinCompile::class.java) {
                it.kotlinOptions {
                    if (!containJavaName(it.name)) {
                        jvmTarget = javaVersion.fullJavaVersion
                    }
                    freeCompilerArgs += listOf("-Xlambdas=indy", "-Xjvm-default=all")
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
    }
}