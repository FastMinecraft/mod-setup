package dev.fastmc.modsetup

import net.minecraftforge.gradle.userdev.UserDevExtension
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

abstract class RunVmOptionExtension {
    private val options0 = mutableSetOf<String>()

    val options: Set<String> get() = options0

    init {
        if (System.getProperty("use_large_pages") == "true") {
            options0.add("-XX:+UseLargePages")
        }
    }

    fun add(vararg options: String) {
        options0.addAll(options)
    }

    fun add(options: Iterable<String>) {
        options0.addAll(options)
    }

    fun add(option: String) {
        options0.add(option)
    }

    fun addMultiLine(option: String) {
        option
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toCollection(options0)
    }
}

val Project.runVmOptions: RunVmOptionExtension
    get() = rootProject.extensions.getByType(RunVmOptionExtension::class.java)

abstract class AbstractProjectExtension {
    abstract val mixinConfigs: SetProperty<String>
    abstract val modPackage: Property<String>

    fun mixinConfig(config: String) {
        mixinConfigs.add(config)
    }

    fun mixinConfig(vararg configs: String) {
        mixinConfigs.addAll(*configs)
    }
}

abstract class ArchitecturyProjectExtension : AbstractProjectExtension() {
    internal lateinit var commonProject0 : Project
    val commonProject get() = commonProject0

    fun commonProject(block: Project.() -> Unit) {
        commonProject0.block()
    }

    internal val platformProjects0 = mutableListOf<Project>()
    val platformProjects: List<Project> get() = platformProjects0

    fun platformProject(block: Project.() -> Unit) {
        platformProjects.forEach {
            it.block()
        }
    }

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

abstract class ForgeProjectExtension(private val project: Project) : AbstractProjectExtension() {
    abstract val coreModClass: Property<String>
    abstract val devCoreModClass: Property<String>

    var accessTransformer: String? = null
        set(value) {
            field = value
            project.extensions.configure(UserDevExtension::class.java) {
                it.accessTransformer(project.file("src/main/resources/META-INF/$value"))
            }
        }
}