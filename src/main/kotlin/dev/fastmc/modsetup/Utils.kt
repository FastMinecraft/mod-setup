package dev.fastmc.modsetup

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.exclude

private val javaNameRegex = "java(\\d+)".toRegex()

val Project.targetModCoreName
    get() = "${javaVersion.javaName}ModCore"

val Project.targetModCoreOutputName
    get() = "${javaVersion.javaName}ModCoreOutput"

val Project.javaVersion: JavaLanguageVersion
    get() {
        var result: JavaLanguageVersion? = null

        val minecraftVersion = minecraftVersion
        if (minecraftVersion != null) {
            val split = minecraftVersion.split('.')
            val major = split[1].toInt()
            result = when {
                major >= 18 -> JavaLanguageVersion.of(17)
                major >= 17 -> JavaLanguageVersion.of(16)
                else -> JavaLanguageVersion.of(8)
            }
        }

        javaNameRegex.find(name)?.let {
            result = JavaLanguageVersion.of(it.groupValues[1].toInt())
        }

        return result ?: JavaLanguageVersion.of(8)
    }

val JavaLanguageVersion.fullJavaVersion: String
    get() = if (this <= JavaLanguageVersion.of(8)) "1.$this" else this.toString()

val JavaLanguageVersion.javaName: String
    get() = "java${this.asInt()}"

val Project.launchJavaToolchain: Provider<JavaLauncher>
    get() = (extensions.getByName("javaToolchains") as JavaToolchainService).launcherFor {
        languageVersion.set(javaVersion)
    }

private val minecraftVersionRegex = "\\d+\\.\\d+\\.\\d+".toRegex()

val Project.minecraftVersion
    get() = minecraftVersionRegex.find(this.displayName)?.value

val Project.yarnMappings
    get() = property("yarnMappings")

val Project.fabricLoaderVersion
    get() = property("fabricLoaderVersion")

val Project.forgeVersion
    get() = property("forgeVersion")


fun disableTask(it: TaskProvider<*>) {
    it.get().enabled = false
}

fun disableTask(it: Task) {
    it.enabled = false
}

fun ModuleDependency.exclude(moduleName: String): ModuleDependency {
    return exclude(module = moduleName)
}