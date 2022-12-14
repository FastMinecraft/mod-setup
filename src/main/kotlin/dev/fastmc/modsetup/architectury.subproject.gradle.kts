package dev.fastmc.modsetup

println("[Mod Setup] [architectury.subproject] [${project.displayName}] Configuring architectury subproject")

afterEvaluate {
    group = rootProject.group
    version = rootProject.version
}

plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
    java
}

apply {
    plugin("architectury-plugin")
    plugin("dev.architectury.loom")
}

dependencies {
    "minecraft"("com.mojang:minecraft:$minecraftVersion")
    "mappings"("net.fabricmc:yarn:$yarnMappings")
    compileOnly(project(":shared"))
}

when (name) {
    "common" -> {
        apply {
            plugin("dev.fastmc.modsetup.architectury.common")
        }
    }
    "forge" -> {
        apply {
            plugin("dev.fastmc.modsetup.architectury.platform")
        }
    }
    "fabric" -> {
        apply {
            plugin("dev.fastmc.modsetup.architectury.platform")
        }
    }
}