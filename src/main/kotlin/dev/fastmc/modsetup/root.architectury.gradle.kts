package dev.fastmc.modsetup

println("[Mod Setup] [root.architectury] [${project.displayName}] Configuring architectury for root project")

subprojects.filter {
    it.rootProject == project && it.name.startsWith("architectury-")
}.forEach {
    it.apply {
        plugin("architectury.root")
    }
}

tasks {
    val clearRuns by register<Task>("clearRuns") {
        doLast {
            val regex =
                "Minecraft_(Server|Client)___architectury.+?(fabric|forge)__architectury-.+?(fabric|forge).xml".toRegex()
            File(rootProject.projectDir.absoluteFile, ".idea/runConfigurations").listFiles()?.let { files ->
                files.asSequence()
                    .filter { it.name.matches(regex) }
                    .forEach { it.delete() }
            }
        }
    }

    subprojects {
        afterEvaluate {
            tasks.findByName("ideaSyncTask")?.finalizedBy(clearRuns)
        }
    }
}