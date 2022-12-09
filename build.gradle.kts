plugins {
    `kotlin-dsl`
}

group = "dev.fastmc"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://files.minecraftforge.net/maven/")
    maven("https://maven.architectury.dev/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.22")
    implementation("architectury-plugin:architectury-plugin.gradle.plugin:3.4.+")
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.0.+")
    implementation("net.minecraftforge.gradle:ForgeGradle:5.+")
    implementation("org.spongepowered:mixingradle:0.7.+")
}

kotlin {
    val jvmArgs = mutableListOf<String>()
    """
        -Xms128M
        -Xmx512M
        -XX:+UnlockExperimentalVMOptions
        -XX:CompileThresholdScaling=0.25
        -XX:+AlwaysPreTouch
        -XX:+ParallelRefProcEnabled
        -XX:+UseG1GC
        -XX:+UseStringDeduplication
        -XX:MaxGCPauseMillis=200
        -XX:G1NewSizePercent=10
        -XX:G1MaxNewSizePercent=25
        -XX:G1HeapRegionSize=1M
        -XX:G1MixedGCCountTarget=4
        -XX:InitiatingHeapOccupancyPercent=75
        -XX:G1RSetUpdatingPauseTimePercent=25
        -XX:MinHeapFreeRatio=5
        -XX:MaxHeapFreeRatio=10
        -XX:ParallelGCThreads=4
        -XX:ConcGCThreads=1
        -XX:G1PeriodicGCInterval=10000
    """.trimIndent().lineSequence().filter { it.isNotBlank() }.toCollection(jvmArgs)
    System.getProperty("use_large_pages")?.let {
        if (it.equals("t", true) || it.equals("true", true)) {
            jvmArgs.add("-XX:+UseLargePages")
        }
    }
    kotlinDaemonJvmArgs = jvmArgs
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