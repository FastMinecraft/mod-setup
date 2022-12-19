package dev.fastmc.modsetup

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

        artifacts {
            add("modCore", jar)
        }
    }
}

tasks {
    disableTask(compileJava)
    disableTask(compileKotlin)
    disableTask(processResources)
    disableTask(jar)
}