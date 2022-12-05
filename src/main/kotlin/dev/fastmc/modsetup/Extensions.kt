package dev.fastmc.modsetup

import org.gradle.api.file.RegularFileProperty

abstract class RunVmOptionExtension {
    internal val runVmOptions = mutableSetOf<String>()

    fun add(vararg options: String) {
        runVmOptions.addAll(options)
    }

    fun add(options: Iterable<String>) {
        runVmOptions.addAll(options)
    }

    fun add(option: String) {
        runVmOptions.add(option)
    }

    fun addMultiLine(option: String) {
        option
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toCollection(runVmOptions)
    }
}

abstract class ArchitecturyProjectExtension {
    internal val mixinConfigs = mutableSetOf<String>()
    abstract val accessWidenerPath: RegularFileProperty
    val forge = ArchitecturyForgeExtension()

    fun mixinConfig(config: String) {
        mixinConfigs.add(config)
    }

    fun mixinConfig(vararg configs: String) {
        mixinConfigs.addAll(configs)
    }

    fun forge(block: ArchitecturyForgeExtension.() -> Unit) {
        forge.block()
    }
}

class ArchitecturyForgeExtension {
    val atPatch = AtPatchExtension()

    fun atPatch(block: AtPatchExtension.() -> Unit) {
        atPatch.block()
    }

    class AtPatchExtension {
        internal val patches = mutableMapOf<String, String>()

        fun patch(patch: String, target: String) {
            patches[patch] = target
        }
    }
}