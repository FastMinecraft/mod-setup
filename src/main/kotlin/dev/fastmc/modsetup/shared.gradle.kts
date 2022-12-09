import dev.fastmc.modsetup.disableTask

plugins {
    java
    kotlin("jvm")
}

val sharedProject = project

subprojects {
    apply {
        plugin("java")
        plugin("kotlin")
    }

    dependencies {
        val kotlinVersion: String by rootProject
        val kotlinxCoroutineVersion: String by rootProject

        "libraryApi"("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
        "libraryApi"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")
    }

    tasks {
        processResources {
            from(sharedProject.sourceSets.main.get().resources)
        }
        compileJava {
            source(sharedProject.sourceSets.main.get().java)
        }
        compileKotlin {
            source(sharedProject.sourceSets.main.get().kotlin)
        }
        sharedProject.tasks.classes.get().dependsOn(classes)

        jar {
            archiveBaseName.set(sharedProject.name)
            archiveClassifier.set(project.name)
        }
    }
}

tasks {
    disableTask(compileJava)
    disableTask(compileKotlin)
    disableTask(processResources)
    disableTask(jar)
}