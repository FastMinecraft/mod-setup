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

    val count = run {
        val regex = "project ':architectury-[\\d.]+:(forge|fabric)'".toRegex()
        subprojects.filter { it.displayName.matches(regex) }.size
    }

    val taskList = mutableListOf<Task>()

    subprojects {
        tasks.findByName("ideaSyncTask")?.finalizedBy(clearRuns)
        tasks.findByName("transformProductionForge")?.let {
            taskList.add(it)
        }
        tasks.findByName("transformProductionFabric")?.let {
            taskList.add(it)
        }

        if (taskList.size == count) {
            var last = taskList.first()
            for (i in 1 until taskList.size) {
                val task = taskList[i]
                task.mustRunAfter(last)
                last = task
            }
        }
    }
}