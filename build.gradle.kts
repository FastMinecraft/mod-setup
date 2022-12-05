plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    `java-gradle-plugin`
}

group = "dev.fastmc"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://files.minecraftforge.net/maven/")
    maven("https://maven.architectury.dev/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.22")
    implementation("architectury-plugin:architectury-plugin.gradle.plugin:3.4.+")
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:0.12.+")
}

kotlin {
    val jvmArgs = mutableListOf<String>()
        """
            -Xms128M
            -Xmx512M
            -XX:+UnlockExperimentalVMOptions
            -XX:+AlwaysPreTouch
            -XX:+ParallelRefProcEnabled
            -XX:+UseG1GC
            -XX:+UseStringDeduplication
            -XX:MaxGCPauseMillis=50
            -XX:G1NewSizePercent=10
            -XX:G1MaxNewSizePercent=25
            -XX:G1HeapRegionSize=1M
            -XX:G1MixedGCCountTarget=4
            -XX:InitiatingHeapOccupancyPercent=80
            -XX:G1RSetUpdatingPauseTimePercent=25
            -XX:G1HeapWastePercent=10
            -XX:G1ReservePercent=5
            -XX:MinHeapFreeRatio=5
            -XX:MaxHeapFreeRatio=10
            -XX:ParallelGCThreads=4
            -XX:ConcGCThreads=2
            -XX:G1PeriodicGCInterval=5000
        """.trimIndent().lineSequence().filter { it.isNotBlank() }.toCollection(jvmArgs)
    System.getProperty("use_large_pages")?.let {
        if (it.equals("t", true) || it.equals("true", true)) {
            jvmArgs.add("-XX:+UseLargePages")
        }
    }
    kotlinDaemonJvmArgs = jvmArgs
}