package dev.fastmc.modsetup

import org.gradle.api.Project

class ArchitecturyCommonConfigure(project: Project) : ProjectConfigure("architecturyCommon", project) {
    val architecturyRoot = project.parent!!

    override fun configure() {
        project.loom {
            accessWidenerPath.set(architecturyRoot.architecturyProject.accessWidenerPath)
        }

        project.architectury {
            @Suppress("UNCHECKED_CAST")
            common(project.property("platforms") as List<String>)
        }

        project.dependencies {
            add("modImplementation", "net.fabricmc:fabric-loader:${project.fabricLoaderVersion}")
            add("modCore", project(":shared", project.targetModCoreOutputName))
        }

        disableTask(project.tasks.remapJar)
        disableTask(project.tasks.findByName("prepareRemapJar"))
    }
}