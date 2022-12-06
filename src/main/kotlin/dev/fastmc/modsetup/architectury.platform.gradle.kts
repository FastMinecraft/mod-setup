import dev.fastmc.modsetup.javaName
import dev.fastmc.modsetup.javaVersion
import dev.fastmc.modsetup.minecraftVersion

println("[Mod Setup] [architectury.platform] [${project.displayName}] Configuring architectury platform project")

val platform = project.property("loom.platform") as String
val architecturyCommonProject = project("${project.parent!!.path}:common")

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

dependencies {
    runtimeOnly(architecturyCommonProject.sourceSets.main.get().output)
    "library"(project(architecturyCommonProject.path, "transformProduction${platform.capitalize()}"))
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
        archiveClassifier.set("dev")
    }

    remapJar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        archiveBaseName.set(rootProject.name)
        archiveAppendix.set("${project.name}-${minecraftVersion}")
        archiveClassifier.set("release")
    }
}

afterEvaluate {
    tasks {
        jar {
            from(
                configurations["library"].map {
                    if (it.isDirectory) it else zipTree(it)
                }
            )
        }
    }
}