import dev.fastmc.modsetup.ArchitecturyProjectExtension
import dev.fastmc.modsetup.AtPatchTask
import dev.fastmc.modsetup.forgeVersion
import dev.fastmc.modsetup.minecraftVersion

println("[Mod Setup] [architectury.forge] [${project.displayName}] Configuring architectury forge project")
val thisProject = project

project.parent!!.afterEvaluate {
    thisProject.loom.forge.mixinConfigs.addAll(
        extensions.getByType(ArchitecturyProjectExtension::class.java).mixinConfigs
    )
}

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    java
}

architectury {
    forge()
}

loom.forge {
    convertAccessWideners.set(true)
}

afterEvaluate {
    loom.forge {
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
    }
}

dependencies {
    "forge"("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
}

tasks {
    processResources {
        filesMatching("*/mods.toml") {
            expand("version" to project.version)
        }
    }
}

parent!!.afterEvaluate {
    val atPatchExtension = extensions.getByType(ArchitecturyProjectExtension::class.java).forge.atPatch
    val patches = atPatchExtension.patches
    if (patches.isNotEmpty()) {
        thisProject.tasks {
            remapJar {
                archiveClassifier.set("unpatched")
                create("atPatch", AtPatchTask::class.java, patches, this)
            }
        }
    }
}

afterEvaluate {
// Forge run is broken in dev env
//    tasks.register<Task>("genRuns") {
//        group = "ide"
//        doLast {
//            File(rootDir, ".idea/runConfigurations/${project.name}-${minecraftVersion}_runClient.xml").writer().use {
//                @Suppress("UNCHECKED_CAST")
//                val vmOptions = ((rootProject.ext["runVmOptions"] as List<String>) + listOf(
//                    "-Dfabric.dli.config=${project.projectDir.absolutePath}/.gradle/loom-cache/launch.cfg",
//                    "-Dfabric.dli.env=client",
//                    "-XX:+IgnoreUnrecognizedVMOptions",
//                    "--add-exports=java.base/sun.security.util=ALL-UNNAMED",
//                    "--add-exports=jdk.naming.dns/com.sun.jndi.dns=java.naming",
//                    "--add-opens=java.base/java.util.jar=ALL-UNNAMED",
//                    "-Dfabric.dli.main=net.minecraftforge.userdev.LaunchTesting",
//                    "-Darchitectury.main.class=${project.projectDir.absolutePath}/.gradle/architectury/.main_class",
//                    "-Darchitectury.runtime.transformer=${project.projectDir.absolutePath}/.gradle/architectury/.transforms",
//                    "-Darchitectury.properties=${project.projectDir.absolutePath}/.gradle/architectury/.properties",
//                    "-Djdk.attach.allowAttachSelf=true",
//                    "-javaagent:${rootProject.projectDir.absolutePath}/.gradle/architectury/architectury-transformer-agent.jar"
//                )).joinToString(" ")
//
//                it.write(
//                    """
//                        <component name="ProjectRunConfigurationManager">
//                          <configuration default="false" name="${project.name}-${minecraftVersion} runClient" type="Application" factoryName="Application">
//                            <envs>
//                              <env name="MOD_CLASSES" value="main%%${project.projectDir.absolutePath}/build/resources/main;main%%${project.projectDir.absolutePath}/build/classes/java/main;main%%${project.projectDir.absolutePath}/build/classes/kotlin/main" />
//                              <env name="MCP_MAPPINGS" value="loom.stub" />
//                              <env name="MCP_VERSION" value="20210115.111550" />
//                              <env name="FORGE_VERSION" value="$forgeVersion" />
//                              <env name="assetIndex" value="1.16.5-1.16" />
//                              <env name="assetDirectory" value="${gradle.gradleUserHomeDir}/caches/fabric-loom/assets" />
//                              <env name="nativesDirectory" value="${rootProject.projectDir.absolutePath}/.gradle/loom-cache/natives/1.16.5" />
//                              <env name="FORGE_GROUP" value="net.minecraftforge" />
//                              <env name="target" value="fmluserdevclient" />
//                              <env name="MC_VERSION" value="$minecraftVersion" />
//                            </envs>
//                            <option name="MAIN_CLASS_NAME" value="dev.architectury.transformer.TransformerRuntime" />
//                            <module name="${rootProject.name}.architectury-${minecraftVersion}.${project.name}.main" />
//                            <option name="PROGRAM_PARAMETERS" value="--width 1280 --height 720 --username TEST" />
//                            <option name="VM_PARAMETERS" value="$vmOptions" />
//                            <option name="WORKING_DIRECTORY" value="${rootProject.projectDir.absolutePath}/architectury-${minecraftVersion}/run" />
//                            <method v="2">
//                              <option name="Make" enabled="true" />
//                            </method>
//                          </configuration>
//                        </component>
//                    """.trimIndent()
//                )
//            }
//            file("${rootProject.projectDir.absolutePath}/architectury-${minecraftVersion}/run").mkdir()
//        }
//    }
}