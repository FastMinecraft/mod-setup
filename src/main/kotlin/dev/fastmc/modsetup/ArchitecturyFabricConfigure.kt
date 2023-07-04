package dev.fastmc.modsetup

import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

class ArchitecturyFabricConfigure(project: Project) : ProjectConfigure("architecturyForge", project) {
    override fun configure() {
        project.architectury {
            fabric()
            platformSetupLoomIde()
        }

        project.dependencies {
            add("modImplementation", "net.fabricmc:fabric-loader:${project.fabricLoaderVersion}")
        }

        project.afterEvaluate {
            project.tasks.create<Task>("genRuns") {
                group = "ide"

                dependsOn("configureClientLaunch")

                doLast {
                    File(project.rootDir, ".idea/runConfigurations").mkdirs()
                    File(
                        project.rootDir,
                        ".idea/runConfigurations/${project.name}-${project.minecraftVersion}_runClient.xml"
                    ).writer().use {
                        val vmOptions = (project.runVmOptions.options.toList() + listOf(
                            "-Dfabric.dli.config=${project.projectDir}/.gradle/loom-cache/launch.cfg",
                            "-Dfabric.dli.env=client",
                            "-Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotClient",
                            "-Darchitectury.main.class=${project.projectDir}/.gradle/architectury/.main_class",
                            "-Darchitectury.runtime.transformer=${project.projectDir}/.gradle/architectury/.transforms",
                            "-Darchitectury.properties=${project.projectDir}/.gradle/architectury/.properties",
                            "-Djdk.attach.allowAttachSelf=true",
                            "-javaagent:${project.rootDir}/.gradle/architectury/architectury-transformer-agent.jar"
                        )).joinToString(" ")

                        val runDir = project.file("${project.parent!!.projectDir.absolutePath}/run")
                        runDir.mkdir()

                        it.write(
                            """
                        <component name="ProjectRunConfigurationManager">
                          <configuration default="false" name="${project.name}-${project.minecraftVersion} runClient" type="Application" factoryName="Application">
                            <option name="ALTERNATIVE_JRE_PATH" value="${project.launchJavaToolchain.get().executablePath.asFile.parentFile.parent}" />
                            <option name="ALTERNATIVE_JRE_PATH_ENABLED" value="true" />
                            <option name="MAIN_CLASS_NAME" value="dev.architectury.transformer.TransformerRuntime" />
                            <module name="${rootProject.name}.architectury-${project.minecraftVersion}.${project.name}.main" />
                            <option name="PROGRAM_PARAMETERS" value="--width 1280 --height 720 --username TEST" />
                            <option name="VM_PARAMETERS" value="$vmOptions" />
                            <option name="WORKING_DIRECTORY" value="${runDir.absolutePath}" />
                            <method v="2">
                              <option name="Gradle.BeforeRunTask" enabled="true" tasks="${project.path}:jar" externalProjectPath="${project.rootDir.absolutePath}" />
                            </method>
                          </configuration>
                        </component>
                    """.trimIndent()
                        )
                    }
                }
            }
        }
    }
}