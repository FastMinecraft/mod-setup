package dev.fastmc.modsetup

import org.gradle.api.Project

class ArchitecturySubprojectConfigure(project: Project) : ProjectConfigure("architecturySubproject", project) {
    override fun configure() {
        project.pluginManager {
            apply("dev.architectury.loom")
            apply("architectury-plugin")
        }

        project.tasks.processResources {
            filesMatching(listOf("fabric.mod.json", "*/mods.toml")) {
                it.expand(mapOf("version" to rootProject.version))
            }
        }

        project.base {
            archivesName.set("${rootProject.name}-${project.name}-${project.minecraftVersion}")
        }

        project.dependencies {
            add("minecraft", "com.mojang:minecraft:${project.minecraftVersion}")
            add("mappings", "net.fabricmc:yarn:${project.yarnMappings}")
            add("compileOnly", project(":shared", "apiElements"))
        }

        when (project.name) {
            "common" -> {
                ArchitecturyCommonConfigure(project).configure()
            }
            "forge" -> {
                ArchitecturyPlatformConfigure(project).configure()
                ArchitecturyForgeConfigure(project).configure()
            }
            "fabric" -> {
                ArchitecturyPlatformConfigure(project).configure()
                ArchitecturyFabricConfigure(project).configure()
            }
        }
    }
}