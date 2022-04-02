buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.17.1")
    }
}

plugins {
    kotlin("multiplatform") version "1.6.10" apply false
    kotlin("jvm") version "1.6.10" apply false
}

configure(subprojects) {
    apply(plugin = "maven-publish")

    group = "dev.brella"


    configure<PublishingExtension> {
        repositories {
            maven(url = "${rootProject.buildDir}/repo")
        }
    }
}