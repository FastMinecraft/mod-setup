package dev.fastmc.modsetup

extensions.create("architecturyProject", ArchitecturyProjectExtension::class.java)

println("[Mod Setup] [architectury] [${project.displayName}] Configuring architectury root")

plugins {
    id("architectury-plugin")
    java
    idea
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}

architectury {
    minecraft = minecraftVersion!!
}

disableTask(tasks.jar)

val architecturyRootProject = project
val platforms = mutableListOf<String>()
architecturyRootProject.extra["platforms"] = platforms

subprojects.forEach {
    when (it.name) {
        "forge" -> {
            platforms.add("forge")
            it.extra["loom.platform"] = "forge"
        }
        "fabric" -> {
            platforms.add("fabric")
            it.extra["loom.platform"] = "fabric"
        }
    }
}

subprojects {
    apply {
        plugin("dev.fastmc.modsetup.architectury.subproject")
    }
}