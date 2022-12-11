package dev.fastmc.modsetup

println("[Mod Setup] [architectury.subproject] [${project.displayName}] Configuring architectury subproject")

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