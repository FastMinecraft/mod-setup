package dev.fastmc.modsetup

println("[Mod Setup] [architectury.fabric] [${project.displayName}] Configuring architectury fabric project")

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    java
}

architectury {
    fabric()
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}

afterEvaluate {
    tasks.register<Task>("genRuns") {
        group = "ide"
        doLast {
            File(rootDir, ".idea/runConfigurations").mkdirs()
            File(rootDir, ".idea/runConfigurations/${project.name}-${minecraftVersion}_runClient.xml").writer().use {
                val rootProjectDir = rootDir

                val vmOptions =
                    (runVmOptions.options.toList() + listOf(
                        "-Dfabric.dli.config=${rootProjectDir}/.gradle/loom-cache/launch.cfg",
                        "-Dfabric.dli.env=client",
                        "-Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotClient",
                        "-Darchitectury.main.class=${rootProjectDir}/.gradle/architectury/.main_class",
                        "-Darchitectury.runtime.transformer=${rootProjectDir}/.gradle/architectury/.transforms",
                        "-Darchitectury.properties=${rootProjectDir}/.gradle/architectury/.properties",
                        "-Djdk.attach.allowAttachSelf=true",
                        "-javaagent:$rootProjectDir/.gradle/architectury/architectury-transformer-agent.jar"
                    )).joinToString(" ")

                val runDir = file("${parent!!.projectDir.absolutePath}/run")
                runDir.mkdir()

                it.write(
                    """
                        <component name="ProjectRunConfigurationManager">
                          <configuration default="false" name="${project.name}-${minecraftVersion} runClient" type="Application" factoryName="Application">
                            <option name="ALTERNATIVE_JRE_PATH" value="${launchJavaToolchain.get().executablePath.asFile.parentFile.parent}" />
                            <option name="ALTERNATIVE_JRE_PATH_ENABLED" value="true" />
                            <option name="MAIN_CLASS_NAME" value="dev.architectury.transformer.TransformerRuntime" />
                            <module name="${rootProject.name}.architectury-${minecraftVersion}.${project.name}.main" />
                            <option name="PROGRAM_PARAMETERS" value="--width 1280 --height 720 --username TEST" />
                            <option name="VM_PARAMETERS" value="$vmOptions" />
                            <option name="WORKING_DIRECTORY" value="${runDir.absolutePath}" />
                            <method v="2">
                              <option name="Make" enabled="true" />
                            </method>
                          </configuration>
                        </component>
                    """.trimIndent()
                )
            }
        }
    }
}