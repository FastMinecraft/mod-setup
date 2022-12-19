package dev.fastmc.modsetup

println("[Mod Setup] [architectury.common] [${project.displayName}] Configuring architectury common project")
val thisProject = project

parent!!.afterEvaluate {
    thisProject.loom.accessWidenerPath.set(
        extensions.getByType(ArchitecturyProjectExtension::class.java).accessWidenerPath
    )
}

plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
    java
}

architectury {
    @Suppress("UNCHECKED_CAST")
    common(project.property("platforms") as List<String>)
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    "modCore"(project(":shared:${javaVersion.javaName}", "modCore"))
}

afterEvaluate {
    tasks {
        processResources {
            filesMatching("fabric.mod.json") {
                expand("version" to rootProject.version)
            }
        }
    }
}

disableTask(tasks.remapJar)
disableTask(tasks.prepareRemapJar)