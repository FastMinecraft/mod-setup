package dev.fastmc.modsetup

import org.gradle.api.Project

class SharedConfigure(project: Project) : ProjectConfigure("shared", project) {
    override fun configure() {
        // No-op
    }
}