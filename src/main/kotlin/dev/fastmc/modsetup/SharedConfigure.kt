package dev.fastmc.modsetup

import dev.fastmc.multijdk.MultiJdkExtension
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion

class SharedConfigure(project: Project) : ProjectConfigure("shared", project) {
    override fun configure() {
        project.pluginManager.apply("dev.fastmc.multi-jdk")

        project.extensions.configure(MultiJdkExtension::class.java) { multiJdk ->
            multiJdk.baseJavaVersion(JavaLanguageVersion.of(8))
            project.rootProject.allprojects
                .map { it.javaVersion }
                .forEach {
                    multiJdk.newJavaVersion(it)
                }
            multiJdk.sourceSets.values.forEach { sourceSet ->
                project.configurations.named(sourceSet.implementationConfigurationName) {
                    it.extendsFrom(project.configurations.getByName("implementation"))
                }
            }
        }

        project.configurations.getByName("modCore")
            .extendsFrom(project.configurations.getByName("java8ModCore"))
    }
}