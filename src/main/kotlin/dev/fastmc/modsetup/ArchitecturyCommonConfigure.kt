package dev.fastmc.modsetup

import org.gradle.api.Project

class ArchitecturyCommonConfigure(project: Project) : ProjectConfigure("architecturyCommon", project) {
    val architecturyRoot = project.parent!!
    val projectExtension: ArchitecturyProjectExtension = architecturyRoot.extensions.getByType(ArchitecturyProjectExtension::class.java)

    override fun configure() {
        projectExtension.commonProject0 = project

        project.loom {
            accessWidenerPath.set(architecturyRoot.architecturyProject.accessWidenerPath)
        }

        project.architectury {
            @Suppress("UNCHECKED_CAST")
            common(project.property("platforms") as List<String>)
        }

        project.dependencies {
            add("modImplementation", "net.fabricmc:fabric-loader:${project.fabricLoaderVersion}")
            add("modCore", project(":shared", "modCoreOutput"))
        }

        disableTask(project.tasks.remapJar)
        disableTask(project.tasks.findByName("prepareRemapJar"))
    }
}