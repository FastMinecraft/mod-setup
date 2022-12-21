package dev.fastmc.modsetup

println("[Mod Setup] [architectury.platform] [${project.displayName}] Configuring architectury platform project")

val platform = project.property("loom.platform") as String
val architecturyCommonProject = project("${project.parent!!.path}:common")

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    java
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

val common by configurations.creating

dependencies {
    implementation(architecturyCommonProject.sourceSets.main.get().output)
    common(project(architecturyCommonProject.path, "transformProduction${platform.capitalize()}"))
    "modCore"(project(architecturyCommonProject.path, "modCore"))
}

tasks {
    jar {
        dependsOn(architecturyCommonProject.tasks["transformProduction${platform.capitalize()}"])

        from(
            provider {
                (configurations["modCore"] + common).map {
                    if (it.isDirectory) it else zipTree(it)
                }
            }
        ) {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        archiveClassifier.set("dev")
    }

    val devModJar by registering(Jar::class) {
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

    afterEvaluate {
        val releaseJar by registering(Jar::class) {
            group = "build"
            val taskInput = findByName("atPatch") ?: remapJar.get()
            dependsOn(taskInput)
            mustRunAfter(taskInput)

            manifest {
                from(provider {
                    zipTree(taskInput.outputs.files.singleFile).find {
                        it.name == "MANIFEST.MF"
                    }
                })
            }

            from(
                provider {
                    configurations["library"].map {
                        if (it.isDirectory) it else zipTree(it)
                    }
                }
            ) {
                exclude("META-INF/**")
            }

            from(
                zipTree(taskInput.outputs.files.singleFile)
            )
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            archiveBaseName.set(rootProject.name)
            archiveAppendix.set("${project.name}-${minecraftVersion}")
            archiveClassifier.set("release")
        }

        artifacts {
            archives(devModJar)
            archives(releaseJar)
        }
    }
}