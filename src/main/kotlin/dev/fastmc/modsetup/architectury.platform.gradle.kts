import dev.fastmc.modsetup.javaName
import dev.fastmc.modsetup.javaVersion
import dev.fastmc.modsetup.minecraftVersion

println("[Mod Setup] [architectury.platform] [${project.displayName}] Configuring architectury platform project")

val platform = project.property("loom.platform") as String
val architecturyCommonProject = project("${project.parent!!.path}:common")

group = rootProject.group
version = rootProject.version

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    java
}

apply {
    plugin("architectury.$platform")
}

architectury {
    platformSetupLoomIde()
}

loom {
    accessWidenerPath.set(architecturyCommonProject.loom.accessWidenerPath)
}

val common by configurations.creating

dependencies {
    implementation(architecturyCommonProject.sourceSets.main.get().output)
    common(project(architecturyCommonProject.path, "transformProduction${platform.capitalize()}"))
    "libraryImplementation"(project(":shared:${javaVersion.javaName}"))
}

tasks {
    classes {
        dependsOn(architecturyCommonProject.tasks.classes)
    }

    jar {
        dependsOn(architecturyCommonProject.tasks["transformProduction${platform.capitalize()}"])
    }

    jar {
        from(
            provider {
                common.map {
                    if (it.isDirectory) it else zipTree(it)
                }
            }
        )

        archiveClassifier.set("dev")
    }

    remapJar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        archiveBaseName.set(rootProject.name)
        archiveAppendix.set("${project.name}-${minecraftVersion}")
        archiveClassifier.set("remapped")
    }

    val releaseJar by registering(Jar::class) {
        group = "build"
        dependsOn(remapJar)

        from(
            remapJar.get().outputs.files.map {
                if (it.isDirectory) it else zipTree(it)
            }
        )

        from(
            provider {
                configurations["library"].map {
                    if (it.isDirectory) it else zipTree(it)
                }
            }
        )

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        archiveBaseName.set(rootProject.name)
        archiveAppendix.set("${project.name}-${minecraftVersion}")
        archiveClassifier.set("release")
    }

    artifacts {
        archives(releaseJar)
    }
}