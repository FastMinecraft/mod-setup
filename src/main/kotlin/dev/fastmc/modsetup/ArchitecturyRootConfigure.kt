package dev.fastmc.modsetup

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.File

class ArchitecturyRootConfigure(project: Project) : ProjectConfigure("architecturyRoot", project) {
    val projectExtension = project.extensions.create("architecturyProject", ArchitecturyProjectExtension::class.java)

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
    }
}