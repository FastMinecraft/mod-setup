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

    tasks {
        processResources {
            from(sharedProject.sourceSets.main.get().resources)
        }
        compileJava {
            source(sharedProject.sourceSets.main.get().java)
        }
        compileKotlin {
            source(sharedProject.kotlin.sourceSets["main"].kotlin)
        }
        sharedProject.tasks.classes.get().dependsOn(classes)
    }
}

tasks {
    disableTask(compileJava)
    disableTask(compileKotlin)
    disableTask(processResources)
    disableTask(jar)
}