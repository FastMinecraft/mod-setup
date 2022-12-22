package dev.fastmc.modsetup

import dev.fastmc.multijdk.MultiJdkExtension

apply {
    plugin("dev.fastmc.multi-jdk")
}

configure<MultiJdkExtension> {
    baseJavaVersion(JavaLanguageVersion.of(8))
    rootProject.allprojects
        .map { it.javaVersion }
        .forEach {
            newJavaVersion(it)
        }
    sourceSets.values.forEach {
        configurations.named(it.implementationConfigurationName) {
            extendsFrom(configurations.getByName("implementation"))
        }
    }
}

configurations.getByName("modCore")
    .extendsFrom(configurations.getByName("java8ModCore"))
