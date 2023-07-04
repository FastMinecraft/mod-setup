package dev.fastmc.modsetup

import org.gradle.api.Project
import org.gradle.api.Task
import org.jetbrains.kotlin.gradle.plugin.extraProperties

class ArchitecturyRootConfigure(project: Project) : ProjectConfigure("architecturyRoot", project) {
    val projectExtension = project.extensions.create("architecturyProject", ArchitecturyProjectExtension::class.java)
    private val flag = BooleanArray(project.allprojects.size) { false }

    override fun configure() {
        project.pluginManager {
            apply("architectury-plugin")
            apply("dev.fastmc.mod-loader-plugin")
        }

        project.idea {
            module {
                it.excludeDirs.add(this@ArchitecturyRootConfigure.project.file("run"))
            }
        }

        project.architectury {
            minecraft = project.minecraftVersion!!
        }

        disableTask(project.tasks.jar)

        val platforms = mutableListOf<String>()
        val paths = mutableListOf<String>()
        project.extraProperties["platforms"] = platforms

        project.subprojects {
            when (it.name) {
                "forge" -> {
                    platforms.add("forge")
                    it.extraProperties["loom.platform"] = "forge"
                    paths.add(it.path)
                }
                "fabric" -> {
                    platforms.add("fabric")
                    it.extraProperties["loom.platform"] = "fabric"
                    paths.add(it.path)
                }
            }
        }

        project.dependencies {
            paths.forEach {
                add("modLoaderPlatforms", project(it, "releaseElements"))
            }
        }

        project.modLoader {
            modPackage.set(projectExtension.modPackage)
            mcVersion.set(project.minecraftVersion)
        }

        project.tasks.modLoaderJar {
            archiveClassifier.set("release")
        }

        project.subprojects {
            ArchitecturySubprojectConfigure(it).configure()
        }

        project.allprojects.forEachIndexed { i, it ->
            it.afterEvaluate {
                synchronized(flag) {
                    flag[i] = true
                    if (!flag.contains(false)) {
                        afterAll()
                    }
                }
            }
        }
    }

    private fun afterAll() {
        val subprojects = project.subprojects.sortedByDescending { it.name }

        fun getTasks(taskName: String): List<Task> {
            return subprojects.mapNotNull {
                it.tasks.findByName(taskName)
            }
        }

        val unpickJar = getTasks("unpickJars")
        val genSourcesWithCfr = getTasks("genSourcesWithCfr")
        val genSourcesWithFernFlower = getTasks("genSourcesWithFernFlower")


        fun foldTasks(tasks: List<Task>) {
            tasks.fold(null as Task?) { prev, task ->
                if (prev != null) {
                    task.mustRunAfter(prev)
                }
                task
            }
        }

        foldTasks(unpickJar)
        foldTasks(genSourcesWithCfr)
        foldTasks(genSourcesWithFernFlower)

        genSourcesWithCfr.forEach {
            it.mustRunAfter(unpickJar)
        }

        genSourcesWithFernFlower.forEach {
            it.mustRunAfter(unpickJar)
        }
    }
}