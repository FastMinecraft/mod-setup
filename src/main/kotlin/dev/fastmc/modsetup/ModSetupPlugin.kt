package dev.fastmc.modsetup

import org.gradle.api.Plugin
import org.gradle.api.Project

class ModSetupPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        RootConfigure(project).configure()
    }
}