plugins {
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
}

apply(plugin = "maven-publish")


group = "dev.brella"
version = "1.0.0"

repositories {
    mavenCentral()
    maven(url = "https://maven.brella.dev")
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("dev.brella:kornea-io:5.5.1-alpha")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":zshk-antlr-java"))
            }
        }
    }
}