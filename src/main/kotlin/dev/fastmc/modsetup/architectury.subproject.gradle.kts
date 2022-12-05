import dev.fastmc.modsetup.minecraftVersion
import dev.fastmc.modsetup.yarnMappings

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
            plugin("architectury.common")
        }
    }
    "forge" -> {
        apply {
            plugin("architectury.platform")
        }
    }
    "fabric" -> {
        apply {
            plugin("architectury.platform")
        }
    }
}