group = "dev.fastmc"
version = "1.0-SNAPSHOT"

plugins {
    `kotlin-dsl`
    `maven-publish`
    id("dev.fastmc.maven-repo").version("1.0.0")
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.fastmc.dev/")
    maven("https://maven.fabricmc.net/")
    maven("https://files.minecraftforge.net/maven/")
    maven("https://maven.architectury.dev/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    val kotlinVersion: String by project

    implementation("dev.fastmc:multi-jdk:1.1.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("architectury-plugin:architectury-plugin.gradle.plugin:3.4-SNAPSHOT")
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.0-SNAPSHOT")
    implementation("net.minecraftforge.gradle:ForgeGradle:5.1.58")
    implementation("org.spongepowered:mixingradle:0.7-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}