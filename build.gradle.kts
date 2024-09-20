import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "dev.fastmc"
version = "1.3.2"

plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm")
    id("dev.fastmc.maven-repo").version("1.0.0")
}

repositories {
    maven("https://maven.luna5ama.dev/")
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://files.minecraftforge.net/maven/")
    maven("https://maven.architectury.dev/")
}

val kotlinVersion: String by project

configurations {
    all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "org.jetbrains.kotlin") {
                    useVersion(kotlinVersion)
                }
            }
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation(kotlin("stdlib-jdk8", kotlinVersion))

    implementation("dev.luna5ama:jar-optimizer:1.2.1")
    implementation("dev.fastmc:fast-remapper:1.1.1")
    implementation("dev.fastmc:mod-loader-plugin:1.1.1")

    implementation("architectury-plugin:architectury-plugin.gradle.plugin:3.4-SNAPSHOT")
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.7-SNAPSHOT")
    implementation("net.minecraftforge.gradle:ForgeGradle:6.0.20")
}

gradlePlugin {
    plugins {
        create("mod-setup") {
            id = "dev.fastmc.mod-setup"
            displayName = "Mod Setup"
            description = "Gradle plugin for setting up Minecraft mod project"
            implementationClass = "dev.fastmc.modsetup.ModSetupPlugin"
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

kotlin {
    val jvmArgs = mutableSetOf<String>()
    (rootProject.findProperty("kotlin.daemon.jvmargs") as? String)
        ?.split("\\s+".toRegex())?.toCollection(jvmArgs)
    System.getenv("KOTLIN_DAEMON_VM")
        ?.split("\\s+".toRegex())?.toCollection(jvmArgs)

    kotlinDaemonJvmArgs = jvmArgs.toList()

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs = listOf(
            "-Xbackend-threads=0"
        )
    }
}

tasks {
    withType(JavaCompile::class.java) {
        options.encoding = "UTF-8"
    }
}
