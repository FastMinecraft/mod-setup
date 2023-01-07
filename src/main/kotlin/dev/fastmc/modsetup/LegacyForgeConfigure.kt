package dev.fastmc.modsetup

import dev.fastmc.loader.ModPlatform
import dev.fastmc.remapper.mapping.MappingName
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar
import java.io.File

class LegacyForgeConfigure(project: Project) : ProjectConfigure("legacyForge", project) {
    val projectExtension: ForgeProjectExtension =
        project.extensions.create("forgeProject", ForgeProjectExtension::class.java)
    val releaseElements: Configuration = project.configurations.create("releaseElements")

    val forgeVersion by project
    val mappingsChannel by project
    val mappingsVersion by project

    override fun configure() {
        project.pluginManager {
            apply("dev.fastmc.fast-remapper")
            apply("dev.fastmc.mod-loader-plugin")
            apply("dev.luna5ama.jar-optimizer")
            apply("net.minecraftforge.gradle")
        }

        project.idea {
            module {
                it.excludeDirs.add(this@LegacyForgeConfigure.project.file("run"))
            }
        }

        project.repositories {
            maven("https://repo.spongepowered.org/repository/maven-public/")
        }

        project.dependencies {
            add("minecraft", "net.minecraftforge:forge:${project.minecraftVersion}-$forgeVersion")
            add("compileOnly", project(":shared", "apiElements"))
            add("modCore", project(":shared", project.targetModCoreOutputName))
            add("libraryImplementation", "org.spongepowered:mixin:0.7.11-SNAPSHOT") {
                isTransitive = false
            }
        }

        project.extensions.configure(UserDevExtension::class.java) {
            it.mappings(mappingsChannel, mappingsVersion)
            it.runs.create("client")
        }

        project.fastRemapper {
            mcVersion(project.minecraftVersion!!)
            forge()
            mapping.set(MappingName.Mcp(mappingsChannel, mappingsVersion.substringBefore('-')))
            minecraftJar.set {
                project.configurations.getByName("minecraft").copy().apply { isTransitive = false }.singleFile
            }
            mixinConfigs.addAll(projectExtension.mixinConfigs)
        }

        val fastRemapJar = project.fastRemapper.register(project.tasks.jar)

        project.afterEvaluate {
            project.tasks.jar {
                projectExtension.coreModClass.orNull?.let {
                    manifest.attributes(
                        "FMLCorePluginContainsFMLMod" to true,
                        "FMLCorePlugin" to it
                    )
                }

                val mixinConfigs = projectExtension.mixinConfigs.get()
                if (mixinConfigs.isNotEmpty()) {
                    manifest.attributes(
                        "MixinConfigs" to mixinConfigs.joinToString(",")
                    )
                }

                projectExtension.accessTransformer?.let {
                    manifest.attributes(
                        "FMLAT" to it
                    )
                }
            }
        }

        project.tasks.clean {
            val set = mutableSetOf<Any>()
            project.buildDir.listFiles()?.filterNotTo(set) {
                it.name == "fg_cache"
            }
           delete = set
        }

        project.tasks.jar {
            fromConfiguration("modCoreRuntime")

            exclude {
                it.name.contains("devfix", true)
            }

            archiveClassifier.set("devmod")
        }

        val fatJar = project.tasks.register<Jar>("fatJar") {
            manifest {
                it.attributes(
                    "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker"
                )
                from(fastRemapJar.outputManifest)
            }

            val excludeDirs = setOf(
                "META-INF/com.android.tools",
                "META-INF/maven",
                "META-INF/proguard",
                "META-INF/versions"
            )
            val excludeNames = setOf(
                "module-info",
                "MUMFREY",
                "LICENSE",
                "kotlinx_coroutines_core"
            )
            exclude { file ->
                file.name.endsWith("kotlin_module")
                    || excludeNames.contains(file.file.nameWithoutExtension)
                    || excludeDirs.any { file.path.contains(it) }
            }

            fromJarTask(fastRemapJar)
            fromConfiguration("library")

            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            archiveClassifier.set("fatJar")
        }

        val optimizeFatJar =
            project.jarOptimizer.register(fatJar, projectExtension.modPackage.map { listOf(it, "org.spongepowered") })

        project.modLoader {
            modPackage.set(projectExtension.modPackage)
            defaultPlatform.set(ModPlatform.FORGE)
        }

        project.artifacts {
            it.add("archives", optimizeFatJar)
            it.add(releaseElements, optimizeFatJar)
            it.add("modLoaderPlatforms", optimizeFatJar)
        }

        project.afterEvaluate {
            project.tasks.register<Task>("genRuns") {
                group = "ide"
                doLast {
                    File(project.rootDir, ".idea/runConfigurations").mkdirs()
                    File(project.rootDir, ".idea/runConfigurations/${project.name}_runClient.xml").writer()
                        .use { writer ->
                            val vmOptionsList = mutableListOf<String>()
                            vmOptionsList.addAll(project.runVmOptions.options)
                            vmOptionsList.addAll(
                                listOf(
                                    "-Dforge.logging.console.level=info",
                                    "-Dmixin.env.disableRefMap=true",
                                )
                            )
                            projectExtension.devCoreModClass.orElse(projectExtension.coreModClass).orNull.let {
                                vmOptionsList.add("-Dfml.coreMods.load=$it")
                            }

                            val runDir = File(project.projectDir, "run")
                            runDir.mkdirs()

                            val buildDir = project.buildDir.absolutePath

                            writer.write(
                                """
                            <component name="ProjectRunConfigurationManager">
                              <configuration default="false" name="${project.name} runClient" type="Application" factoryName="Application">
                              <option name="ALTERNATIVE_JRE_PATH" value="${project.launchJavaToolchain.get().executablePath.asFile.parentFile.parent}" />
                              <option name="ALTERNATIVE_JRE_PATH_ENABLED" value="true" />
                                <envs>
                                  <env name="MCP_TO_SRG" value="${buildDir}/createSrgToMcp/output.srg" />
                                  <env name="MOD_CLASSES" value="$${buildDir}/resources/main;${buildDir}/classes/java/main;${buildDir}/classes/kotlin/main" />
                                  <env name="mainClass" value="net.minecraft.launchwrapper.Launch" />
                                  <env name="MCP_MAPPINGS" value="${mappingsChannel}_$mappingsVersion" />
                                  <env name="FORGE_VERSION" value="$forgeVersion" />
                                  <env name="assetIndex" value="1.12" />
                                  <env name="assetDirectory" value="${
                                    project.gradle.gradleUserHomeDir.path.replace(
                                        '\\',
                                        '/'
                                    )
                                }/caches/forge_gradle/assets" />
                                  <env name="nativesDirectory" value="${buildDir}/natives" />
                                  <env name="FORGE_GROUP" value="net.minecraftforge" />
                                  <env name="tweakClass" value="net.minecraftforge.fml.common.launcher.FMLTweaker" />
                                  <env name="MC_VERSION" value="${'$'}{MC_VERSION}" />
                                </envs>
                                <option name="MAIN_CLASS_NAME" value="net.minecraftforge.legacydev.MainClient" />
                                <module name="${rootProject.name}.${project.name}.main" />
                                <option name="PROGRAM_PARAMETERS" value="--width 1280 --height 720 --username TEST" />
                                <option name="VM_PARAMETERS" value="${vmOptionsList.joinToString(" ")}" />
                                <option name="WORKING_DIRECTORY" value="${runDir.absolutePath}" />
                                <method v="2">
                                  <option name="Gradle.BeforeRunTask" enabled="true" tasks="${project.name}:prepareRunClient" externalProjectPath="${project.rootDir.absolutePath}" />
                                </method>
                              </configuration>
                            </component>
                        """.trimIndent()
                            )
                        }
                }
            }

            disableTask(project.tasks.findByName("reobfJar"))
        }
    }
}
