package dev.fastmc.modsetup

afterEvaluate {
    group = rootProject.group
    version = rootProject.version
}

val projectExtension = extensions.create("architecturyProject", ArchitecturyProjectExtension::class.java)

println("[Mod Setup] [architectury] [${project.displayName}] Configuring architectury root")

plugins {
    id("architectury-plugin")
    java
    idea
    id("dev.fastmc.mod-loader-plugin")
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}

architectury {
    minecraft = minecraftVersion!!
}

val architecturyRootProject = project
val platforms = mutableListOf<String>()
architecturyRootProject.extra["platforms"] = platforms

val paths = mutableListOf<String>()

subprojects.forEach {
    when (it.name) {
        "forge" -> {
            platforms.add("forge")
            it.extra["loom.platform"] = "forge"
            paths.add(it.path)
        }
        "fabric" -> {
            platforms.add("fabric")
            it.extra["loom.platform"] = "fabric"
            paths.add(it.path)
        }
    }
}

dependencies {
    paths.forEach {
        modLoaderPlatforms(project(it, "releaseElements"))
    }
}

modLoader {
    modPackage.set(projectExtension.modPackage)
}

subprojects {
    apply {
        plugin("dev.fastmc.modsetup.architectury.subproject")
    }
}

tasks {
    modLoaderJar {
        archiveBaseName.set(rootProject.name)
        archiveAppendix.set(project.minecraftVersion)
        archiveClassifier.set("release")
    }
}