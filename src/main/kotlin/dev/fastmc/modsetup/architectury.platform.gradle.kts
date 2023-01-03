package dev.fastmc.modsetup

println("[Mod Setup] [architectury.platform] [${project.displayName}] Configuring architectury platform project")

val platform = project.property("loom.platform") as String
val architecturyCommonProject = project("${project.parent!!.path}:common")
val projectExtension = project.parent!!.extensions.getByType(ArchitecturyProjectExtension::class.java)

plugins {
    java
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("dev.luna5ama.jar-optimizer")
}

apply {
    plugin("dev.fastmc.modsetup.architectury.$platform")
}

architectury {
    platformSetupLoomIde()
}

loom {
    accessWidenerPath.set(architecturyCommonProject.loom.accessWidenerPath)
}

val releaseElements by configurations.creating

dependencies {
    implementation(project(architecturyCommonProject.path, "namedElements"))
    "modCoreRuntime"(project(architecturyCommonProject.path, "transformProduction${platform.capitalize()}"))
    "modCore"(project(architecturyCommonProject.path, "modCore"))
}

tasks {
    jar {
        dependsOn(architecturyCommonProject.tasks["transformProduction${platform.capitalize()}"])

        from(
            provider {
                configurations["modCoreRuntime"].map {
                    if (it.isDirectory) it else zipTree(it)
                }
            }
        ) {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        archiveClassifier.set("dev")
    }

    val devModJar by creating(Jar::class) {
        from(
            jar.map { jarTask -> jarTask.archiveFile.map { zipTree(it) } }
        )

        archiveBaseName.set(rootProject.name)
        archiveAppendix.set("${project.name}-${minecraftVersion}")
        archiveClassifier.set("devmod")
    }

    remapJar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        archiveBaseName.set(rootProject.name)
        archiveAppendix.set("${project.name}-${minecraftVersion}")
        archiveClassifier.set("remapped")
    }
}

val fatJar by tasks.registering(Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations["library"].elements.map { set ->
        set.map { it.asFile }.map { if (it.isDirectory) it else zipTree(it) }
    }) {
        exclude("META-INF/**")
    }

    archiveBaseName.set(rootProject.name)
    archiveAppendix.set("${project.name}-${minecraftVersion}")
    archiveClassifier.set("farJar")
}

afterEvaluate {
    fatJar.configure {
        val taskInput = tasks.findByName("atPatch") ?: tasks.remapJar.get()
        dependsOn(taskInput)
        mustRunAfter(taskInput)

        manifest.from(provider { zipTree(taskInput.outputs.files.singleFile).find { it.name == "MANIFEST.MF" } })

        from(zipTree(taskInput.outputs.files.singleFile))
    }
}

jarOptimizer {
    optimize(fatJar, projectExtension.modPackage.map { listOf(it) })
}

afterEvaluate {
    artifacts {
        archives(tasks.getByName("devModJar"))
        archives(tasks.getByName("optimizeFatJar"))
        add(releaseElements.name, tasks.getByName("optimizeFatJar"))
    }
}