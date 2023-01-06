package dev.fastmc.modsetup

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar

class ArchitecturyPlatformConfigure(project: Project) : ProjectConfigure("architecturyPlatform", project) {
    val architecturyRootProject: Project = project.parent!!
    val architecturyCommonProject: Project = project.project("${architecturyRootProject.path}:common")
    val projectExtension: ArchitecturyProjectExtension =
        architecturyRootProject.extensions.getByType(ArchitecturyProjectExtension::class.java)
    val platform = project.property("loom.platform") as String

    val releaseElements: NamedDomainObjectProvider<Configuration> = project.configurations.register("releaseElements")

    override fun configure() {
        project.pluginManager {
            apply("dev.luna5ama.jar-optimizer")
        }

        project.loom {
            accessWidenerPath.set(architecturyCommonProject.loom.accessWidenerPath)
        }

        project.dependencies {
            add("implementation", project(architecturyCommonProject.path, "namedElements"))
            add(
                "modCoreRuntime",
                project(architecturyCommonProject.path, "transformProduction${platform.capitalize()}")
            )
            add("modCore", project(architecturyCommonProject.path, "modCore"))
        }

        project.tasks.jar {
            fromConfiguration("modCoreRuntime") {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }

            archiveClassifier.set("dev")
        }

        project.tasks.remapJar {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            archiveClassifier.set("remapped")
        }

        val devModJar = project.tasks.register<Jar>("devModJar") {
            fromJarTask(project.tasks.jar)
            archiveClassifier.set("devmod")
        }

        val fatJar = project.tasks.register<Jar>("fatJar") {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            fromJarTask(project.tasks.remapJar)
            fromConfiguration("library") {
                exclude("META-INF/**")
            }

            manifest.from(project.tasks.jar.outputManifest)

            archiveClassifier.set("farJar")
        }

        val optimizeFatJar = project.jarOptimizer.register(fatJar, projectExtension.modPackage.map { listOf(it) })

        project.artifacts {
            it.add("archives", devModJar)
            it.add("archives", optimizeFatJar)
            it.add(releaseElements, optimizeFatJar)
        }
    }
}
