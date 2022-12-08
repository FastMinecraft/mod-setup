import dev.fastmc.modsetup.*
import net.minecraftforge.gradle.userdev.UserDevExtension

val forgeProjectExtension = extensions.create("forgeProject", ForgeProjectExtension::class.java)

plugins {
    java
}

apply {
    plugin("net.minecraftforge.gradle")
    plugin("org.spongepowered.mixin")
}

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

val forgeVersion: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project

dependencies {
    // Jar packaging
    fun ModuleDependency.exclude(moduleName: String): ModuleDependency {
        return exclude(module = moduleName)
    }

    // Forge
    "minecraft"("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    // Dependencies
    compileOnly(project(":shared"))
    "libraryImplementation"(project(":shared:java8"))

    "libraryImplementation"("org.spongepowered:mixin:0.8.+") {
        isTransitive = false
    }

    "annotationProcessor"("org.spongepowered:mixin:0.8.+:processor") {
        isTransitive = false
    }
}

configure<UserDevExtension> {
    mappings(mappingsChannel, mappingsVersion)

    runs {
        create("client") {
            workingDirectory = project.file("run").path
            ideaModule("${rootProject.name}.${project.name}.main")

            properties(
                mapOf(
                    "forge.logging.console.level" to "info",
                    "fml.coreMods.load" to "me.luna.fastmc.FastMcDevFixCoremod",
                    "mixin.env.disableRefMap" to "true"
                )
            )
        }
    }
}

afterEvaluate {
    tasks {
        jar {
            exclude {
                it.name.contains("devfix", true)
            }

            archiveBaseName.set(rootProject.name)
            archiveAppendix.set(project.name)
        }

        val releaseJar = register<Jar>("releaseJar") {
            group = "build"
            dependsOn("reobfJar")

            manifest {
                attributes(
                    "Manifest-Version" to 1.0,
                    "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
                    "FMLCorePluginContainsFMLMod" to true,
                    "FMLCorePlugin" to "me.luna.fastmc.FastMcCoremod",
                )

                if (forgeProjectExtension.mixinConfigs.isNotEmpty()) {
                    attributes(
                        "MixinConfigs" to forgeProjectExtension.mixinConfigs.joinToString(",")
                    )
                }

                forgeProjectExtension.accessTransformer?.let {
                    attributes(
                        "FMLAT" to it
                    )
                }
            }

            val excludeDirs = listOf(
                "META-INF/com.android.tools",
                "META-INF/maven",
                "META-INF/proguard",
                "META-INF/versions"
            )
            val excludeNames = hashSetOf(
                "module-info",
                "MUMFREY",
                "LICENSE",
                "kotlinx_coroutines_core"
            )

            from(
                jar.get().outputs.files.map {
                    if (it.isDirectory) it else zipTree(it)
                }
            )

            exclude { file ->
                file.name.endsWith("kotlin_module")
                    || excludeNames.contains(file.file.nameWithoutExtension)
                    || excludeDirs.any { file.path.contains(it) }
            }

            from(
                configurations["library"].map {
                    if (it.isDirectory) it else zipTree(it)
                }
            )

            archiveBaseName.set(rootProject.name)
            archiveAppendix.set(project.name)
            archiveClassifier.set("release")
        }

        afterEvaluate {
            getByName("reobfJar").finalizedBy(releaseJar)
        }

        artifacts {
            archives(releaseJar)
        }

        clean {
            val set = mutableSetOf<Any>()
            buildDir.listFiles()?.filterNotTo(set) {
                it.name == "fg_cache"
            }
            delete = set
        }

        register<Task>("genRuns") {
            group = "ide"
            doLast {
                File(rootDir, ".idea/runConfigurations/${project.name}_runClient.xml").writer().use { writer ->
                    @Suppress("UNCHECKED_CAST")
                    val vmOptionsList = mutableListOf<String>()
                    vmOptionsList.addAll(runVmOptions.options)
                    vmOptionsList.addAll(
                        listOf(
                            "-Dforge.logging.console.level=info",
                            "-Dmixin.env.disableRefMap=true",
                        )
                    )
                    forgeProjectExtension.coreModClass.orNull?.let {
                        vmOptionsList.add("-Dfml.coreMods.load=$it")
                    }

                    val runDir = File(projectDir, "run")
                    runDir.mkdirs()

                    val buildDir = project.buildDir.absolutePath

                    writer.write(
                        """
                        <component name="ProjectRunConfigurationManager">
                          <configuration default="false" name="${project.name} runClient" type="Application" factoryName="Application">
                            <envs>
                              <env name="MCP_TO_SRG" value="${buildDir}/createSrgToMcp/output.srg" />
                              <env name="MOD_CLASSES" value="$${buildDir}/resources/main;${buildDir}/classes/java/main;${buildDir}/classes/kotlin/main" />
                              <env name="mainClass" value="net.minecraft.launchwrapper.Launch" />
                              <env name="MCP_MAPPINGS" value="${mappingsChannel}_$mappingsVersion" />
                              <env name="FORGE_VERSION" value="$forgeVersion" />
                              <env name="assetIndex" value="1.12" />
                              <env name="assetDirectory" value="${
                            gradle.gradleUserHomeDir.path.replace(
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
                              <option name="Gradle.BeforeRunTask" enabled="true" tasks="${project.name}:prepareRunClient" externalProjectPath="${rootDir.absolutePath}" />
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