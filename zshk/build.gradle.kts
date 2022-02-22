plugins {
    kotlin("multiplatform")
}

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
                implementation("dev.brella:kornea-io:5.4.3-alpha")
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
                implementation(project(":antlr-java"))
            }
        }
    }
}