package dev.fastmc.modsetup

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService

private val javaNameRegex = "java(\\d+)".toRegex()

fun containJavaName(s: String) = javaNameRegex.containsMatchIn(s)

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
        it.languageVersion.set(javaVersion)
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
    it.configure { it.enabled = false }
}

fun disableTask(it: Task?) {
    it?.enabled = false
}

fun ModuleDependency.exclude(moduleName: String): ModuleDependency {
    return exclude(mapOf("module" to moduleName))
}

fun String.capitalize(): String {
    return this[0].uppercaseChar() + this.substring(1)
}

@Suppress("UNCHECKED_CAST")
fun <T, R : Provider<T>> Provider<R>.flatten(): R {
    return this.flatMap { it } as R
}