package dev.fastmc.modsetup

import net.minecraftforge.gradle.userdev.UserDevExtension
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.configure

abstract class RunVmOptionExtension {
    internal val options = mutableSetOf<String>()

    fun add(vararg options: String) {
        this.options.addAll(options)
    }

    fun add(options: Iterable<String>) {
        this.options.addAll(options)
    }

    fun add(option: String) {
        options.add(option)
    }

    fun addMultiLine(option: String) {
        option
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toCollection(options)
    }
}

val Project.runVmOptions: RunVmOptionExtension
    get() = rootProject.extensions.getByType(RunVmOptionExtension::class.java)

abstract class MixinConfigContainer {
    internal val mixinConfigs = mutableSetOf<String>()

    fun mixinConfig(config: String) {
        mixinConfigs.add(config)
    }

    fun mixinConfig(vararg configs: String) {
        mixinConfigs.addAll(configs)
    }
}

abstract class ArchitecturyProjectExtension : MixinConfigContainer() {
    abstract val accessWidenerPath: RegularFileProperty
    val forge = ArchitecturyForgeExtension()

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

abstract class ForgeProjectExtension(private val project: Project) : MixinConfigContainer() {
    abstract val coreModClass: Property<String>

    var accessTransformer: String? = null
        set(value) {
            field = value
            project.configure<UserDevExtension> {
                accessTransformer(project.file("src/main/resources/META-INF/$value"))
            }
        }
}